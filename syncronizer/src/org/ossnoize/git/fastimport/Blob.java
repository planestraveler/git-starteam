package org.ossnoize.git.fastimport;

import java.io.IOException;
import java.io.OutputStream;

public class Blob implements Markable {

	private static final String BLOB = "blob\n";
	private Mark MarkId;
	private Data Content;

	public Blob() {
		MarkId = new Mark();
	}
	
	public void setData(Data data) {
		Content = data;
	}
	
	public void writeTo(OutputStream out) throws IOException {
		out.write(BLOB.getBytes());
		MarkId.writeTo(out);
		Content.writeTo(out);
	}

	@Override
	public MarkID getMarkID() {
		return MarkId.getID();
	}
}
