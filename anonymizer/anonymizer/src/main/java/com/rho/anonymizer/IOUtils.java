package com.rho.anonymizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

// TODO: Remove this class and replace references with the version
// in the rhover_common project.
public final class IOUtils {

	private IOUtils() {
		
	}

	public static void close(InputStream inStream) {
		if (inStream == null) {
			return;
		}
		try {
			inStream.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void close(Writer writer) {
		if (writer == null) {
			return;
		}
		try {
			writer.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
