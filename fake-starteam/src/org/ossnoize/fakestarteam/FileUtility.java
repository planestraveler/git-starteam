/*****************************************************************************
 * All public interface based on Starteam API are a property of Borland, 
 * those interface are reproduced here only for testing purpose. You should
 * never use those interface to create a competitive product to the Starteam
 * Server. 
 * 
 * The implementation is given AS-IS and should not be considered a reference 
 * to the API. The behavior on a lots of method and class will not be the
 * same as the real API. The reproduction only seek to mimic some basic 
 * operation. You will not found anything here that can be deduced by using
 * the real API.
 * 
 * Fake-Starteam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *****************************************************************************/
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
