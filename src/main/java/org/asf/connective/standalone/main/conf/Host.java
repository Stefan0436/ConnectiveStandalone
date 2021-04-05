package org.asf.connective.standalone.main.conf;

import org.asf.cyan.api.config.Configuration;

public class Host extends Configuration<Host> {

	@Override
	public String filename() {
		return null;
	}

	@Override
	public String folder() {
		return null;
	}
	
	public String ip = "0.0.0.0";
	public int port = 8080;
	public String contextDomain = "default";
	public String implementation = "auto";

}
