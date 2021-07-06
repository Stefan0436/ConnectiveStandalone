package org.asf.connective.standalone.main;

import java.util.HashMap;
import java.util.Map;

import org.asf.cyan.api.config.Configuration;
import org.asf.cyan.api.config.annotations.Comment;
import org.asf.rats.ModuleBasedConfiguration;

/**
 * 
 * Connective Standalone Configuration, access as ModuleBasedConfiguration from
 * the 'memory.modules.shared.config' {@link org.asf.rats.Memory Memory} entry,
 * <b>avoid direct usage</b>.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@Comment("WARNING!")
@Comment("At the time of writing, CCFG does not support value overwriting!")
@Comment("When a configuration changes programmatically, it will be re-generated entirely, comments will get lost!")
@Comment("")
@Comment("Main server configuration file.")
@Comment("This is the base configuration used by ConnectiveHTTP.")
public class ConnectiveConfiguration extends ModuleBasedConfiguration<ConnectiveConfiguration> {

	private static ConnectiveConfiguration instance;

	public static ConnectiveConfiguration getInstance() {
		if (instance == null) {
			instance = new ConnectiveConfiguration(baseDir);
		}
		return instance;
	}
	
	public ConnectiveConfiguration() {
		this(Configuration.baseDir);
	}

	public ConnectiveConfiguration(String base) {
		super(base);
	}

	@Override
	public String filename() {
		return "server.ccfg";
	}

	@Override
	public String folder() {
		return "";
	}

	@Comment("Default HTTP Server Port")
	public int httpPort = 8080;

	@Comment("Default HTTP Server IP")
	public String httpIp = "0.0.0.0";

	@Comment("HTTP Context Configuration")
	@Comment("Format: context-root> 'contextfile'")
	public HashMap<String, String> context = new HashMap<String, String>(Map.of("root", "\n\nvirtualroot \"/\"\n\n"));

	@Comment("Normal get/post processors, specify module classes here.")
	public String processors = "\n";
	
	@Comment("Rewrite tasks for the rewrite contextfile instruction")
	public HashMap<String, String> tasks = new HashMap<String, String>();
}
