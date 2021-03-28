package org.example.examplemodule.examples.basicfile;

import org.asf.rats.HttpRequest;
import org.asf.rats.http.providers.IFileRestrictionProvider;

// Example restriction, blocks access to /tester with custom status if a header is not present.
public class RestrictionExample implements IFileRestrictionProvider {

	@Override
	public boolean checkRestriction(String path, HttpRequest request) {
		if (!request.headers.containsKey("My-Own-Header") && (path.startsWith("/tester/") || path.equals("/tester"))) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String getResponseMessage(HttpRequest request) {
		return "Missing header, access denied.";
	}

}
