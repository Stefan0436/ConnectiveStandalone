package org.asf.connective.standalone.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;

public class CredentialTool {

	public static void main(String[] args) {
		if (args.length < 2 && !(args.length >= 1 && (args[0].equals("/ls") || args[0].equals("/rmuser")))) {
			usage();
		} else {
			String group = args[0];
			String username = (args.length >= 2 ? args[1] : "");
			if (group.equals("/ls")) {
				File credFolder = new File("credentials");
				System.out.println("List of users:");
				if (credFolder.exists()) {
					for (File f : credFolder.listFiles((file) -> !file.isDirectory() && file.getName().endsWith(".cred")
							&& file.getName().startsWith("gr."))) {

						String userdata = f.getName().substring(0, f.getName().lastIndexOf(".cred")).substring(3);
						group = userdata.substring(0, userdata.indexOf("."));
						username = userdata.substring(userdata.indexOf(".") + 1);
						System.out.println(" - " + username + " (" + group + ")");
					}
				}
				return;
			}
			if (group.equals("/rmuser")) {
				if (args.length < 3) {
					usage();
				} else {
					group = args[1];
					username = args[2];
					if (group.isEmpty()) {
						System.err.println("Group empty, exiting.");
						System.exit(-1);
						return;
					}
					if (username.isEmpty()) {
						System.err.println("Username empty, exiting.");
						System.exit(-1);
						return;
					}

					if (!username.matches("^[A-Za-z0-9@.]+$")) {
						System.err.println(
								"Use of illegal characters detected, only alphanumeric characters are allowed. (with exception to the '.' and '@' which are also allowed)");
						System.exit(-1);
						return;
					}

					if (!group.matches("^[A-Za-z0-9]+$")) {
						System.err.println(
								"Use of illegal characters detected, only alphanumeric characters are allowed. (the group does not allow the '.')");
						System.exit(-1);
						return;
					}

					File cred = new File("credentials", "gr." + group + "." + username + ".cred");
					if (!cred.exists()) {
						System.err.println("User not found.");
						System.exit(-1);
					} else {
						cred.delete();
						System.exit(0);
					}
				}

				return;
			}
			if (username.isEmpty()) {
				System.err.println("Username empty, exiting.");
				System.exit(-1);
				return;
			}
			if (username.isEmpty()) {
				System.err.println("Username empty, exiting.");
				System.exit(-1);
				return;
			}

			if (!username.matches("^[A-Za-z0-9@.]+$")) {
				System.err.println(
						"Use of illegal characters detected, only alphanumeric characters are allowed. (with exception to the '.' and '@' which are also allowed)");
				System.exit(-1);
				return;
			}

			if (!group.matches("^[A-Za-z0-9]+$")) {
				System.err.println(
						"Use of illegal characters detected, only alphanumeric characters are allowed. (the group does not allow the '.')");
				System.exit(-1);
				return;
			}

			char[] password = new char[0];
			if (args.length >= 3) {
				password = args[2].toCharArray();
			} else {
				System.out.print(username + "'s Password: ");
				password = System.console().readPassword();
				System.out.print("Repeat " + username + "'s password: ");
				char[] validatepassword = System.console().readPassword();
				if (!Arrays.equals(password, validatepassword)) {
					for (int i = 0; i < validatepassword.length; i++) {
						validatepassword[i] = 0;
					}
					for (int i = 0; i < password.length; i++) {
						password[i] = 0;
					}
					System.err.println("Passwords do not match.");
					System.exit(-1);
					return;
				}
				for (int i = 0; i < validatepassword.length; i++) {
					validatepassword[i] = 0;
				}
			}
			File credFolder = new File("credentials");
			if (!credFolder.exists())
				credFolder.mkdirs();

			File userCred = new File(credFolder, "gr." + group + "." + username + ".cred");
			if (userCred.exists()) {
				userCred.delete();
			}

			try {
				Files.write(userCred.toPath(), Base64.getEncoder().encode(String.valueOf(password).getBytes()));

				for (int i = 0; i < password.length; i++) {
					password[i] = 0;
				}
				System.exit(0);
				return;
			} catch (IOException e) {
				System.err.println("Failed to write user file.");
				System.exit(-1);
				return;
			}
		}
	}

	private static void usage() {
		System.err.println("ASF Connective Credential Tool - Post Request Credential Storage");
		System.err.println(
				"----------------------------------------------------------------------------------------------------");
		System.err.println("");
		System.err.println("Usage:");
		System.err.println("connective credtool \"<group>\" \"<username>\" \"[password]\"");
		System.err.println("");
		System.err.println("User deletion:");
		System.err.println("connective credtool /rmuser \"<group>\" \"<username>\"");
		System.err.println("");
		System.err.println("User listing:");
		System.err.println("connective credtool /ls");
		System.err.println("");
		System.err.println("");
		System.err.println("Files are stored in the current directory's credentials folder.");
		System.err.println("Format is basic; base64 encoded credential exchange.");
		System.err.println("");
		System.err.println("WARNING!");
		System.err.println("User files are NOT encrypted!");
		System.err.println(
				"User format: base64 password string - keep them safe and AWAY from the server file provider context.");
		System.err.println("");
		System.err.println("");
		System.err.println(
				"NOTE: the group only allows for use of alphanumeric characters, so does the username, though");
		System.err.println("      the username also allows for use of the following characters: '@', '.'");
		System.err.println("");
		System.err.println(
				"----------------------------------------------------------------------------------------------------");
		System.err.println("Copyright(c) 2021 AerialWorks Software Foundation.");
		System.err.println("Most of the Connective server is LGPL 3 licensed.");
		System.exit(-1);
	}

}
