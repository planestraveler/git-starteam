package org.ossnoize.fakestarteam;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtility {
	private final static int BUFFER_SIZE = 64*1024;

	public static void close(Closeable ... list) {
		for(Closeable c : list) {
			if(null != c) {
				try {
					c.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static void copyStream(OutputStream out, InputStream in) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		
		int read = in.read(buffer);
		while(read >= 0) {
			out.write(buffer, 0, read);
			read = in.read(buffer);
		}
		out.flush();
	}
}
