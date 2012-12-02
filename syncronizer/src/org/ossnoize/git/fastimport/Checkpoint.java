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
package org.ossnoize.git.fastimport;

import java.io.IOException;
import java.io.OutputStream;

public class Checkpoint implements FastImportObject {
	
	private final static String CHECKPOINT = "checkpoint";
	
	/**
	 * Forces fast-import to close the current packfile, start a new one, and to
	 * save out all current branch refs, tags and marks. 
	 */
	public Checkpoint() {
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(CHECKPOINT.getBytes());
		out.write('\n');
		out.flush();
	}
}
