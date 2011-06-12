package org.ossnoize.git.fastimport;

import java.io.IOException;
import java.io.OutputStream;

public class Mark implements FastImportObject {

	private MarkID ID;
	
	public Mark() {
		ID = MarkID.getNextMarkID();
	}
	
	public void writeTo(OutputStream out) throws IOException {
		out.write("mark ".getBytes());
		ID.writeTo(out);
		out.write('\n');
	}

	public MarkID getID() {
		return ID;
	}
}
