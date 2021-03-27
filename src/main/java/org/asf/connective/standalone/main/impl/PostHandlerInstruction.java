package org.asf.connective.standalone.main.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
import org.asf.connective.standalone.ContextFileInstruction;
import org.asf.connective.standalone.main.ConnectiveStandalone;

import org.asf.rats.HttpRequest;
import org.asf.rats.http.ProviderContext;
import org.asf.rats.http.ProviderContextFactory;
import org.asf.rats.http.providers.FilePostHandler;
import org.asf.rats.http.providers.IContextProviderExtension;
import org.asf.rats.http.providers.IPathProviderExtension;

public class PostHandlerInstruction implements ContextFileInstruction {

	public static class DefaultPostHandler extends FilePostHandler
			implements IContextProviderExtension, IPathProviderExtension {

		private String path = null;

		private ProviderContext context;
		private String affectedPath = "";
		private String group = "";
		private Logger logger = LogManager.getLogger("POST-PROCESSING");

		private String serverDir = System.getProperty("rats.config.dir") == null ? "."
				: System.getProperty("rats.config.dir");

		public DefaultPostHandler(String group, String path, ProviderContext context) {
			this.group = group;

			if (!path.startsWith("/"))
				path = "/" + path;
			if (!path.endsWith("/"))
				path += "/";

			this.affectedPath = path;
			this.context = context;
		}

		@Override
		protected FilePostHandler newInstance() {
			return new DefaultPostHandler(group, affectedPath, context);
		}

		@Override
		public boolean match(HttpRequest request, String path) {
			if (!path.endsWith("/"))
				path += "/";

			return path.toLowerCase().startsWith(affectedPath.toLowerCase());
		}

		@Override
		public void process(String contentType, Socket client) {
			if (getHeader("Authorization") != null) {
				String header = getHeader("Authorization");
				String type = header.substring(0, header.indexOf(" "));
				String cred = header.substring(header.indexOf(" ") + 1);

				if (type.equals("Basic")) {
					cred = new String(Base64.getDecoder().decode(cred));
					String username = cred.substring(0, cred.indexOf(":"));
					String password = cred.substring(cred.indexOf(":") + 1);

					if (username.matches("^[A-Za-z0-9@.]+$")) {
						File userFile = new File(new File(serverDir, "credentials"),
								"gr." + group + "." + username + ".cred");
						if (!userFile.exists()) {
							this.setResponseCode(403);
							this.setResponseMessage("Access denied");
							this.setBody("text/html", null);
						} else {
							try {
								String userPass = new String(
										Base64.getDecoder().decode(Files.readAllBytes(userFile.toPath())));

								if (userPass.equals(password)) {
									password = null;

									File file = new File(new File(serverDir, context.getSourceDirectory()), path);
									if (!file.getParentFile().exists()) {
										file.getParentFile().mkdirs();
									}

									FileOutputStream strm = new FileOutputStream(file);
									if (getHeader("Content-Length") != null) {
										long length = Long.valueOf(getHeader("Content-Length"));
										int tr = 0;
										for (long i = 0; i < length; i += tr) {
											tr = Integer.MAX_VALUE / 1000;
											if ((length - (long) i) < tr) {
												tr = getBodyStream().available();
												if (tr == 0) {
													strm.write(getBodyStream().read());
													i += 1;
												}
												tr = getBodyStream().available();
											}
											strm.write(getBodyStream().readNBytes(tr));
										}
									} else {
										getBodyStream().transferTo(strm);
									}
									strm.close();

									this.setResponseCode(204);
									this.setResponseMessage("No content");
									this.setBody("");
								} else {
									this.setResponseCode(403);
									this.setResponseMessage("Access denied");
									this.setBody("text/html", null);
								}

								userPass = null;
							} catch (IOException e) {
								this.setResponseCode(503);
								this.setResponseMessage("Internal server error");
								this.setBody("text/html", null);
								logger.error(MarkerManager.getMarker(group.toUpperCase()),
										"Failed to process user file: " + userFile, e);
							}
						}
					} else {
						this.setResponseCode(403);
						this.setResponseMessage("Access denied");
						this.setBody("text/html", null);
					}
					password = null;
				} else {
					this.setResponseCode(403);
					this.setResponseMessage("Access denied");
					this.setBody("text/html", null);
				}
			} else {
				this.setResponseHeader("WWW-Authenticate", "Basic realm=" + group);

				this.setResponseCode(401);
				this.setResponseMessage("Authorization required");
				this.setBody("");
			}
		}

		@Override
		public void provide(ProviderContext context) {
			this.context = context;
		}

		@Override
		public void provide(String path) {
			this.path = path;
		}

	}

	@Override
	public String instructionName() {
		return "posthandler";
	}

	@Override
	public int maximalArguments() {
		return 2;
	}

	@Override
	public int minimalArguments() {
		return 1;
	}

	@Override
	public void run(String[] arguments, ProviderContextFactory factory)
			throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		if (arguments[0].startsWith("class:")) {
			factory.addPostHandler(
					(FilePostHandler) Class
							.forName(arguments[0].substring("class:".length()), false,
									ConnectiveStandalone.getInstance().getClassLoader())
							.getConstructor().newInstance());
		} else if (arguments.length == 2) {
			factory.addPostHandler(new DefaultPostHandler(arguments[1], arguments[0], null));
		} else {
			throw new IOException(
					"Invalid format! Expected either: 'folder-path' 'allowed-group' or: 'class:<post-handler>'");
		}
	}

}
