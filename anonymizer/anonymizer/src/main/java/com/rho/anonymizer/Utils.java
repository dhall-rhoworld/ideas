package com.rho.anonymizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

public final class Utils {

	private Utils() {
		
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
