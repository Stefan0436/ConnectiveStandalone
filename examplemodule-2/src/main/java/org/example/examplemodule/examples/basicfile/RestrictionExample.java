package org.example.examplemodule.examples.basicfile;

import org.asf.rats.HttpRequest;
import org.asf.rats.http.providers.IFileRestrictionProvider;

// Example restriction, blocks access to /tester with custom status if a header is not present.
public class RestrictionExample implements IFileRestrictionProvider {

	@Override
	public boolean match(HttpRequest request, String file) {

		//
		// This restriction only applies to the 'tester' folder
		// We use toLowerCase() to prevent windows servers from being exploited.
		//
		// Windows uses case-insensitive paths, which means windows servers can
		// have /tester restricted, but when a user requests /Tester, they get around
		// the restriction.
		//
		// UNIX is case-sensitive, when requesting /Tester, they get a different folder
		// than when they request /tester.
		//

		return file.toLowerCase().startsWith("/tester/") || file.toLowerCase().equals("/tester");
	}

	@Override
	public boolean checkRestriction(String file, HttpRequest request) {
		if (!request.headers.containsKey("My-Own-Header")) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String getResponseMessage(HttpRequest request) {
		return "Missing header, access denied.";
	}

	@Override
	public IFileRestrictionProvider newInstance() {
		return new RestrictionExample();
	}

}
