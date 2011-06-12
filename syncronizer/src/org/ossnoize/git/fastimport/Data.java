package org.ossnoize.git.fastimport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Data implements FastImportObject {
	private static final String DATA = "data";
	private ByteArrayOutputStream Container;
	
	public Data() {
		Container = new ByteArrayOutputStream();
	}
	
	public OutputStream getOutputStream() {
		return Container;
	}
	
	public void writeData(byte[] array) throws IOException {
		Container.write(array);
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(DATA).append(" ").append(Container.size()).append("\n");
		out.write(builder.toString().getBytes());
		Container.writeTo(out);
		out.write('\n');
	}

}
