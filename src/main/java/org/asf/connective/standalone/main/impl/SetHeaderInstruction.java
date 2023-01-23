package org.asf.connective.standalone.main.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.function.Consumer;

import org.asf.connective.standalone.ContextFileInstruction;
import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;
import org.asf.rats.http.ProviderContextFactory;
import org.asf.rats.http.providers.IDocumentPostProcessor;

public class SetHeaderInstruction implements ContextFileInstruction {

	@Override
	public String instructionName() {
		return "set-header";
	}

	@Override
	public int maximalArguments() {
		return 2;
	}

	@Override
	public void run(String[] arguments, ProviderContextFactory factory)
			throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		factory.addDocumentPostProcessor(new HeaderPostProcessor(arguments));
	}

	public static class HeaderPostProcessor implements IDocumentPostProcessor {

		private String[] arguments;
		private Consumer<String> callback;

		public HeaderPostProcessor(String[] arguments) {
			this.arguments = arguments;
		}

		@Override
		public boolean acceptNonHTML() {
			return true;
		}

		@Override
		public boolean match(String path, HttpRequest request) {
			return true;
		}

		@Override
		public IDocumentPostProcessor newInstance() {
			return new HeaderPostProcessor(arguments);
		}

		@Override
		public void process(String path, String uploadMediaType, HttpRequest request, HttpResponse response,
				Socket client, String method) {
			response.setHeader(arguments[0], arguments[1], true);
		}

		@Override
		public void setWriteCallback(Consumer<String> callback) {
			this.callback = callback;
		}

		@Override
		public Consumer<String> getWriteCallback() {
			return callback;
		}
	}

}
