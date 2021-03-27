package org.example.examplemodule;

import java.io.IOException;
import java.util.HashMap;

import org.asf.cyan.api.common.CYAN_COMPONENT;
import org.asf.rats.Memory;
import org.asf.rats.ModuleBasedConfiguration;

@CYAN_COMPONENT
public class ExampleModule extends ExampleModificationManager {

	// Our configuration properties, also contains the defaults
	private static HashMap<String, String> ourOwnProperties = new HashMap<String, String>();

	// Temporary boolean to check if the module config is new or not.
	private static boolean hasConfigChanged = false;

	@Override
	// Module id for the modification manager and configuration
	protected String moduleId() {
		return "examplemodule";
	}

	static {
		// Lets set our default config properties
		// These values are added if not done so before.

		// creates a key named 'world' with 'hello' as default value.
		ourOwnProperties.put("world", "hello");

		// creates a key named 'tester' with 'test' as default value.
		ourOwnProperties.put("tester", "test");
	}

	// Called on module loading
	protected static void initComponent() {
		// Print two info messages
		info("Hello World!");
		info("My own module!");

		// Assigns the modification manager
		assign(new ExampleModule());

		// Starts our module
		ExampleModule.start();
	}

	@Override
	protected void startModule() {
		// Lets create a bootstrap.call handler, so we can properly generate our
		// configuration properties.

		// Get the default memory instance
		Memory memory = Memory.getInstance();

		// Get (or create) the 'bootstrap.call' entry
		Memory bootstrapCall = memory.getOrCreate("bootstrap.call");

		// Add our handler, this adds a Runnable to the value set of the entry
		bootstrapCall.<Runnable>append(() -> {
			info("Startup call, lets load our configuration properties...");
			readConfig();

			String world = ourOwnProperties.get("world");
			String tester = ourOwnProperties.get("tester");

			info(world + " world");
			info(tester + " tester");
		});

		// Or... you can do it all in one go:
//		Memory.getInstance().getOrCreate("bootstrap.call").<Runnable>append(() -> readConfig());
	}

	private void readConfig() {
		// Get the server configuration as ModuleBasedConfiguration from the
		// 'memory.modules.shared.config' entry.
		ModuleBasedConfiguration<?> config = Memory.getInstance().get("memory.modules.shared.config")
				.getValue(ModuleBasedConfiguration.class);

		HashMap<String, String> ourConfigCategory = config.modules.getOrDefault(moduleId(),
				new HashMap<String, String>());

		// Check for missing configuration entries, add those that are missing, load the
		// ones that are present.
		if (!config.modules.containsKey(moduleId())) {
			ourConfigCategory.putAll(ourOwnProperties);
			hasConfigChanged = true;

		} else {
			ourOwnProperties.forEach((key, value) -> {
				if (!ourConfigCategory.containsKey(key)) {
					hasConfigChanged = true;
					ourConfigCategory.put(key, value);
				} else {
					ourOwnProperties.put(key, ourConfigCategory.get(key));
				}

			});
		}

		// Save our category and configuration (if changes are present)
		config.modules.put(moduleId(), ourConfigCategory);
		if (hasConfigChanged) {
			try {
				config.writeAll();
			} catch (IOException e) {
				error("Config saving failed!", e);
			}
		}

	}
}
