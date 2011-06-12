package org.ossnoize.git.fastimport;

import org.ossnoize.git.fastimport.exception.InvalidPathException;

public interface FileOperation extends FastImportObject {
	public void setPath(String path) throws InvalidPathException;
}
