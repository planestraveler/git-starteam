package org.ossnoize.git.fastimport;

import java.io.IOException;
import java.io.OutputStream;

public class Blob implements FastImportObject {

	private static final String BLOB = "blob\n";
	private Mark MarkId;

	public Blob() {
		MarkId = Mark.getMark();
	}
	
	public void writeTo(OutputStream out) throws IOException {
		out.write(BLOB.getBytes());
		MarkId.writeTo(out);
	}
}
