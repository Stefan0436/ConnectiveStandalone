package org.asf.connective.standalone;

public interface IMavenRepositoryProvider {
	public String serverBaseURL();
	
	public default int priority() {
		return 5;
	}
}
