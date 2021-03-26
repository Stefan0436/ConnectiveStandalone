package org.asf.connective.standalone.main.impl;

import org.asf.connective.standalone.ContextFileInstruction;
import org.asf.rats.http.ProviderContextFactory;

public class VirtualRootInstruction implements ContextFileInstruction {

	@Override
	public String instructionName() {
		return "virtualroot";
	}

	@Override
	public int maximalArguments() {
		return 1;
	}

	@Override
	public void run(String[] arguments, ProviderContextFactory factory) {
		factory.setRootFile(arguments[0]);
	}

}
