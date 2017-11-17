package com.rho.rhover.daemon;

/**
 * Represent issues in the source study data.
 * @author dhall
 *
 */
public class SourceDataException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SourceDataException(String message) {
		super(message);
	}

}
