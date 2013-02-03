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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ossnoize.git.fastimport.Blob;
import org.ossnoize.git.fastimport.Checkpoint;
import org.ossnoize.git.fastimport.Commit;
import org.sync.util.StarteamFileInfo;

import com.starbase.util.MD5;

public abstract class RepositoryHelper {
	protected final static long timeForEachCheckpoint = 1000L*60L*60L*3L; // 3 Hours

	protected File fastExportOverrideToFile;
	protected long commitingSince = 0;
	protected Map<String, Map<String, StarteamFileInfo>> fileInformation;
	protected String repositoryDir;

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
	public abstract int gc();
	
	/**
	 * Tell if the "git fast-import" process is still running.
	 * @return True if it is running, False otherwise.
	 */
	public abstract boolean isFastImportRunning();
	
	/**
	 * Inform the user that the repository is a bare one
	 * @return True if it is a bare repository
	 */
	public abstract boolean isBareRepository();
	
	/**
	 * Create a fast-import process and dump all the repository information in the 
	 * input stream of the process.
	 * @return The OutputStream representing the InputStream of the process.
	 */
	protected abstract OutputStream getFastImportStream();
	
	/**
	 * Load the saved file information from the previous fast-export run.
	 * @return The file was found and loaded correctly
	 */
	protected abstract boolean loadFileInformation();

	/**
	 * Save the file information into an hidden file inside the repository folder.
	 */
	protected abstract void saveFileInformation();

	/**
	 * Register in file hidden inside the repository (.git, .bazaar, ...) the list
	 * of existing repository file and it's id.
	 * @param head reference name
	 * @param filename the full path of the file and its name inside the repository
	 * @param fileId the Starteam file id
	 * @param fileVersion the Starteam file version
	 * 
	 * @return true if the file was correctly registered. False otherwise.
	 */
	public boolean registerFileId(String head, String filename, int fileId, int fileVersion) {
		if(null == fileInformation) {
			if(!loadFileInformation()) {
				fileInformation = new HashMap<String, Map<String, StarteamFileInfo>>();
			}
		}
		if(!fileInformation.containsKey(head)) {
			fileInformation.put(head, new HashMap<String, StarteamFileInfo>());
		}
		if(!fileInformation.get(head).containsKey(filename)) {
			fileInformation.get(head).put(filename, new StarteamFileInfo(filename, fileId, fileVersion));
			return true;
		}
		return false;
	}

	/**
	 * Save in file hidden inside the repository (.git, .bazaar, ...) the version of an
	 * already registered file existing inside the repository.
	 * @param head the reference name
	 * @param filename the full path of the file and its name inside the repository
	 * @param fileVersion the Starteam version of this file.
	 */
	public boolean updateFileVersion(String head, String filename, int fileVersion) {
		if(null != fileInformation) {
			if(fileInformation.containsKey(head) && fileInformation.get(head).containsKey(filename)) {
				fileInformation.get(head).get(filename).setVersion(fileVersion);
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove the registered file from the repository.
	 * @param head the reference name
	 * @param filename the full path of the file and its name inside the repository
	 */
	public void unregisterFileId(String head, String filename) {
		if(null != fileInformation) {
			if(fileInformation.containsKey(head)) {
				fileInformation.get(head).remove(filename);
			}
		}
	}

	/**
	 * Return the registered file id from the repository tracked file.
	 * @param head the name of the branch to check
	 * @param filename the full path of the file and its name inside the repository
	 * 
	 * @return the id of the file or NULL if not found.
	 */
	public Integer getRegisteredFileId(String head, String filename) {
		if(null != fileInformation) {
			if(fileInformation.containsKey(head)) {
				if(fileInformation.get(head).containsKey(filename)) {
					return fileInformation.get(head).get(filename).getId();
				}
			}
		} else if (loadFileInformation()) {
			return getRegisteredFileId(head, filename);
		}
		return null;
	}

	/**
	 * Return the registered file version from the repository tracked file.
	 * @param head the name of the branch to check
	 * @param filename the full path of the file and its name inside the repository
	 * 
	 * @return the id of the file or NULL if not found.
	 */
	public Integer getRegisteredFileVersion(String head, String filename) {
		if(null != fileInformation) {
			if(fileInformation.containsKey(head)) {
				if(fileInformation.get(head).containsKey(filename)) {
					return fileInformation.get(head).get(filename).getVersion();
				}
			}
		} else if (loadFileInformation()) {
			return getRegisteredFileVersion(head, filename);
		}
		return null;
	}

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
	
	/**
	 * Write a commit into the fast-import stream. After 3 hours of continuous 
	 * commit, a checkpoint is done to preserve the history recorded so far.
	 * @param commit The commit to record.
	 * @throws IOException 
	 */
	public void writeCommit(Commit commit) throws IOException {
		OutputStream fastImportStream = getFastImportStream();
		commit.writeTo(fastImportStream);
		if(commitingSince <= 0) {
			commitingSince = System.currentTimeMillis();
		}
		if((System.currentTimeMillis() - commitingSince) >= timeForEachCheckpoint) {
			// if we are doing this process for 3 hours
			Checkpoint checkpoint = new Checkpoint();
			checkpoint.writeTo(fastImportStream);
			commitingSince = System.currentTimeMillis();
			System.err.println("Checkpoint done");
			saveFileInformation();
		}
	}
	
	/**
	 * Write a blob to the fast-import stream to be used by commit afterward.
	 * @param fileToStage The blob object representing a file or other markable
	 *                    data.
	 * @throws IOException
	 */
	public void writeBlob(Blob fileToStage) throws IOException {
		OutputStream fastImportStream = getFastImportStream();
		fileToStage.writeTo(fastImportStream);
	}
	
	public abstract java.util.Date getLastCommitOfBranch(String branchName);

	public void dispose() {
		saveFileInformation();
		try {
			getFastImportStream().close();
		} catch (IOException e) {
		}
	}
	
	/**
	 * Set the working directory where the fast-import process and all other
	 * process will query the repository. 
	 * @param dir Where to make the fast-import on disk
	 * @param create Ask to create the repository using an init command.
	 */
	public void setWorkingDirectory(String dir, boolean create) {
		repositoryDir = dir;
	}

}
