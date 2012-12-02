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

import java.text.MessageFormat;

import org.ossnoize.git.fastimport.exception.InvalidPathException;

public abstract class FileOperation implements FastImportObject {
	protected String Path;

	public void setPath(String path) throws InvalidPathException {
		if(path.endsWith("/"))
			throw new InvalidPathException(MessageFormat.format("The path {0} end with \"/\".", path));
		if(path.startsWith("/"))
			throw new InvalidPathException(MessageFormat.format("The path {0} start with a \"/\".", path));
		if(path.startsWith("\""))
			throw new InvalidPathException(MessageFormat.format("The path {0} start with a \".", path));
		if(path.contains("//"))
			throw new InvalidPathException(MessageFormat.format("The path {0} should not contains double \"/\".", path));
		if(path.contains("/../") || path.contains("/./"))
			throw new InvalidPathException(MessageFormat.format("The path {0} should not contains relative reference (.. or .) in it.", path));
		Path = path;
	}
	
	public String getPath() {
		return Path;
	}
	
	public abstract boolean isInline();
	
	public abstract MarkID getMark();
}
