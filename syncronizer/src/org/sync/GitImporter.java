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
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

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
import com.starbase.starteam.View;
import com.starbase.starteam.File;
import com.starbase.starteam.CheckoutManager;

public class GitImporter {
	private static final String revisionKeyFormat = "{0,number,000000000000000}|{1,number,000000}|{2}|{3}";
	private Server server;
	private Project project;
	private Folder folder;
	private int folderNameLength;
	private long lastModifiedTime = 0;
	private Map<String, File> sortedFileList = new TreeMap<String, File>();
	private Map<String, File> AddedSortedFileList = new TreeMap<String, File>();
	private Map<String, File> lastSortedFileList = new TreeMap<String, File>();
	private Commit lastcommit;
	private OutputStream exportStream;
	private final String headFormat = "refs/heads/{0}";
	private String alternateHead = null;
	private boolean isResume = false;
	private RepositoryHelper helper;
	private PropertyEnums propertyEnums = null;
	// Use these sets to find all the deleted files.
	private Set<String> files = new HashSet<String>();
	private Set<String> deletedFiles = new HashSet<String>();
	private Set<String> lastFiles = new HashSet<String>();
	
	public GitImporter(Server s, Project p) {
		server = s;
		project = p;
		propertyEnums = server.getPropertyEnums();
	}

	public void openHelper() {
		helper = RepositoryHelperFactory.getFactory().createHelper();
	}
	
	public void closeHelper() {
		helper.gc();
		RepositoryHelperFactory.getFactory().clearCachedHelper();
	}
	
	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setFolder(View view, String folderPath) {
		if(null != folderPath) {
			recursiveFolderPopulation(view.getRootFolder(), folderPath);
			folderNameLength = folderPath.length();
		} else {
			folder = view.getRootFolder();
			folderNameLength = 0;
		}
	}

	public Folder getFolder() {
		return folder;
	}

	public void recursiveLastModifiedTime(Folder f) {
		for(Item i : f.getItems(f.getTypeNames().FILE)) {
			if(i instanceof File) {
				long modifiedTime = i.getModifiedTime().getLongValue();
				if(modifiedTime > lastModifiedTime) {
					lastModifiedTime = modifiedTime;
				}
				i.discard();
			}
		}
		for(Folder subfolder : f.getSubFolders()) {
			recursiveLastModifiedTime(subfolder);
		}
		f.discard();
	}

	public void setLastFilesLastSortedFileList(View view, String folderPath) {
		folder = null;
		setFolder(view, folderPath);
		if(null == folder) {
			return;
		}

		files.clear();
		deletedFiles.clear();
		deletedFiles.addAll(lastFiles);
		sortedFileList.clear();
		recursiveFilePopulation(folder);
		lastFiles.clear();
		lastFiles.addAll(files);
		lastSortedFileList.clear();
		lastSortedFileList.putAll(sortedFileList);

		AddedSortedFileList.clear();
	}

