package org.ossnoize.git.fastimport;

import java.io.IOException;
import java.io.OutputStream;

public class FileDelete extends FileOperation {
	private final static String DELETE_SP = "D ";
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		if(null == Path) {
			throw new NullPointerException("Path cannot be null");
		}
		StringBuilder builder = new StringBuilder();
		builder.append(DELETE_SP);
		builder.append(Path);
		builder.append('\n');
		
		out.write(builder.toString().getBytes());
	}
}
