package org.example.examplemodule.examples.basicfile;

import org.asf.rats.HttpRequest;
import org.asf.rats.http.providers.IFileAlias;

// Simple alias rewriting paths starting with /example/test/
// if you request /example/test/file, you get /tester/test/file
public class AliasExample implements IFileAlias {

	// IMPORTANT: the contextfile instructions depend on an empty constructor for instantiation.

	@Override
	public boolean match(HttpRequest request, String input) {

		//
		// Important notice: you should not assign or be dependent on changing fields
		// here, you should only do that in rewrite which runs in its own instance.
		//

		return input.equals("/test");
	}

	@Override
	public String rewrite(HttpRequest request, String input) {
		return "/example.html";
	}

	@Override
	public IFileAlias newInstance() {
		return new AliasExample();
	}

}
