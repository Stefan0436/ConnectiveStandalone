package org.example.examplemodule.examples.basicfile;

import java.io.IOException;
import java.util.Base64;

import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;
import org.asf.rats.IAuthenticationProvider;
import org.asf.rats.Memory;
import org.asf.rats.http.providers.IFileRestrictionProvider;

// Simple restriction preventing access to /tester2/ unless the
// user is authenticated using a username and password in the 'simple' group
public class RestrictionAuthenticationExample implements IFileRestrictionProvider {

	private String group = "simple"; // Our group

	// IMPORTANT: the contextfile instructions depend on an empty constructor for
	// instantiation.

	@Override
	public IFileRestrictionProvider newInstance() {

		//
		// If match returns true, checkRestriction is run its own instance.
		// If you are using a dynamic group system, pass it down here.
		//
		// IMPORTANT: this method and check() run in the main instance!
		//

		return new RestrictionAuthenticationExample();
	}

	@Override
	public boolean match(HttpRequest request, String file) {

		//
		// Important notice:
		// This method runs in the main instance.
		//
		//
		// This restriction only applies to the 'tester2' folder
		// We use toLowerCase() to prevent windows servers from being exploited.
		//
		// Windows uses case-insensitive paths, which means windows servers can
		// have /tester restricted, but when a user requests /Tester2, they get around
		// the restriction.
		//
		// UNIX is case-sensitive, when requesting /Tester2, they get a different folder
		// than when they request /tester2.
		//

		return file.toLowerCase().startsWith("/tester2/") || file.toLowerCase().equals("/tester2");
	}

	@Override
	public boolean checkRestriction(String file, HttpRequest request) {
		if (!request.headers.containsKey("Authorization")) {

			//
			// Returns a 401 if the Authorization header is not present.
			// The code in getResponseCode decides whether it is a 403 or 401.
			//

			return false;

		} else {

			// Read the header
			String header = request.headers.get("Authorization");

			// Parse it
			String type = header.substring(0, header.indexOf(" "));
			String cred = header.substring(header.indexOf(" ") + 1);

			if (type.equals("Basic")) {
				// Decode the credentials and check authorization
				cred = new String(Base64.getDecoder().decode(cred));
				String username = cred.substring(0, cred.indexOf(":"));
				String password = cred.substring(cred.indexOf(":") + 1);

				try {
					// The connective.standard.authprovider provides the IAuthenticationProvider
					// used by
					// the running ConnectiveHTTP implementation.
					if (Memory.getInstance().get("connective.standard.authprovider")
							.getValue(IAuthenticationProvider.class)
							.authenticate(group, username, password.toCharArray())) {

						password = null;
						return true;
					} else {
						password = null;
						return false;
					}
				} catch (IOException e) {
					password = null;
					return false;
				}
			} else {
				return false;
			}
		}

	}

	@Override
	public String getResponseMessage(HttpRequest request) {
		if (request.headers.containsKey("Authorization"))
			return "Access denied";
		else
			return "Authorization required";
	}

	@Override
	public int getResponseCode(HttpRequest request) {
        return 401;
	}

	@Override
	public void rewriteResponse(HttpRequest request, HttpResponse response) {
		if (response.status == 401 || response.status == 403) {
			response.headers.put("WWW-Authenticate", "Basic realm=" + group);
		}
	}

}
