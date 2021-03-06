package com.rho.rhover.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

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
	
	public static void close(Reader reader) {
		if (reader == null) {
			return;
		}
		try {
			reader.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
