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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.ossnoize.git.fastimport.Blob;
import org.ossnoize.git.fastimport.Commit;
import org.ossnoize.git.fastimport.Data;
import org.ossnoize.git.fastimport.FileDelete;
import org.ossnoize.git.fastimport.FileModification;
import org.ossnoize.git.fastimport.FileOperation;
import org.ossnoize.git.fastimport.enumeration.GitFileType;
import org.ossnoize.git.fastimport.exception.InvalidPathException;
import org.sync.util.CommitInformation;
import org.sync.util.TempFileManager;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Item;
import com.starbase.starteam.Project;
import com.starbase.starteam.RecycleBin;
import com.starbase.starteam.Server;
import com.starbase.starteam.Status;
import com.starbase.starteam.Type;
import com.starbase.starteam.View;
import com.starbase.starteam.File;
import com.starbase.starteam.CheckoutManager;
import com.starbase.starteam.ViewConfiguration;
import com.starbase.util.OLEDate;

public class GitImporter {
	private Server server;
	private Project project;
	private Folder folder;
	private int folderNameLength;
	private long lastModifiedTime = 0;
	private Map<CommitInformation, File> sortedFileList = new TreeMap<CommitInformation, File>();
	private Map<CommitInformation, File> AddedSortedFileList = new TreeMap<CommitInformation, File>();
	private Map<CommitInformation, File> lastSortedFileList = new TreeMap<CommitInformation, File>();
	private Commit lastCommit; 
	// get the really old time as base information;
	private CommitInformation lastInformation = null;
	private OutputStream exportStream;
	private String alternateHead = null;
	private boolean isResume = false;
	private boolean verbose = false;
	private RepositoryHelper helper;
	// Use these sets to find all the deleted files.
	private Set<String> files = new HashSet<String>();
	private Set<String> deletedFiles = new HashSet<String>();
	private Set<String> lastFiles = new HashSet<String>();
	
