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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ErrorEater implements Runnable {

	private InputStream errorStream;
	private boolean dontWrite;
	private String process;
	
	/**
	 * Simple runner class to eat all error stream form subprocess and redirect it
	 * to the error stream of the current running class.
	 * @param in The error stream to read from.
	 */
	public ErrorEater(InputStream in) {
		this(in, "no identified", false);
	}

	/**
	 * Simple runner class to eat all stream and push it in the error stream of the
	 * current running class.
	 * @param in The error stream to read from.
	 * @param processName The name of the process launched.
	 */
	public ErrorEater(InputStream in, String processName) {
		this(in, processName, false);
	}
	
	/**
	 * Simple runner class to eat all stream from subprocess and redirect it
	 * to the error stream of the current running process or just dump it in the void.
	 * @param in The error stream to read from.
	 * @param dump If we don't write to the current process error stream.
	 */
	public ErrorEater(InputStream in, boolean dump) {
		this(in, "not identified", dump);
	}
	
	/**
	 * Simple runner class to eat all stream and push it in the error stream of the
	 * current running class.
	 * @param in The stream to read from.
	 * @param processName The name of the process launched.
	 * @param dump If we don't write to the current process error stream.
	 */
	public ErrorEater(InputStream in, String processName, boolean dump) {
		errorStream = in;
		dontWrite = dump;
		process = processName;
	}

	@Override
	public void run() {
		InputStreamReader reader = null;
		BufferedReader buffer = null;
		
		try {
			reader = new InputStreamReader(errorStream);
			buffer = new BufferedReader(reader);
			String line = null;
			while(null != (line = buffer.readLine())) {
				if(!dontWrite)
					System.err.println(process + ":" + line);
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(null != buffer) {
				try {
					buffer.close();
				} catch (IOException e) {
				}
			}
			if(null != reader) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
