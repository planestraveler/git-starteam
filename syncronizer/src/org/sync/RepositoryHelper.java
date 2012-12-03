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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import org.ossnoize.git.fastimport.Blob;
import org.ossnoize.git.fastimport.Commit;

import com.starbase.util.MD5;

public abstract class RepositoryHelper {

	protected File fastExportOverrideToFile;
	protected Commit lastCommit;

	/**
	 * Return the full list of path to files that are tracked in the repository.
	 * @return a set of File path contained in the current version of the repository.
	 */
	public abstract Set<String> getListOfTrackedFile(String head);
	
	/**
	 * Tell if the file is a special target repository type file.
	 * @return True if it is a special file, False otherwise.
	 */
	public abstract boolean isSpecialFile(String filename);
	
	/**
	 * Do the repository garbage collection and the compression of it's database.
	 */
	public abstract void gc();
	
	/**
	 * Tell if the "git fast-import" process is still running.
	 * @return True if it is running, False otherwise.
	 */
	public abstract boolean isFastImportRunning();
	
	/**
	 * Create a fast-import process and dump all the repository information in the 
	 * input stream of the process.
	 * @return The OutputStream representing the InputStream of the process.
	 */
	protected abstract OutputStream getFastImportStream();
	
	/**
	 * Register in file hidden inside the repository (.git, .bazaar, ...) the list
	 * of existing repository file and it's id.
	 * 
	 * @param filename the full path of the file and its name inside the repository
	 * @param fileId the Starteam file id
	 * @param fileVersion the Starteam file version
	 * @return true if the file was correctly registered. False otherwise.
	 */
	public abstract boolean registerFileId(String filename, int fileId, int fileVersion);
	
	/**
	 * Save in file hidden inside the repository (.git, .bazaar, ...) the version of an
	 * already registered file existing inside the repository.
	 * 
	 * @param filename the full path of the file and its name inside the repository
	 * @param fileVersion the Starteam version of this file.
	 */
	public abstract boolean updateFileVersion(String filename, int fileVersion);
	
	/**
	 * Remove the registered file from the repository.
	 *  
	 * @param filename the full path of the file and its name inside the repository
	 */
	public abstract void unregisterFileId(String filename);
	
	/**
	 * Return the registered file id from the repository tracked file.
	 * 
	 * @param filename the full path of the file and its name inside the repository
	 * @return the id of the file or NULL if not found.
	 */
	public abstract Integer getRegisteredFileId(String filename);
	
	/**
	 * Return the registered file version from the repository tracked file.
	 * 
	 * @param filename the full path of the file and its name inside the repository
	 * @return the id of the file or NULL if not found.
	 */
	public abstract Integer getRegisteredFileVersion(String filename);

	/**
	 * Return the MD5 sum object from the repository tracked file. 
	 * 
	 * @param filename The full path of the file and its name inside the repository
	 * @param head the branch we are referring to.
	 * @return the MD5 object generated for the file inside the repository.
	 */
	public abstract MD5 getMD5Of(String filename, String head) throws IOException;
	
	/**
	 * Request that the stream be dumped into the following file
	 * 
	 * @param file The file where is to be written the fast-export
	 **/	
	public void setFastExportDumpFile(File file) {
		fastExportOverrideToFile = file;
	}
	
	public void writeCommit(Commit commit) throws IOException {
		OutputStream fastImportStream = getFastImportStream();
		commit.writeTo(fastImportStream);
	}
	
	public void writeBlob(Blob fileToStage) throws IOException {
		OutputStream fastImportStream = getFastImportStream();
		fileToStage.writeTo(fastImportStream);
	}
	
	public abstract java.util.Date getLastCommitOfBranch(String branchName);

	public void dispose() {
		try {
			getFastImportStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
