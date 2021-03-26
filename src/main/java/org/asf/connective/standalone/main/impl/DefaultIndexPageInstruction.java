package org.asf.connective.standalone.main.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.asf.connective.standalone.ContextFileInstruction;
import org.asf.connective.standalone.main.ConnectiveStandalone;
import org.asf.rats.http.ProviderContextFactory;
import org.asf.rats.http.providers.IndexPageProvider;

public class DefaultIndexPageInstruction implements ContextFileInstruction {

	@Override
	public String instructionName() {
		return "defaultindexpage";
	}

	@Override
	public int maximalArguments() {
		return 1;
	}

	@Override
	public void run(String[] arguments, ProviderContextFactory factory)
			throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		if (arguments[0].equals("null") || arguments[0].startsWith("class:")) {
			if (arguments[0].equals("null")) {
				factory.setDefaultIndexPage(null);
				factory.setOption(ProviderContextFactory.OPTION_DISABLE_DEFAULT_INDEX);
			} else {
				factory.setDefaultIndexPage(
						(IndexPageProvider) Class
								.forName(arguments[0].substring("class:".length()), false,
										ConnectiveStandalone.getInstance().getClassLoader())
								.getConstructor().newInstance());
			}
		} else {
			throw new IOException("Invalid index page! Expected either 'null' or 'class:<index-page-class>'");
		}
	}

}
