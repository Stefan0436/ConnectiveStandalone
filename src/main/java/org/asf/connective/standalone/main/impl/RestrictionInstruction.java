package org.asf.connective.standalone.main.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asf.connective.standalone.ContextFileInstruction;
import org.asf.connective.standalone.main.ConnectiveStandalone;
import org.asf.rats.HttpRequest;
import org.asf.rats.http.ProviderContextFactory;
import org.asf.rats.http.providers.IFileRestrictionProvider;

public class RestrictionInstruction implements ContextFileInstruction {

	@Override
	public String instructionName() {
		return "restriction";
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
			factory.addRestriction(
					(IFileRestrictionProvider) Class
							.forName(arguments[0].substring("class:".length()), false,
									ConnectiveStandalone.getInstance().getClassLoader())
							.getConstructor().newInstance());
		} else if (arguments.length >= 2) {
			factory.addRestriction(new IFileRestrictionProvider() {

				@Override
				public boolean checkRestriction(String file, HttpRequest request) {
					String matchTest = arguments[0];
					if (file.endsWith("/"))
						file = file.substring(0, file.length() - 1);

					if (!file.startsWith("/"))
						file = "/" + file;

					matchTest = matchTest.replace("%file%", file);
					matchTest = matchTest.replace("%request.http.version%", request.version);
					matchTest = matchTest.replace("%request.http.method%", request.method);
					matchTest = matchTest.replace("%request.http.query%", (request.query != null ? request.query : ""));
					matchTest = matchTest.replace("%request.http.path%", request.path);

					for (String header : request.headers.keySet()) {
						matchTest = matchTest.replace("%request.headers." + header.toLowerCase() + "%",
								request.headers.get(header));
					}

					boolean match = true;
					for (int i = 1; i < arguments.length; i++) {
						boolean not = false;
						String matcher = arguments[i];
						if (matcher.startsWith("%NOT%")) {
							matcher = matcher.substring(5);
							not = true;
						}

						Pattern pattern = Pattern.compile(matcher);
						Matcher matcherInst = pattern.matcher(matchTest);
						boolean b = matcherInst.find();
						match = (!not ? !b : b);

						if (match)
							return true;
					}
					return match;
				}

			});
		} else {
			throw new Exception("Invalid format! Expected either 'class:<class>' or 'test' 'matcher'");
		}
	}

}
