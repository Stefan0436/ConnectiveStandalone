package org.asf.connective.standalone.main.impl.internal;

import org.asf.connective.standalone.IMavenRepositoryProvider;

public class MavenCentralRepositoryProvider implements IMavenRepositoryProvider {

	@Override
	public String serverBaseURL() {
		return "https://repo1.maven.org/maven2/";		
	}

}
