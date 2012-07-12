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

import java.io.OutputStream;
import java.util.Set;

public interface RepositoryHelper {

	/**
	 * Return the full list of path to files that are tracked in the repository.
	 * @return a set of File path contained in the current version of the repository.
	 */
	public Set<String> getListOfTrackedFile();
	
	/**
	 * Tell if the file is a special target repository type file.
	 * @return True if it is a special file, False otherwise.
	 */
	public boolean isSpecialFile(String filename);
	
	public boolean isGitFastImportRunning();
	
	/**
	 * Create a fast-import process and dump all the repository information in the 
	 * input stream of the process.
	 * @return The OutputStream representing the InputStream of the process.
	 */
	public OutputStream getFastImportStream();
}
