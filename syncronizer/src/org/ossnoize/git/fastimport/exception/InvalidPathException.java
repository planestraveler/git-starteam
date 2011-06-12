package org.ossnoize.git.fastimport.exception;

public class InvalidPathException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9049924805400597631L;

	public InvalidPathException(String path) {
		super("The specified path " + path + " is an invalid path");
	}
}
