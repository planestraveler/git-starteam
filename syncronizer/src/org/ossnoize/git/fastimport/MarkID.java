package org.ossnoize.git.fastimport;

import java.io.IOException;
import java.io.OutputStream;

public class MarkID implements FastImportObject {

	private static long MarkID = 0;
	public static MarkID getNextMarkID() {
		return new MarkID(MarkID++);
	}

	private String Id;
	
	private MarkID(long id) {
		Id = ":" + id;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(Id.getBytes());
	}

}
