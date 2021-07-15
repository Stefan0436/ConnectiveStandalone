package org.asf.connective.standalone.main.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.asf.connective.standalone.ContextFileInstruction;
import org.asf.connective.standalone.main.ConnectiveStandalone;
import org.asf.rats.http.ProviderContextFactory;
import org.asf.rats.http.providers.IDocumentPostProcessor;

public class DocPostProcessorInstruction implements ContextFileInstruction {

	@Override
	public String instructionName() {
		return "post-processor";
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
			factory.addDocumentPostProcessor(
					(IDocumentPostProcessor) Class
							.forName(arguments[0].substring("class:".length()), false,
									ConnectiveStandalone.getInstance().getClassLoader())
							.getConstructor().newInstance());
		} else {
			throw new IOException("Invalid document post-processor! Expected: 'class:<extension>'");
		}
	}

}
