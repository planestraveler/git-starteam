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
package org.sync.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class TempFileManager {

	private static TempFileManager Reference = new TempFileManager();
	public static TempFileManager getInstance() {
		return Reference;
	}
	
	private List<File> tempFiles;
	
	private TempFileManager() {
		tempFiles = new ArrayList<File>();
	}
	
	public File createTempFile(String name, String extension) throws IOException {
		File ret = File.createTempFile(name, extension);
		ret.deleteOnExit();
		tempFiles.add(ret);
		return ret;
	}
	
	public void deleteTempFiles() {
		for(File f : tempFiles) {
			f.delete();
		}
		tempFiles.clear();
	}
}
