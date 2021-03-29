package org.asf.connective.standalone.main.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asf.connective.standalone.ContextFileInstruction;
import org.asf.connective.standalone.main.ConnectiveStandalone;
import org.asf.rats.HttpRequest;
import org.asf.rats.http.ProviderContextFactory;
import org.asf.rats.http.providers.IFileAlias;

public class AliasInstruction implements ContextFileInstruction {

	public class DefaultFileAlias implements IFileAlias {

		private String[] arguments;

		public DefaultFileAlias(String[] args) {
			this.arguments = args;
		}

		@Override
		public boolean match(HttpRequest request, String input) {
			String matchTest = arguments[0];
			if (input.endsWith("/"))
				input = input.substring(0, input.length() - 1);

			matchTest = matchTest.replace("%input%", input);
			matchTest = matchTest.replace("%request.http.version%", request.version);
			matchTest = matchTest.replace("%request.http.method%", request.method);
			matchTest = matchTest.replace("%request.http.query%", (request.query != null ? request.query : ""));
			matchTest = matchTest.replace("%request.http.path%", request.path);

			for (String header : request.headers.keySet()) {
				matchTest = matchTest.replace("%request.headers." + header.toLowerCase() + "%",
						request.headers.get(header));
			}

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

				if (!match)
					return false;
			}
			return match;
		}

		@Override
		public String rewrite(HttpRequest request, String input) {
			return arguments[1];
		}

		@Override
		public IFileAlias newInstance() {
			return new DefaultFileAlias(arguments);
		}

	}

	@Override
	public String instructionName() {
		return "alias";
	}

	@Override
	public int maximalArguments() {
		return -1;
	}

	@Override
	public int minimalArguments() {
		return 1;
	}

	@Override
	public void run(String[] arguments, ProviderContextFactory factory) throws Exception {
		if (arguments[0].startsWith("class:")) {
			factory.addAlias(
					(IFileAlias) Class
							.forName(arguments[0].substring("class:".length()), false,
									ConnectiveStandalone.getInstance().getClassLoader())
							.getConstructor().newInstance());
		} else if (arguments.length >= 3) {
			factory.addAlias(new DefaultFileAlias(arguments));
		} else {
			throw new Exception("Invalid format! Expected either 'class:<class>' or 'test' 'location' 'matcher'");
		}
	}

}
