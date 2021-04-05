package org.asf.connective.standalone.main;

import java.util.HashMap;

import org.asf.connective.standalone.main.conf.Host;
import org.asf.cyan.api.config.Configuration;
import org.asf.cyan.api.config.annotations.Comment;

/**
 * 
 * Server hosts configuration.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@Comment("Host configuration file, configures extra hosts")
public class HostConfiguration extends Configuration<HostConfiguration> {

	private static HostConfiguration instance;

	public static HostConfiguration getInstance() {
		if (instance == null) {
			instance = new HostConfiguration();
		}
		return instance;
	}

	@Override
	public String filename() {
		return "hosts.ccfg";
	}

	@Override
	public String folder() {
		return "";
	}
	
	@Comment("Server hosts, configure alternate hosts here, the main host must be configured from server.ccfg")
	public HashMap<String, Host> hosts = new HashMap<String, Host>(); 
	
}