	public GitImporter(Server s, Project p) {
		server = s;
		project = p;
		helper = RepositoryHelperFactory.getFactory().createHelper();
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

	public void generateFastImportStream(View view, String folderPath, String domain) {
		// http://techpubs.borland.com/starteam/2009/en/sdk_documentation/api/com/starbase/starteam/CheckoutManager.html
		// said old version (passed in /opt/StarTeamCP_2005r2/lib/starteam80.jar) "Deprecated. Use View.createCheckoutManager() instead."
		CheckoutManager cm = new CheckoutManager(view);
		cm.getOptions().setEOLConversionEnabled(false);
		lastInformation = new CommitInformation(Long.MIN_VALUE, Integer.MIN_VALUE, "", "");

		folder = null;
		setFolder(view, folderPath);
		if(null == folder) {
			if(folderPath != null) {
				System.err.println("Folder not found: " + folderPath);
			}
			return;
		}
		
		String head = view.getName();
		if(null != alternateHead) {
			head = alternateHead;
		}
		
		if(verbose) {
			System.out.println("Populating files");
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
		recoverDeleteInformation(deletedFiles, head, view);

		if(verbose) {
			System.out.println("Creating commits");
		}
		exportStream = helper.getFastImportStream();
		for(Map.Entry<CommitInformation, File> e : AddedSortedFileList.entrySet()) {
			File f = e.getValue();
			CommitInformation current = e.getKey();
			String userName = server.getUser(current.getUid()).getName();
			String userEmail = userName.replaceAll(" ", ".") + "@" + domain;
			String path = f.getParentFolderHierarchy() + f.getName();
			path = path.replace('\\', '/');
			// Strip the view name from the path
			int indexOfFirstPath = path.indexOf('/');
			path = path.substring(indexOfFirstPath + 1 + folderNameLength);
			
			try {
				if(f.getStatus() == Status.CURRENT) {
					f.discard();
					continue;
				}
				if(f.getStatusByMD5(helper.getMD5Of(path, head)) == Status.CURRENT) {
					f.discard();
					continue;
				}
				FileOperation fo = null;
				java.io.File aFile = null;
				if(f.isDeleted() || current.isFileMove()) {
					fo = new FileDelete();
					fo.setPath(current.getPath());
					helper.unregisterFileId(head, path);
				} else {
					aFile = TempFileManager.getInstance().createTempFile("StarteamFile", ".tmp");
					cm.checkoutTo(f, aFile);
					
					Integer fileid = helper.getRegisteredFileId(head, path);
					if(null == fileid) {
						helper.registerFileId(head, path, f.getItemID(), f.getRevisionNumber());
					} else {
						helper.updateFileVersion(head, path, f.getRevisionNumber());
					}
					Blob fileToStage = new Blob(new Data(aFile));
					
					helper.writeBlob(fileToStage);
					
					FileModification fm = new FileModification(fileToStage);
					if(aFile.canExecute()) {
						fm.setFileType(GitFileType.Executable);
					} else {
						fm.setFileType(GitFileType.Normal);
					}
					fm.setPath(path);
					fo = fm;
				}
				if(null != lastCommit && lastInformation.equivalent(current)) {
					if(lastInformation.getComment().trim().length() == 0 && current.getComment().trim().length() > 0) {
						lastInformation = current;
						lastCommit.setComment(current.getComment());
					}
					lastCommit.addFileOperation(fo);
				} else {
					Commit commit = new Commit(userName, userEmail, current.getComment(), head, new java.util.Date(current.getTime()));
					commit.addFileOperation(fo);
					if(null == lastCommit) {
						if(isResume) {
							commit.resumeOnTopOfRef();
						}
					} else {
						helper.writeCommit(lastCommit);
						TempFileManager.getInstance().deleteTempFiles();
						commit.setFromCommit(lastCommit);
					}
					/** Keep last for information **/
					lastCommit = commit;
					lastInformation = current;
				}
			} catch (IOException io) {
				io.printStackTrace();
				System.err.println("Git outputstream just crash unexpectedly. Stopping process");
				System.exit(-1);
			} catch (InvalidPathException e1) {
				e1.printStackTrace();
			}
			f.discard();
		}
		// TODO: Simple hack to make deletion of diapered files. Since starteam does not carry some kind of
		// TODO: delete event. (as known from now)
		if(deletedFiles.size() > 0) {
			try {
				System.err.println("Janitor was needed for cleanup");
				java.util.Date janitorTime;
				if(view.getConfiguration().isTimeBased()) {
					janitorTime = new java.util.Date(view.getConfiguration().getTime().getLongValue());
				} else {
					janitorTime = new java.util.Date(lastModifiedTime);
				}
				Commit commit = new Commit("File Janitor",
						"janitor@" + domain,
						"Cleaning files move along",
						head,
						janitorTime);
				if(null == lastCommit) {
					if(isResume) {
						commit.resumeOnTopOfRef();
					}
				} else {
					helper.writeCommit(lastCommit);
					commit.setFromCommit(lastCommit);
					TempFileManager.getInstance().deleteTempFiles();
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
				lastCommit = commit;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(null != lastCommit) {
			try {
				helper.writeCommit(lastCommit);
				TempFileManager.getInstance().deleteTempFiles();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			if(AddedSortedFileList.size() > 0) {
				System.err.println("There was no new revision in the starteam view.");
				System.err.println("All the files in the repository are at their latest version");
			} else {
//				System.err.println("The starteam view specified was empty.");
			}
		}

		AddedSortedFileList.clear();
		cm = null;
		System.gc();
	}
	
	@Override
	protected void finalize() throws Throwable {
		exportStream.close();
		while(helper.isFastImportRunning()) {
			Thread.sleep(500); // active wait but leave him a chance to actually finish.
		}
		super.finalize();
	}
	
	private void recoverDeleteInformation(Set<String> listOfFiles, String head, View view) {
		RecycleBin recycleBin = view.getRecycleBin();
		recycleBin.setIncludeDeletedItems(true);
		Type fileType = server.typeForName(recycleBin.getTypeNames().FILE);
		for(Iterator<String> ith = listOfFiles.iterator(); ith.hasNext(); ) {
			String path = ith.next();
			Integer fileID = helper.getRegisteredFileId(head, path);
			if(null != fileID) {
				CommitInformation info = null;
				Item item = recycleBin.findItem(fileType, fileID);
				if(null != item && item.isDeleted()) {
					info = new CommitInformation(item.getDeletedTime().getLongValue(), 
												 item.getDeletedUserID(),
												 "",
												 path);
					item.discard();
				} else {
					item = view.findItem(fileType, fileID);
					if(null != item) {
						info = new CommitInformation(item.getModifiedTime().getLongValue(),
													 item.getModifiedBy(),
													 "",
													 path);
						info.setFileMove(true);
						item.discard();
					}
				}
				if(info != null) {
					ith.remove();
					sortedFileList.put(info, (File)item);
					AddedSortedFileList.put(info, (File)item);
				}
			} else {
				System.err.println("Never seen the file " + path + " in " + head);
			}
		}
	}

	public void setResume(boolean b) {
		isResume = b;
	}

	public void setVerbose(boolean b) {
		verbose = b;
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
			if(verbose) {
				System.err.println("Not folder: " + path);
			}
			recursiveFolderPopulation(subfolder, folderPath);
		}
		f.discard();
	}

	private void recursiveFilePopulation(Folder f) {
		if(verbose) {
			System.err.println("Adding directory " + f);
		}
		for(Item i : f.getItems(f.getTypeNames().FILE)) {
			if(i instanceof File) {
				File historyFile = (File) i;

				String path = i.getParentFolderHierarchy() + historyFile.getName();
				path = path.replace('\\', '/');
				//path = path.substring(1);
				int indexOfFirstPath = path.indexOf('/');
				path = path.substring(indexOfFirstPath + 1 + folderNameLength);

				if(deletedFiles.contains(path)) {
					deletedFiles.remove(path);
				}
				files.add(path);
				if(verbose) {
					System.err.println("Added file " + path);
				}
				CommitInformation info = new CommitInformation(i.getModifiedTime().getLongValue(), i.getModifiedBy(), i.getComment(), path);
				if(! lastSortedFileList.containsKey(info)) {
					AddedSortedFileList.put(info, historyFile);
//					System.err.println("Found file marked as: " + key);
				}
				sortedFileList.put(info, historyFile);
			}
			i.discard();
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
		if(null != helper) {
			helper.setFastExportDumpFile(file);
		} else {
			throw new NullPointerException("Ensure that the helper is correctly started.");
		}
	}

	public void generateDayByDayImport(View view, Date date, String baseFolder, String domain) {
		View vc;
		long hour = 3600000L; // mSec
		long day = 24 * hour; // 86400000 mSec
		long firstTime = 0;
		if(isResume) {
			String head = view.getName();
			if(null != alternateHead) {
				head = alternateHead;
			}
			java.util.Date lastCommit = helper.getLastCommitOfBranch(head);
			if(null != lastCommit) {
				firstTime = lastCommit.getTime();
			} else {
				System.err.println("Cannot resume an import in a non existing branch");
				return;
			}
		}
		if(firstTime < view.getCreatedTime().getLongValue()) {
			firstTime = view.getCreatedTime().getLongValue();
		}
		System.err.println("View Created Time: " + new java.util.Date(firstTime));
		if (null == date){
			if(isResume) {
				// -R is for branch view
				// 2000 mSec here is to avoid side effect in StarTeam View Configuration
				vc = new View(view, ViewConfiguration.createFromTime(new OLEDate(firstTime + 2000)));
				setLastFilesLastSortedFileList(vc, baseFolder);
				vc.discard();
			} 
			Calendar time = Calendar.getInstance();
			time.setTimeInMillis(firstTime);
			time.set(Calendar.HOUR_OF_DAY, 23);
			time.set(Calendar.MINUTE, 59);
			time.set(Calendar.SECOND, 59);
			date = time.getTime();
		}
		firstTime = date.getTime();
		
		// get the most recent commit 
		// could we just get the current date ? like (now)
		setFolder(view, baseFolder);
		recursiveLastModifiedTime(getFolder());
		long lastTime = getLastModifiedTime();
		// in case View life less than 24 hours
		if(firstTime > lastTime) {
			firstTime = view.getCreatedTime().getLongValue();
		}

		System.err.println("Commit from " + new java.util.Date(firstTime) + " to " + new java.util.Date(lastTime));
		Calendar timeIncrement = Calendar.getInstance();
		timeIncrement.setTimeInMillis(firstTime);
		for(;timeIncrement.getTimeInMillis() < lastTime; timeIncrement.add(Calendar.DAY_OF_YEAR, 1)) {
			if(lastTime - timeIncrement.getTimeInMillis() <= day) {
				vc = view;
			} else {
				vc = new View(view, ViewConfiguration.createFromTime(new OLEDate(timeIncrement.getTimeInMillis())));
			}
			System.err.println("View Configuration Time: " + timeIncrement.getTime());
			generateFastImportStream(vc, baseFolder, domain);
			vc.discard();
			vc = null;
		}
		helper.gc();
	}
	
	public void dispose() {
		helper.dispose();
	}
}
