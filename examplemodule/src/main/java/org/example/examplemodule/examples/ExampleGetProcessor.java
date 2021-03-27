package org.example.examplemodule.examples;

import java.net.Socket;

import org.asf.rats.processors.HttpGetProcessor;

public class ExampleGetProcessor extends HttpGetProcessor {

	@Override
	public HttpGetProcessor createNewInstance() {
		return new ExampleGetProcessor();
	}

	@Override
	public String path() {
		return "/example";
	}

	@Override
	public void process(Socket arg0) {
		this.setBody("Example: Hello World!");
	}

}
