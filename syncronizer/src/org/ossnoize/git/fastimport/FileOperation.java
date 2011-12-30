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

import org.ossnoize.git.fastimport.exception.InvalidPathException;

public abstract class FileOperation implements FastImportObject {
	protected String Path;

	public void setPath(String path) throws InvalidPathException {
		if(path.endsWith("/"))
			throw new InvalidPathException("The path end with '/'.");
		if(path.startsWith("/"))
			throw new InvalidPathException("The path start with a '/'.");
		if(path.startsWith("\""))
			throw new InvalidPathException("The path start with a '\"'.");
		if(path.contains("//"))
			throw new InvalidPathException("The path should not contains double '/'.");
		if(path.contains("/../") || path.contains("/./"))
			throw new InvalidPathException("The path should not contains relative reference (.. or .) in it.");
		Path = path;
	}
}
