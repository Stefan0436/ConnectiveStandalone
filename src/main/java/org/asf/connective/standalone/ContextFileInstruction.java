package org.asf.connective.standalone;

import org.asf.rats.http.ProviderContextFactory;

/**
 * 
 * ContextFile instruction interface - creates contextfile instructions for the
 * server.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface ContextFileInstruction {
	public String instructionName();

	public default int minimalArguments() {
		return maximalArguments();
	}

	public int maximalArguments();

	public void run(String[] arguments, ProviderContextFactory factory) throws Exception;
}
