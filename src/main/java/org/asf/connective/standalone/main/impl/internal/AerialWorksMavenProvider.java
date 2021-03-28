package org.asf.connective.standalone.main.impl.internal;

import org.asf.connective.standalone.IMavenRepositoryProvider;

public class AerialWorksMavenProvider implements IMavenRepositoryProvider {

	@Override
	public String serverBaseURL() {
		return "https://aerialworks.ddns.net/maven";
	}

	@Override
	public int priority() {
		return 0; // we host things that are not on any other repositories
	}

}
