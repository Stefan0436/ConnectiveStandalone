package org.example.examplemodule.examples.basicfile;

import java.io.File;
import java.net.Socket;

import org.asf.rats.http.providers.IndexPageProvider;

// Example index page
public class IndexPageExample extends IndexPageProvider {

	// IMPORTANT: the contextfile instructions depend on an empty constructor for instantiation.

	@Override
	protected IndexPageProvider newInstance() {
		return new IndexPageExample();
	}

	@Override
	public void process(Socket client, File[] directories, File[] files) {
		String names = "";
		for (File f : files) {
			if (!names.isEmpty())
				names += ", ";

			names += f.getName();
		}

		setBody("My own index, little useless, but here is the file count: " + files.length + "\nFile names: " + names
				+ "\n\nTake a look at the sources of the DefaultIndexPage to get an idea of how to build such a page");
	}

}
