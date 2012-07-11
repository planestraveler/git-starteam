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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.ossnoize.git.fastimport.Commit;
import org.ossnoize.git.fastimport.Data;
import org.ossnoize.git.fastimport.FileDelete;
import org.ossnoize.git.fastimport.FileModification;
import org.ossnoize.git.fastimport.FileOperation;
import org.ossnoize.git.fastimport.enumeration.GitFileType;
import org.ossnoize.git.fastimport.exception.InvalidPathException;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Item;
import com.starbase.starteam.Project;
import com.starbase.starteam.PropertyEnums;
import com.starbase.starteam.Server;
import com.starbase.starteam.Status;
import com.starbase.starteam.View;
import com.starbase.starteam.File;
import com.starbase.starteam.CheckoutManager;
import com.starbase.util.MD5;
import com.starbase.util.OLEDate;

public class GitImporter {
	private static final String revisionKeyFormat = "{0,number,000000000000000}|{1,number,000000}|{2}|{3}";
	private Server server;
	private Project project;
	private View view;
	private long lastModifiedTime = 0;
	private Map<String, File> sortedFileList = new TreeMap<String, File>();
	private Map<String, File> AddedSortedFileList = new TreeMap<String, File>();
	private Map<String, File> lastSortedFileList = new TreeMap<String, File>();
	Commit lastcommit;
	OutputStream exportStream;
	private final String headFormat = "refs/heads/{0}";
	private String alternateHead = null;
	private boolean isResume = false;
	private RepositoryHelper helper;
	private PropertyEnums propertyEnums = null;
	// Use this set to find all the deleted files.
	private Set<String> files = new HashSet<String>();
	private Set<String> deletedFiles = new HashSet<String>();
	private Set<String> lastFiles = new HashSet<String>();
	
	public GitImporter(Server s, Project p) {
		server = s;
		project = p;
		propertyEnums = server.getPropertyEnums();
	}

	public void init(View v) {
		view = v;
	}
	
	public void end() {
	}
	
	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void recursiveLastModifiedTime(Folder f) {
		for(Item i : f.getItems(f.getTypeNames().FILE)) {
			if(i instanceof File) {
				long modifiedTime = i.getModifiedTime().getLongValue();
				if(modifiedTime > lastModifiedTime) {
					lastModifiedTime = modifiedTime;
				}
			}
		}
		for(Folder subfolder : f.getSubFolders()) {
			recursiveLastModifiedTime(subfolder);
		}
	}

