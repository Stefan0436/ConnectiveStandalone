package org.example.examplemodule.examples;

import java.net.Socket;

import org.asf.rats.processors.HttpUploadProcessor;

// Simple echo POST processor
public class ExampleUploadProcessor extends HttpUploadProcessor {

	@Override
	public HttpUploadProcessor createNewInstance() {
		return new ExampleUploadProcessor();
	}

	@Override
	public String path() {
		return "/example";
	}

	@Override
	public void process(String arg0, Socket arg1, String method) {
		if (method.equals("POST"))
			this.setBody("Example: Hello World!\n" + this.getRequestBody());
		else {
			// Deny other methods
			this.setResponseCode(405);
			this.setResponseMessage(method.toUpperCase() + " is not supported.");
			this.setBody(getServer().genError(getResponse(), getRequest())); // Generate the error page
		}
	}

}