	public void generateFastImportStream(View view, long vcTime, String folderPath, String domain) {
		// http://techpubs.borland.com/starteam/2009/en/sdk_documentation/api/com/starbase/starteam/CheckoutManager.html
		// said old version (passed in /opt/StarTeamCP_2005r2/lib/starteam80.jar) "Deprecated. Use View.createCheckoutManager() instead."
		CheckoutManager cm = new CheckoutManager(view);
		cm.getOptions().setEOLConversionEnabled(false);

		folder = null;
		setFolder(view, folderPath);
		if(null == folder) {
			return;
		}

		files.clear();
		deletedFiles.clear();
		deletedFiles.addAll(lastFiles);
		sortedFileList.clear();
		recursiveFilePopulation(folder);
		lastFiles.clear();
		lastFiles.addAll(files);
		lastSortedFileList.clear();
		lastSortedFileList.putAll(sortedFileList);
		recoverDeleteInformation(deletedFiles);

		if(AddedSortedFileList.size() > 0 || deletedFiles.size() > 0) {
			try {
				exportStream = helper.getFastImportStream();
			} catch (NullPointerException e) {
				e.printStackTrace();
				return;
			}
		}
		
		String head = view.getName();
		if(null != alternateHead) {
			head = alternateHead;
		}
		String lastComment = "";
		int lastUID = -1;
		lastcommit = null;
		Vector<java.io.File> lastFiles = new Vector<java.io.File>(10);
		for(Map.Entry<String, File> e : AddedSortedFileList.entrySet()) {
			File f = e.getValue();
			String cmt = f.getComment();
			String userName = server.getUser(f.getModifiedBy()).getName();
			String userEmail = userName.replaceAll(" ", ".");
			userEmail += "@" + domain;
			String path = f.getParentFolderHierarchy() + f.getName();
			path = path.replace('\\', '/');
			// Strip the view name from the path
			int indexOfFirstPath = path.indexOf('/');
			path = path.substring(indexOfFirstPath + 1 + folderNameLength);
			
			try {
				java.io.File aFile = java.io.File.createTempFile("StarteamFile", ".tmp");
				cm.checkoutTo(f, aFile);
				
				FileModification fm = new FileModification(new Data(aFile));
				if(aFile.canExecute()) {
					fm.setFileType(GitFileType.Executable);
				} else {
					fm.setFileType(GitFileType.Normal);
				}
				fm.setPath(path);
				if(null != lastcommit && lastComment.equalsIgnoreCase(cmt) && lastUID == f.getModifiedBy()) {
					lastcommit.addFileOperation(fm);
					lastFiles.add(aFile);
				} else {
					String ref = MessageFormat.format(headFormat, head);
					Commit commit = new Commit(userName, userEmail, cmt, ref, new java.util.Date(f.getModifiedTime().getLongValue()));
					commit.addFileOperation(fm);
					if(null == lastcommit) {
						if(isResume) {
							commit.resumeOnTopOfRef();
						}
					} else {
						lastcommit.writeTo(exportStream);
						if(! isResume) {
							isResume = true;
						}
						for(java.io.File old : lastFiles) {
							old.delete();
						}
						lastFiles.clear();
						commit.setFromCommit(lastcommit);
					}
					lastFiles.add(aFile);
					
					/** Keep last for information **/
					lastComment = cmt;
					lastUID = f.getModifiedBy();
					lastcommit = commit;
				}
			} catch (IOException io) {
				io.printStackTrace();
			} catch (InvalidPathException e1) {
				e1.printStackTrace();
			}
			f.discard();
		}
		// TODO: Simple hack to make deletion of disapered files. Since starteam does not carry some kind of
		// TODO: delete event. (as known from now)
		if(deletedFiles.size() > 0) {
			try {
				String ref = MessageFormat.format(headFormat, head);
				Commit commit = new Commit("File Janitor", "janitor@" + domain, "Cleaning files move along", ref, new java.util.Date(vcTime));
				if(null == lastcommit) {
					if(isResume) {
						commit.resumeOnTopOfRef();
					}
				} else {
					lastcommit.writeTo(exportStream);
					if(! isResume) {
						isResume = true;
					}
					for(java.io.File old : lastFiles) {
						old.delete();
					}
					lastFiles.clear();
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
				lastcommit.writeTo(exportStream);
				for(java.io.File old : lastFiles) {
					old.delete();
				}
				lastFiles.clear();
			} catch (IOException e1) {
				e1.printStackTrace();
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
		if(AddedSortedFileList.size() > 0 || deletedFiles.size() > 0) {
			try {
				exportStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.gc();
			while(helper.isGitFastImportRunning());
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

	private void recursiveFolderPopulation(Folder f, String folderPath) {
		for(Folder subfolder : f.getSubFolders()) {
			if(null != folder) {
				subfolder.discard();
				f.discard();
				break;
			}
			String path = subfolder.getFolderHierarchy();
			path = path.replace('\\', '/');
			int indexOfFirstPath = path.indexOf('/');
			path = path.substring(indexOfFirstPath + 1);
			if(folderPath.equalsIgnoreCase(path)) {
				folder = subfolder;
				break;
			}
			recursiveFolderPopulation(subfolder, folderPath);
		}
		f.discard();
	}

	private void recursiveFilePopulation(Folder f) {
		for(Item i : f.getItems(f.getTypeNames().FILE)) {
			if(i instanceof File) {
				File historyFile = (File) i;
				long modifiedTime = i.getModifiedTime().getLongValue();
				int userid = i.getModifiedBy();
				String path = i.getParentFolderHierarchy() + historyFile.getName();
				path = path.replace('\\', '/');
				//path = path.substring(1);
				int indexOfFirstPath = path.indexOf('/');
				path = path.substring(indexOfFirstPath + 1 + folderNameLength);

				if(deletedFiles.contains(path)) {
					deletedFiles.remove(path);
				}
				files.add(path);
				String comment = i.getComment();
				i.discard();
				String key = MessageFormat.format(revisionKeyFormat, modifiedTime, userid, comment, path);
				if(! lastSortedFileList.containsKey(key)) {
					AddedSortedFileList.put(key, historyFile);
//					System.err.println("Found file marked as: " + key);
				}
				sortedFileList.put(key, historyFile);
			}
		}
		for(Folder subfolder : f.getSubFolders()) {
			recursiveFilePopulation(subfolder);
		}
		f.discard();
	}

	public void setHeadName(String head) {
		alternateHead = head;
	}

	public void setDumpFile(java.io.File file) {
		if(null == helper) {
			helper.setFastExportDumpFile(file);
		} else {
			throw new NullPointerException("Ensure that the helper is correctly started.");
		}
	}
}
