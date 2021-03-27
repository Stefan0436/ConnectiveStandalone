package org.example.examplemodule.examples;

import java.net.Socket;

import org.asf.rats.processors.HttpPostProcessor;

public class ExamplePostProcessor extends HttpPostProcessor {

	@Override
	public HttpPostProcessor createNewInstance() {
		return new ExamplePostProcessor();
	}

	@Override
	public String path() {
		return "/example";
	}

	@Override
	public void process(String arg0, Socket arg1) {
		this.setBody("Example: Hello World!\n" + this.getBody());
	}

}
