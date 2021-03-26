package org.asf.connective.standalone.main.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.asf.connective.standalone.ContextFileInstruction;
import org.asf.connective.standalone.main.ConnectiveStandalone;
import org.asf.rats.http.ProviderContextFactory;
import org.asf.rats.http.providers.IFileExtensionProvider;

public class ExtensionInstruction implements ContextFileInstruction {

	@Override
	public String instructionName() {
		return "extension";
	}

	@Override
	public int maximalArguments() {
		return 1;
	}

	@Override
	public void run(String[] arguments, ProviderContextFactory factory)
			throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		if (arguments[0].startsWith("class:")) {
			factory.addExtension(
					(IFileExtensionProvider) Class
							.forName(arguments[0].substring("class:".length()), false,
									ConnectiveStandalone.getInstance().getClassLoader())
							.getConstructor().newInstance());
		} else {
			throw new IOException("Invalid index page! Expected: 'class:<extension>'");
		}
	}

}
