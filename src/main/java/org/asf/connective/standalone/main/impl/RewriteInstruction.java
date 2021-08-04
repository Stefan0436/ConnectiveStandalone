package org.asf.connective.standalone.main.impl;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.asf.connective.standalone.ContextFileInstruction;
import org.asf.connective.standalone.main.ConnectiveConfiguration;
import org.asf.connective.standalone.main.ConnectiveStandalone;
import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;
import org.asf.rats.http.ProviderContext;
import org.asf.rats.http.ProviderContextFactory;
import org.asf.rats.http.providers.IContextProviderExtension;
import org.asf.rats.http.providers.IContextRootProviderExtension;
import org.asf.rats.http.providers.IServerProviderExtension;
import org.asf.rats.http.providers.IVirtualFileProvider;

public class RewriteInstruction implements ContextFileInstruction {

	private interface Command {
		public String id();

		public void run(HttpRequest request, HttpResponse response, Socket client, String method,
				ArrayList<String> arguments);
	}

	private class BodyCommand implements Command {

		@Override
		public String id() {
			return "body";
		}

		@Override
		public void run(HttpRequest request, HttpResponse response, Socket client, String method,
				ArrayList<String> arguments) {
			if (arguments.size() > 1) {
				response.setContent(arguments.get(0), arguments.get(1).replace("\\n", "\n"));
			} else if (arguments.size() > 0) {
				if (arguments.get(0).equals("null")) {
					response.body = null;
				} else {
					response.setContent("text/plain", arguments.get(0).replace("\\n", "\n"));
				}
			}
		}

	}

	private class StatusCommand implements Command {

		@Override
		public String id() {
			return "status";
		}

		@Override
		public void run(HttpRequest request, HttpResponse response, Socket client, String method,
				ArrayList<String> arguments) {
			if (arguments.size() >= 2) {
				response.status = Integer.valueOf(arguments.get(0));
				response.message = arguments.get(1);
			}
		}

	}

	public class RewriteVirtualFile implements IVirtualFileProvider, IServerProviderExtension,
			IContextProviderExtension, IContextRootProviderExtension {
		private String[] arguments;
		private String task;
		private String matcher;

		private String contextRoot;
		private ConnectiveHTTPServer server;
		private ProviderContext ctx;

		private Command[] commands = new Command[] { new BodyCommand(), new StatusCommand() };

		public RewriteVirtualFile(String[] arguments, String task, String matcher) {
			this.arguments = arguments;
			this.task = task;
			this.matcher = matcher;
		}

		@Override
		public boolean match(String path, HttpRequest request) {
			String matchTest = matcher;

			if (path.endsWith("/"))
				path = path.substring(0, path.length() - 1);

			matchTest = this.parseVars(matchTest, path, request, null);
			boolean match = true;
			for (int i = 2; i < arguments.length; i++) {
				boolean not = false;
				String matcher = arguments[i];
				if (matcher.startsWith("%NOT%")) {
					matcher = matcher.substring(5);
					not = true;
				}

				Pattern pattern = Pattern.compile(matcher, Pattern.CASE_INSENSITIVE);
				Matcher matcherInst = pattern.matcher(matchTest);
				boolean b = matcherInst.find();
				match = (not ? !b : b);

				if (match)
					return true;
			}

			return match;
		}

		@Override
		public IVirtualFileProvider newInstance() {
			return new RewriteVirtualFile(arguments, task, matcher);
		}

		@Override
		public void process(String path, String uploadMediaType, HttpRequest request, HttpResponse response,
				Socket client, String method) {
			String taskFile = ConnectiveConfiguration.getInstance().tasks.get(task);
			for (String line : taskFile.replace("\r", "").split("\n")) {
				if (!line.isEmpty() && !line.startsWith("#")) {
					line = parseVars(line, path, request, response);

					ArrayList<String> cmd = ConnectiveStandalone.parseCommand(line);
					if (cmd.size() != 0) {
						String command = cmd.get(0);
						cmd.remove(0);
						if (Stream.of(commands).anyMatch(t -> t.id().equalsIgnoreCase(command))) {
							Stream.of(commands).filter(t -> t.id().equalsIgnoreCase(command)).findFirst().get()
									.run(request, response, client, method, cmd);
						} else {
							if (cmd.size() > 0)
								response.setHeader(command, cmd.get(0));
						}
					}
				}
			}
		}

		private String parseVars(String line, String path, HttpRequest request, HttpResponse response) {
			if (request.headers.containsKey("Host")) {
				String host = request.headers.get("Host");
				line = line.replace("%host%", host);
				String port = "";
				if (host.contains(":")) {
					port = host.substring(host.indexOf(":") + 1);
					host = host.substring(0, host.indexOf(":"));
				}
				line = line.replace("%host.name%", host);
				line = line.replace("%host.port%", port);
			} else {
				line = line.replace("%host.name%", "");
				line = line.replace("%host.port%", "");
			}

			line = line.replace("%file%", path);
			if (ctx != null) {
				line = line.replace("%context.root%", contextRoot);
			}
			if (server != null) {
				line = line.replace("%server.port%", Integer.toString(server.getPort()));
				line = line.replace("%server.version%", server.getVersion());
				line = line.replace("%server.name%", server.getName());
				line = line.replace("%server.ip%", server.getIp().toString());
				line = line.replace("%server.http.protocol%", server.getPreferredProtocol());
			}

			line = line.replace("%request.http.version%", request.version);
			line = line.replace("%request.http.method%", request.method);
			line = line.replace("%request.http.query%", (request.query != null ? request.query : ""));
			line = line.replace("%request.http.path%", request.path);

			for (String header : request.headers.keySet()) {
				line = line.replace("%request.headers." + header.toLowerCase() + "%", request.headers.get(header));
			}

			if (response != null) {
				line = line.replace("%response.http.status%", Integer.toString(response.status));
				line = line.replace("%response.http.message%", response.message);
				for (String header : response.headers.keySet()) {
					if (header.contains("#"))
						continue;

					line = line.replace("%response.headers." + header.toLowerCase() + "%",
							response.headers.get(header));
				}

			}
			return line;
		}

		@Override
		public void provideVirtualRoot(String virtualRoot) {
			contextRoot = virtualRoot;
		}

		@Override
		public void provide(ProviderContext context) {
			this.ctx = context;
		}

		@Override
		public void provide(ConnectiveHTTPServer server) {
			this.server = server;
		}

	}

	@Override
	public String instructionName() {
		return "rewrite";
	}

	@Override
	public int maximalArguments() {
		return -1;
	}

	@Override
	public int minimalArguments() {
		return 3;
	}

	@Override
	public void run(String[] arguments, ProviderContextFactory factory) throws Exception {
		if (arguments[1].startsWith("task:")
				&& ConnectiveConfiguration.getInstance().tasks.containsKey(arguments[1].substring(5))) {
			String task = arguments[1].substring(5);
			factory.addVirtualFile(new RewriteVirtualFile(arguments, task, arguments[0]));
		} else {
			throw new IOException("Invalid rewrite instruction! Expected: '<matcher-string>' 'task:<task>' '<regex>'");
		}
	}

}
