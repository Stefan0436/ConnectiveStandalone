package org.asf.connective.standalone.main.impl.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;

import org.asf.rats.IAuthenticationProvider;
import org.asf.rats.Memory;

/**
 * 
 * HTTP authentication provider.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class ConnectiveAuthProvider implements IAuthenticationProvider {

	private String serverDir = System.getProperty("rats.config.dir") == null ? "."
			: System.getProperty("rats.config.dir");

	public static void assign() {
		Memory.getInstance().getOrCreate("connective.standard.authprovider").assign(new ConnectiveAuthProvider());
	}

	@Override
	public boolean authenticate(String group, String username, char[] password) throws IOException {
		if (!username.matches("^[A-Za-z0-9.@ ']+$") || !group.matches("^[A-Za-z0-9]+$"))
			return false;

		File authFile = new File(new File(serverDir, "credentials"), "gr." + group + "." + username + ".cred");
		if (!authFile.exists())
			return false;
		else {
			char[] userPass = new String(Base64.getDecoder().decode(Files.readAllBytes(authFile.toPath())))
					.toCharArray();
			boolean match = Arrays.equals(userPass, password);
			for (int i = 0; i < userPass.length; i++)
				userPass[i] = 0;

			return match;
		}
	}

}
