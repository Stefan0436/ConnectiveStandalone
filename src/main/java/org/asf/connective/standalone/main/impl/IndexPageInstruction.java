package org.asf.connective.standalone.main.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.asf.connective.standalone.ContextFileInstruction;
import org.asf.connective.standalone.main.ConnectiveStandalone;
import org.asf.rats.http.ProviderContextFactory;
import org.asf.rats.http.providers.IndexPageProvider;

public class IndexPageInstruction implements ContextFileInstruction {

	@Override
	public String instructionName() {
		return "indexpage";
	}

	@Override
	public int maximalArguments() {
		return 2;
	}

	@Override
	public void run(String[] arguments, ProviderContextFactory factory)
			throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		if (arguments[1].startsWith("class:")) {
			factory.addIndexPage((!arguments[0].startsWith("/") ? "/" + arguments[0] : arguments[0]),
					(IndexPageProvider) Class
							.forName(arguments[1].substring("class:".length()), false,
									ConnectiveStandalone.getInstance().getClassLoader())
							.getConstructor().newInstance());
		} else {
			throw new IOException("Invalid index page! Expected '<path>' 'class:<index-page-class>'");
		}
	}

}
