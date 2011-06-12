package org.ossnoize.git.fastimport;

import java.io.IOException;
import java.io.OutputStream;

public class Mark implements FastImportObject {

	private static long MarkID = 0;
	private long ID;
	
	private Mark(long id) {
		ID = id;
	}
	
	public static Mark getMark() {
		return new Mark(MarkID++);
	}
	
	public void writeTo(OutputStream out) throws IOException {
		StringBuilder markString = new StringBuilder();
		markString.append("mark :").append(ID).append('\n');
		out.write(markString.toString().getBytes());
	}
}
