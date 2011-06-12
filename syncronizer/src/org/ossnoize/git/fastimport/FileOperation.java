package org.ossnoize.git.fastimport;

import org.ossnoize.git.fastimport.exception.InvalidPathException;

public interface FileOperation {
	public void setPath(String path) throws InvalidPathException;
}
