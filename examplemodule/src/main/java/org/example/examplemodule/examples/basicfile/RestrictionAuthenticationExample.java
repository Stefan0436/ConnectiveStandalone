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
//
// NOTE: Restrictions are single-instannce modifications, please keep that in mind.
public class RestrictionAuthenticationExample implements IFileRestrictionProvider {

	private String group = "simple"; // Our group

	@Override
	public boolean checkRestriction(String path, HttpRequest request) {
		if (!request.headers.containsKey("Authorization")
				&& (path.startsWith("/tester2/") || path.equals("/tester2"))) {
			return false;
		} else if (!path.startsWith("/tester2/") && !path.equals("/tester2")) {
			return true;
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
					// The connective.standard.authprovider provides the IAuthenticationProvider used by
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
		if (request.headers.containsKey("Authorization"))
			return 403;
		else
			return 401;
	}

	@Override
	public void rewriteResponse(HttpRequest request, HttpResponse response) {
		if (response.status == 401 || response.status == 403) {
			response.headers.put("WWW-Authenticate", "Basic realm=" + group);
		}
	}

}
