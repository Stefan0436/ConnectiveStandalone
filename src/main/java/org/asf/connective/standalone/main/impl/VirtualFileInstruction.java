package org.asf.connective.standalone.main.impl;

import java.io.IOException;

import org.asf.connective.standalone.ContextFileInstruction;
import org.asf.connective.standalone.main.ConnectiveStandalone;
import org.asf.rats.http.ProviderContextFactory;
import org.asf.rats.http.providers.IVirtualFileProvider;

public class VirtualFileInstruction implements ContextFileInstruction {

	@Override
	public String instructionName() {
		return "virtualfile";
	}

	@Override
	public int maximalArguments() {
		return 1;
	}

	@Override
	public void run(String[] arguments, ProviderContextFactory factory) throws Exception {
		if (arguments[0].startsWith("class:")) {
			factory.addVirtualFile(
					(IVirtualFileProvider) Class
							.forName(arguments[0].substring("class:".length()), false,
									ConnectiveStandalone.getInstance().getClassLoader())
							.getConstructor().newInstance());
		} else {
			throw new IOException("Invalid virtual file provider! Expected either 'class:<index-page-class>'");
		}
	}

}