	public void generateFastImportStream(long firstTime) {
		helper = RepositoryHelperFactory.getFactory().createHelper();
		try {
			exportStream = helper.getFastImportStream();
		} catch (NullPointerException e) {
			e.printStackTrace();
			return;
		}
		CheckoutManager cm = new CheckoutManager(view);
		Folder root = view.getRootFolder();
		files.clear();
		deletedFiles.clear();
		deletedFiles.addAll(lastFiles);
		sortedFileList.clear();
		recursiveFilePopulation(root);
		lastFiles.clear();
		lastFiles.addAll(files);
		lastSortedFileList.clear();
		lastSortedFileList.putAll(sortedFileList);
//		if(lastSortedFileList.equals(sortedFileList)) {
//			return;
//		}
//		System.err.println("lastSortedFileList:");
//		for(Map.Entry<String, File> e : lastSortedFileList.entrySet()) {
//			System.err.println(e.getKey() + e.getValue());
//		}
//		System.err.println("sortedFileList:");
//		for(Map.Entry<String, File> e : sortedFileList.entrySet()) {
//			System.err.println(e.getKey() + e.getValue());
//		}
		recoverDeleteInformation(deletedFiles);

		String head = view.getName();
		if(null != alternateHead) {
			head = alternateHead;
		}
		String lastComment = "";
		int lastUID = -1;
		lastcommit = null;
		for(Map.Entry<String, File> e : AddedSortedFileList.entrySet()) {
			File f = e.getValue();
			String cmt = f.getComment();
			String userName = server.getUser(f.getModifiedBy()).getName();
			String userEmail = userName.replaceAll(" ", ".");
			userEmail += "@" + "silan.com";
			String path = f.getParentFolderHierarchy() + f.getName();
			path = path.replace('\\', '/');
			// Strip the view name from the path
			int indexOfFirstPath = path.indexOf('/');
			path = path.substring(indexOfFirstPath + 1);
			
			try {
//				int fileStatus = f.getStatus();
//				System.err.println("fileStatus:" + fileStatus);
/*
				if(Status.UNKNOWN == fileStatus || Status.MODIFIED == fileStatus) {
					// try harder
					MD5 localChecksum = new MD5();
					java.io.File aFile = new java.io.File(System.getProperty("user.dir") + java.io.File.separator + f.getParentFolderHierarchy() + f.getName());
					localChecksum.computeFileMD5Ex(aFile);
					fileStatus = f.getStatusByMD5(localChecksum);
				}
*/
//				System.err.println(System.getProperty("user.dir") + java.io.File.separator + f.getParentFolderHierarchy() + f.getName());
//				System.err.println(cmt);
//				if(fileStatus != Status.CURRENT && fileStatus != Status.MODIFIED) {
					java.io.File aFile = java.io.File.createTempFile("StarteamFile", ".tmp");
					aFile.deleteOnExit();
					cm.checkoutTo(f, aFile);
//					f.checkoutTo(aFile, 0, true, false, false);
//					f.checkoutByVersion(aFile, 1, 0, true, false, false);
//					f.checkoutByDate(aFile, new OLEDate(1263427200000L), 0, true, false, false);
//					FileOutputStream s = new FileOutputStream(aFile);
//					f.checkoutToStream(s, 0, false);
//					if(propertyEnums.FILE_ENCODING_BINARY != f.getCharset()) {
//						// This is a text file we need to force it's EOL to CR
//						aFile = forceEOLToCR(aFile);
//					}
					
					FileModification fm = new FileModification(new Data(aFile));
					fm.setFileType(GitFileType.Normal);
					fm.setPath(path);
					if(null != lastcommit && lastComment.equalsIgnoreCase(cmt) && lastUID == f.getModifiedBy()) {
						lastcommit.addFileOperation(fm);
					} else {
						String ref = MessageFormat.format(headFormat, head);
						Commit commit = new Commit(userName, userEmail, cmt, ref, new java.util.Date(f.getModifiedTime().getLongValue()));
						commit.addFileOperation(fm);
						if(null == lastcommit) {
							if(isResume) {
								commit.resumeOnTopOfRef();
							}
						} else {
							System.err.println("commit 1.");
							lastcommit.writeTo(exportStream);
							if(! isResume) {
								isResume = true;
							}		
							commit.setFromCommit(lastcommit);
						}
						
						/** Keep last for information **/
						lastComment = cmt;
						lastUID = f.getModifiedBy();
						lastcommit = commit;
					}
//				}
			} catch (IOException io) {
				io.printStackTrace();
			} catch (InvalidPathException e1) {
				e1.printStackTrace();
			}
		}
		// TODO: Simple hack to make deletion of disapered files. Since starteam does not carry some kind of
		// TODO: delete event. (as known from now)
		if(deletedFiles.size() > 0) {
			try {
				String ref = MessageFormat.format(headFormat, head);
				Commit commit = new Commit("File Janitor", "janitor@cie.com", "Cleaning files move along", ref, new java.util.Date(firstTime));
				if(null == lastcommit) {
					if(isResume) {
						commit.resumeOnTopOfRef();
					}
				} else {
					System.err.println("commit 2.");
					lastcommit.writeTo(exportStream);
					if(! isResume) {
						isResume = true;
					}		
					commit.setFromCommit(lastcommit);
				}
				for(String path : deletedFiles) {
					if(!helper.isSpecialFile(path)) {
						FileOperation fileDelete = new FileDelete();
						try {
							fileDelete.setPath(path);
							commit.addFileOperation(fileDelete);
						} catch (InvalidPathException e1) {
							e1.printStackTrace();
						}
					}
				}
				lastcommit = commit;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(null != lastcommit) {
			try {
				System.err.println("commit 3.");
				lastcommit.writeTo(exportStream);
				exportStream.close();
				Thread.sleep(1000);
				RepositoryHelperFactory.getFactory().clearCachedHelper();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
            catch(InterruptedException e)   { 
                System.err.println( "Interrupted "); 
            } 
			if(! isResume) {
				isResume = true;
			}		
		} else {
			if(AddedSortedFileList.size() > 0) {
				System.err.println("There was no new revision in the starteam view.");
				System.err.println("All the files in the repository are at theire lastest version");
			} else {
//				System.err.println("The starteam view specified was empty.");
			}
		}
		AddedSortedFileList.clear();
	}
	
	private void recoverDeleteInformation(Set<String> listOfFiles) {
		for(String path : listOfFiles) {
			//TODO: add delete information when figured out how.
		}
	}

	public void setResume(boolean b) {
		isResume = b;
	}

	private void recursiveFilePopulation(Folder f) {
		boolean first = true;
		for(Item i : f.getItems(f.getTypeNames().FILE)) {
			if(i instanceof File) {
				File historyFile = (File) i;
				long modifiedTime = i.getModifiedTime().getLongValue();
				int userid = i.getModifiedBy();
				String path = i.getParentFolderHierarchy() + historyFile.getName();
				path = path.replace('\\', '/');
				//path = path.substring(1);
				int indexOfFirstPath = path.indexOf('/');
				path = path.substring(indexOfFirstPath + 1);

				if(deletedFiles.contains(path)) {
					deletedFiles.remove(path);
				}
				files.add(path);
				String comment = i.getComment();
				String key = MessageFormat.format(revisionKeyFormat, modifiedTime, userid, comment, path);
				if(! lastSortedFileList.containsKey(key)) {
					AddedSortedFileList.put(key, historyFile);
				}
				sortedFileList.put(key, historyFile);
//				System.err.println(path + i.getRevisionNumber());
//				System.err.println("ItemCreatedTime:" + i.getCreatedTime().getLongValue());
//				System.err.println("Found file marked as: " + key);
			}
		}
		for(Folder subfolder : f.getSubFolders()) {
			recursiveFilePopulation(subfolder);
		}
	}

	public void setHeadName(String head) {
		alternateHead = head;
	}
	
	private java.io.File forceEOLToCR(java.io.File iFile) throws IOException {
		java.io.File ret = java.io.File.createTempFile("ForceEOL", ".tmp");
		ret.deleteOnExit();
		
		FileInputStream fin = new FileInputStream(iFile);
		FileOutputStream writer = new FileOutputStream(ret);
		byte bytio[] = new byte[2048];
		int read = fin.read(bytio);
		while(read >= 0) {
			for(int i=0; i < read; ++i) {
				if(bytio[i] != 0x0D) {
					writer.write(bytio[i]);
				}
			}
			read = fin.read(bytio);
		}
		writer.close();
		fin.close();
		
		// Save some space at least
		iFile.delete();
		
		return ret;
	}
}
