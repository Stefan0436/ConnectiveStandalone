package org.example.examplemodule;

import java.io.IOException;

import org.asf.rats.http.IAutoContextModificationProvider;
import org.asf.rats.http.ProviderContextFactory;

// This class allows RaTs! to use the module components, it allows
// for modifying any context created by the IAutoContextBuilders used by RaTs.
public class ExampleModificationProvider implements IAutoContextModificationProvider {

	@Override
	public void accept(ProviderContextFactory arg0) {
		if (!ExampleModificationManager.hasBeenPrepared()) {
			try {
				ExampleModificationManager.prepareModifications();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		// Apply modifications
		ExampleModificationManager.appy(arg0);
	}

}
