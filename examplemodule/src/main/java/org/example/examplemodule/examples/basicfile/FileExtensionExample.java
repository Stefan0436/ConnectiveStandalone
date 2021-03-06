package org.example.examplemodule.examples.basicfile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;
import org.asf.rats.http.FileContext;
import org.asf.rats.http.providers.IFileExtensionProvider;
import org.asf.rats.http.providers.IServerProviderExtension;

// Simple file extension processor replacing all occurrences of %tester% with 'world'
public class FileExtensionExample implements IFileExtensionProvider, IServerProviderExtension {

	private ConnectiveHTTPServer server;

	// IMPORTANT: the contextfile instructions depend on an empty constructor for instantiation.

	@Override
	public String fileExtension() {

		//
		// Important notice:
		// This method should be considered a final field and must not use code that can
		// generate dynamic results, this field runs in the main instance, rewrite runs
		// in its own instance, see newInstance() below.
		//

		return ".test";
	}

	@Override
	public FileContext rewrite(HttpResponse input, HttpRequest request) {
		String str;
		try {
			str = new String(input.body.readAllBytes());
		} catch (IOException e) {
			input.status = 503;
			input.message = "Internal server error";

			return FileContext.create(input, "text/html",
					new ByteArrayInputStream(server.genError(input, request).getBytes()));
		}
		return FileContext.create(input, "text/html",
				new ByteArrayInputStream(str.replaceAll("\\%tester\\%", "world").getBytes()));
	}

	@Override
	public void provide(ConnectiveHTTPServer arg0) {
		this.server = arg0;
	}

	@Override
	public IFileExtensionProvider newInstance() {
		return new FileExtensionExample();
	}

}
