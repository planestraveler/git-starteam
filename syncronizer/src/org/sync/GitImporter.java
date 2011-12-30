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
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;

import org.ossnoize.git.fastimport.Commit;
import org.ossnoize.git.fastimport.Data;
import org.ossnoize.git.fastimport.FileModification;
import org.ossnoize.git.fastimport.enumeration.GitFileType;
import org.ossnoize.git.fastimport.exception.InvalidPathException;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Item;
import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.Status;
import com.starbase.starteam.View;
import com.starbase.starteam.File;
import com.starbase.util.MD5;

public class GitImporter {
	private Server server;
	private Project project;
	private View view;
	private Map<String, File> sortedFileList = new TreeMap<String, File>();
	private final String headFormat = "refs/heads/{0}";
	private boolean isResume = false;
	
	public GitImporter(Server s, Project p, View v) {
		server = s;
		project = p;
		view = v;
	}

	public void generateFastImportStream() {
		Folder root = view.getRootFolder();
		recursiveFilePopulation(root);

		String lastComment = "";
		int lastUID = -1;
		Commit lastcommit = null;
		for(Map.Entry<String, File> e : sortedFileList.entrySet()) {
			File f = e.getValue();
			String cmt = f.getComment();
			String userName = server.getUser(f.getModifiedBy()).getName();
			String userEmail = userName.replaceAll(" ", ".");
			userEmail += "@" + "cie.com";
			String path = f.getParentFolderHierarchy() + java.io.File.separator + f.getName();
			/* TODO: need to validate this with the real thing */
			path = path.replace('\\', '/');
			path = path.substring(1);
			
			try {
				
				int fileStatus = f.getStatus();
				if(Status.UNKNOWN == fileStatus || Status.MODIFIED == fileStatus) {
					// try harder
					MD5 localChecksum = new MD5();
					java.io.File aFile = new java.io.File(System.getProperty("user.dir") + java.io.File.separator + f.getParentFolderHierarchy() + java.io.File.separator + f.getName());
					localChecksum.computeFileMD5Ex(aFile);
					fileStatus = f.getStatusByMD5(localChecksum);
				}
				if(fileStatus != Status.CURRENT && fileStatus != Status.MODIFIED) {
					java.io.File aFile = java.io.File.createTempFile("StarteamFile", ".tmp");
					aFile.deleteOnExit();
					f.checkoutTo(aFile, 0, true, false, false);
					
					FileModification fm = new FileModification(new Data(aFile));
					fm.setFileType(GitFileType.Normal);
					fm.setPath(path);
					if(null != lastcommit && lastComment.equalsIgnoreCase(cmt) && lastUID == f.getModifiedBy()) {
						lastcommit.addFileOperation(fm);
					} else {
						String ref = MessageFormat.format(headFormat, view.getName());
						Commit commit = new Commit(userName, userEmail, cmt, ref, new java.util.Date(f.getModifiedTime().getLongValue()));
						commit.addFileOperation(fm);
						if(null == lastcommit) {
							if(isResume) {
								commit.resumeOnTopOfRef();
							}
						} else {
							lastcommit.writeTo(System.out);
							commit.setFromCommit(lastcommit);
						}
						
						/** Keep last for information **/
						lastComment = cmt;
						lastUID = f.getModifiedBy();
						lastcommit = commit;
					}
				}
			} catch (IOException io) {
				io.printStackTrace();
			} catch (InvalidPathException e1) {
				e1.printStackTrace();
			}
		}
		try {
			lastcommit.writeTo(System.out);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void setResume(boolean b) {
		isResume = b;
	}

	private void recursiveFilePopulation(Folder f) {
		for(Item i : f.getItems(f.getTypeNames().FILE)) {
			if(i instanceof File) {
				File historyFile = (File) i;
				long modifiedTime = i.getModifiedTime().getLongValue();
				int userid = i.getModifiedBy();
				String path = i.getParentFolderHierarchy() + java.io.File.separator + historyFile.getName();
				String comment = i.getComment();
				String key = MessageFormat.format("{0,number,000000000000000}|{1,number,000000}|{2}|{3}", modifiedTime, userid, comment, path);
				sortedFileList.put(key, historyFile);
				System.err.println("Found file marked as: " + key);
			}
		}
		for(Folder subfolder : f.getSubFolders()) {
			recursiveFilePopulation(subfolder);
		}
	}
}
