/*****************************************************************************
    This file is part of Git-Starteam.

    Git-Starteam is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Git-Starteam is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Git-Starteam.  If not, see <http://www.gnu.org/licenses/>.
******************************************************************************/
package org.sync;

import java.io.IOException;
import java.io.InputStream;

import com.starbase.util.MD5;

public class MD5Builder implements Runnable {

	private InputStream md5Stream;
	private MD5 output;
	private long length;
	
	/**
	 * Create a runnable object which will compute the MD5 sum of any stream.
	 * @param input the stream to compute the MD5sum
	 */
	public MD5Builder(InputStream input) {
		md5Stream = input;
		output = new MD5();
	}
	
	@Override
	public void run() {
		try {
			length = output.computeStreamMD5Ex(md5Stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return The resulting MD5 object 
	 */
	public MD5 getMD5() {
		return output;
	}
	
	/**
	 * @return The length of the computed stream
	 */
	public long getLengthOfFile() {
		return length;
	}
}
