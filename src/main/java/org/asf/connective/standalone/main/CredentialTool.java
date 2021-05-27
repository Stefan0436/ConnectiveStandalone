package org.asf.connective.standalone.main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Stream;

public class CredentialTool {

	public static interface ICredToolTarget {
		public void usageFooter();

		public static class User {
			public String name;
			public String group;
		}

		public User[] lsUsers();

		public boolean deleteUser(String group, String username);

		public void setUser(String group, String username, char[] password) throws IOException;
	}

	public static class DefaultTarget implements ICredToolTarget {

		@Override
		public void usageFooter() {
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
		}

		@Override
		public User[] lsUsers() {
			ArrayList<User> users = new ArrayList<User>();
			File credFolder = new File("credentials");
			if (credFolder.exists()) {
				for (File f : credFolder.listFiles((file) -> !file.isDirectory() && file.getName().endsWith(".cred")
						&& file.getName().startsWith("gr."))) {

					String userdata = f.getName().substring(0, f.getName().lastIndexOf(".cred")).substring(3);
					User user = new User();
					user.group = userdata.substring(0, userdata.indexOf("."));
					user.name = userdata.substring(userdata.indexOf(".") + 1);
					users.add(user);
				}
			}
			return users.toArray(t -> new User[t]);
		}

		@Override
		public boolean deleteUser(String group, String username) {
			File cred = new File("credentials", "gr." + group + "." + username + ".cred");
			if (!cred.exists()) {
				return false;
			} else {
				cred.delete();
				return true;
			}
		}

		@Override
		public void setUser(String group, String username, char[] password) throws IOException {
			File credFolder = new File("credentials");
			if (!credFolder.exists())
				credFolder.mkdirs();

			File userCred = new File(credFolder, "gr." + group + "." + username + ".cred");
			if (userCred.exists()) {
				userCred.delete();
			}

			Files.write(userCred.toPath(), Base64.getEncoder().encode(String.valueOf(password).getBytes()));
		}

	}

	private static ICredToolTarget target = new DefaultTarget();

	public static void main(String[] args)
			throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		File targetInfo = new File(".credtool.target");

		if (targetInfo.exists()) {
			ArrayList<String> info = new ArrayList<String>(Files.readAllLines(targetInfo.toPath()));
			String type = info.get(0);
			info.remove(0);
			ArrayList<URL> urls = new ArrayList<URL>();
			for (String line : info) {
				if (!line.isEmpty() && !line.startsWith("#")) {
					urls.add(new File(line).toURI().toURL());
				}
			}
			URLClassLoader loader = new URLClassLoader(urls.toArray(t -> new URL[t]));
			target = (ICredToolTarget) loader.loadClass(type).getConstructor().newInstance();
		}

		if (args.length < 2 && !(args.length >= 1 && (args[0].equals("/ls") || args[0].equals("/rmuser")))) {
			usage();
		} else {
			String group = args[0];
			String username = (args.length >= 2 ? args[1] : "");
			if (group.equals("/ls")) {
				System.out.println("List of users:");
				Stream.of(target.lsUsers()).sorted((t1, t2) -> {
					return -t1.group.compareTo(t2.group);
				}).forEach(user -> System.out.println(" - " + user.name + " (" + user.group + ")"));
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
						System.exit(1);
						return;
					}
					if (username.isEmpty()) {
						System.err.println("Username empty, exiting.");
						System.exit(1);
						return;
					}

					if (!username.matches("^[A-Za-z0-9\\-@. ']+$")) {
						System.err.println(
								"Use of illegal characters detected, only alphanumeric characters are allowed. (with exception to the '.', '@', ' ', '-' and \"'\" which are also allowed)");
						System.exit(1);
						return;
					}

					if (!group.matches("^[A-Za-z0-9]+$")) {
						System.err.println(
								"Use of illegal characters detected, only alphanumeric characters are allowed. (the group does not allow the '.')");
						System.exit(1);
						return;
					}

					if (!target.deleteUser(group, username)) {
						System.err.println("User not found.");
						System.exit(1);
					} else {
						System.exit(0);
					}
				}

				return;
			}
			if (username.isEmpty()) {
				System.err.println("Username empty, exiting.");
				System.exit(1);
				return;
			}
			if (username.isEmpty()) {
				System.err.println("Username empty, exiting.");
				System.exit(1);
				return;
			}

			if (!username.matches("^[A-Za-z0-9\\-@. ']+$")) {
				System.err.println(
						"Use of illegal characters detected, only alphanumeric characters are allowed. (with exception to the '.', '@', ' ', '-' and \"'\" which are also allowed)");
				System.exit(1);
				return;
			}

			if (!group.matches("^[A-Za-z0-9]+$")) {
				System.err.println(
						"Use of illegal characters detected, only alphanumeric characters are allowed. (the group does not allow the '.')");
				System.exit(1);
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
					System.exit(1);
					return;
				}
				for (int i = 0; i < validatepassword.length; i++) {
					validatepassword[i] = 0;
				}
			}

			try {
				target.setUser(group, username, password);
				for (int i = 0; i < password.length; i++) {
					password[i] = 0;
				}
				System.exit(0);
				return;
			} catch (IOException e) {
				System.err.println("Failed to write user file.");
				System.exit(1);
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
		target.usageFooter();
		System.err.println(
				"NOTE: the group only allows for use of alphanumeric characters, however the username also allows for");
		System.err.println("      the use of the following characters: '.', '@', ' ', '-' and \"'\"");
		System.err.println("");
		System.err.println(
				"----------------------------------------------------------------------------------------------------");
		System.err.println("Copyright(c) 2021 AerialWorks Software Foundation.");
		System.err.println("Most of the Connective server is licensed LGPL 3.");
		System.exit(1);
	}

}
