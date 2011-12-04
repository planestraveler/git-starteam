package org.ossnoize.fakestarteam;

import java.io.Closeable;
import java.io.IOException;

public class FileUtility {

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
}
