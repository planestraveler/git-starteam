package org.ossnoize.git.fastimport;

import java.io.IOException;
import java.io.OutputStream;

public interface FastImportObject {

	public void writeTo(OutputStream out) throws IOException;
}
