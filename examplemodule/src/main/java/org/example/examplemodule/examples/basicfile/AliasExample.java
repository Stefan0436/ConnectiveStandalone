package org.example.examplemodule.examples.basicfile;

import org.asf.rats.HttpRequest;
import org.asf.rats.http.providers.IFileAlias;

// Simple alias rewriting paths starting with /example/test/
// if you request /example/test/file
// you get /tester/test/file
public class AliasExample implements IFileAlias {

	@Override
	public boolean match(HttpRequest arg0, String arg1) {
		return arg1.equals("/test");
	}

	@Override
	public String rewrite(HttpRequest arg0, String arg1) {
		return "/example.html";
	}

}
