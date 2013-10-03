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
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.ossnoize.git.fastimport.Blob;
import org.ossnoize.git.fastimport.Commit;
import org.ossnoize.git.fastimport.Data;
import org.ossnoize.git.fastimport.DataRef;
import org.ossnoize.git.fastimport.FileDelete;
import org.ossnoize.git.fastimport.FileModification;
import org.ossnoize.git.fastimport.FileOperation;
import org.ossnoize.git.fastimport.Tag;
import org.ossnoize.git.fastimport.enumeration.GitFileType;
import org.ossnoize.git.fastimport.exception.InvalidPathException;
import org.sync.util.CommitInformation;
import org.sync.util.LabelDateComparator;
import org.sync.util.TempFileManager;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Item;
import com.starbase.starteam.ItemList;
import com.starbase.starteam.Label;
import com.starbase.starteam.Project;
import com.starbase.starteam.PropertyNames;
import com.starbase.starteam.RecycleBin;
import com.starbase.starteam.Server;
import com.starbase.starteam.StarTeamFinder;
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
	// all files discovered in the current view configuration
	private NavigableMap<CommitInformation, File> sortedFileList = new TreeMap<CommitInformation, File>();
	// files found in the current view configuration but not the previous view configuration
	private NavigableMap<CommitInformation, File> AddedSortedFileList = new TreeMap<CommitInformation, File>();
	// all files discovered in the previous view configuration
	private NavigableMap<CommitInformation, File> lastSortedFileList = new TreeMap<CommitInformation, File>();
	private Commit lastCommit; 
	// get the really old time as base information;
	private CommitInformation lastInformation = null;
	private OutputStream exportStream;
	private String alternateHead = null;
	private boolean isResume = false;
	private DataRef fromRef = null;
	private boolean verbose = false;
	private boolean createCheckpoints = false;
	private RepositoryHelper helper;
	// Use these sets to find all the deleted files.
	private Set<String> files = new HashSet<String>();
	private Set<String> deletedFiles = new HashSet<String>();
	private Set<String> lastFiles = new HashSet<String>();
	// Use this to find the renamed files.
	private long lastViewTime;
	// tracks which ref each tag points at
	private Map<String, DataRef> tagMarks = new HashMap<String, DataRef>();
	// email domain to use
	private String domain;
	
	public GitImporter(Server s, Project p) {
		server = s;
		project = p;
		helper = RepositoryHelperFactory.getFactory().createHelper();
	}
	
	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setFolder(View view, String folderPattern) {
		if(null != folderPattern) {
			folder = findFirstFolder(view.getRootFolder(), folderPattern);
			if (folder != null) {
				String path = this.folder.getFolderHierarchy();
				path = path.replace('\\', '/');
				int indexOfFirstPath = path.indexOf('/');
				path = path.substring(indexOfFirstPath + 1);
				folderNameLength = path.length();
				if(verbose) {
					Log.log("Folder: " + path);
				}
			} else {
				folderNameLength = 0;
			}
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
			}
		}
		for(Folder subfolder : f.getSubFolders()) {
			recursiveLastModifiedTime(subfolder);
		}
	}

	public void clearAllFiles() {
		files.clear();
		deletedFiles.clear();
		sortedFileList.clear();
		lastFiles.clear();
		lastSortedFileList.clear();
		AddedSortedFileList.clear();
	}

	public void setLastFilesLastSortedFileList(View view, String head, String folderPath) {
		folder = null;
		setFolder(view, folderPath);
		if(null == folder) {
			return;
		}

		files.clear();
		deletedFiles.clear();
		deletedFiles.addAll(lastFiles);
		sortedFileList.clear();
		folder.populateNow(server.getTypeNames().FILE, null, -1);
		recursiveFilePopulation(head, folder);
		lastFiles.clear();
		lastFiles.addAll(files);
		lastSortedFileList.clear();
		lastSortedFileList.putAll(sortedFileList);

		AddedSortedFileList.clear();
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void generateFastImportStream(View view, String folderPath) {
		// http://techpubs.borland.com/starteam/2009/en/sdk_documentation/api/com/starbase/starteam/CheckoutManager.html
		// said old version (passed in /opt/StarTeamCP_2005r2/lib/starteam80.jar) "Deprecated. Use View.createCheckoutManager() instead."
		CheckoutManager cm = new CheckoutManager(view);
		cm.getOptions().setEOLConversionEnabled(false);
		// Disabling status update leads to a large performance increase.
		cm.getOptions().setUpdateStatus(false);
		lastInformation = new CommitInformation(Long.MIN_VALUE, Integer.MIN_VALUE, "", "");

		folder = null;
		setFolder(view, folderPath);
		if(null == folder) {
			if(folderPath != null) {
				Log.log("Folder not found: " + folderPath);
			}
			return;
		}
		
		String head = view.getName();
		if(null != alternateHead) {
			head = alternateHead;
		}
		
		if(verbose) {
			Log.out("Populating files");
		}
		files.clear();
		deletedFiles.clear();
		deletedFiles.addAll(lastFiles);
		sortedFileList.clear();
		PropertyNames propNames = folder.getPropertyNames();
		// is this really worth all the trouble? null is pretty fast...
		String[] populateProps = new String[] {
			propNames.FILE_NAME,
			propNames.COMMENT,
			propNames.FILE_CONTENT_REVISION,
			propNames.FILE_DESCRIPTION,
			propNames.MODIFIED_TIME,
			propNames.MODIFIED_USER_ID,
			propNames.EXCLUSIVE_LOCKER,
			propNames.NON_EXCLUSIVE_LOCKERS,
			propNames.FILE_ENCODING,
			propNames.FILE_EOL_CHARACTER,
			propNames.FILE_EXECUTABLE,
		};
		folder.populateNow(server.getTypeNames().FILE, populateProps, -1);
		recursiveFilePopulation(head, folder);
		lastFiles.clear();
		lastFiles.addAll(files);
		lastSortedFileList.clear();
		lastSortedFileList.putAll(sortedFileList);
		recoverDeleteInformation(deletedFiles, head, view);

		if(verbose) {
			Log.out("Creating commits");
			// this is helpful for debugging out-of-order commits
			if(!AddedSortedFileList.isEmpty()) {
				CommitInformation earliest = AddedSortedFileList.firstKey();
				CommitInformation latest = AddedSortedFileList.lastKey();
				Log.log("Earliest file: " + earliest.getPath() + " @ " + new java.util.Date(earliest.getTime()));
				Log.log("Latest file: " + latest.getPath() + " @ " + new java.util.Date(latest.getTime()));
			}
		}
		exportStream = helper.getFastImportStream();
		for(Map.Entry<CommitInformation, File> e : AddedSortedFileList.entrySet()) {
			File f = e.getValue();
			CommitInformation current = e.getKey();
			String userName = server.getUser(current.getUid()).getName();
			String userEmail = userName.replaceAll(" ", ".") + "@" + domain;
			String path = pathname(f);

			try {
				FileOperation fo = null;
				if(current.isFileDelete()) {
					fo = new FileDelete();
					// path may be the new file, but current.getPath() is the old deleted path.
					fo.setPath(current.getPath());
					helper.unregisterFileId(head, current.getPath());
					if(verbose) {
						Log.log("Unregistered " + current.getPath());
					}
				} else {
					java.io.File aFile = TempFileManager.getInstance().createTempFile("StarteamFile", ".tmp");
					try {
						cm.checkoutTo(f, aFile);
					} catch (Exception ex) {
						Log.logf("Failed to checkout %s: %s", path, ex);
						continue;
					}
					Blob fileToStage = new Blob(new Data(aFile));
					
					helper.writeBlob(fileToStage);
					
					Integer revision = helper.getRegisteredFileVersion(head, path);
					if(null != revision && revision != f.getContentVersion()) { 
						helper.updateFileVersion(head, path, f.getContentVersion());
						if(verbose) {
						    Log.log("file was updated " + revision + " => " + f.getContentVersion() + ": " + path);
						}
					}
					
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
						} else if(null != fromRef) {
							commit.setFromRef(fromRef);
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
				Log.log("Git outputstream just crash unexpectedly. Stopping process");
				System.exit(-1);
			} catch (InvalidPathException e1) {
				e1.printStackTrace();
			}
		}
		// TODO: Simple hack to make deletion of diapered files. Since starteam does not carry some kind of
		// TODO: delete event. (as known from now)
		if(deletedFiles.size() > 0) {
			try {
				Log.log("Janitor was needed for cleanup");
				java.util.Date janitorTime;
				if(view.getConfiguration().isTimeBased()) {
					janitorTime = new java.util.Date(view.getConfiguration().getTime().getLongValue());
				} else if (view.getConfiguration().isLabelBased()) {
					long labelTime = Long.MIN_VALUE;
					for(Label l : view.fetchAllLabels()) {
						if(l.getID() == view.getConfiguration().getLabelID()) {
							labelTime = l.getRevisionTime().getLongValue();
							break;
						}
					}
					if(labelTime != Long.MIN_VALUE) {
						janitorTime = new java.util.Date(labelTime);
					} else {
						Log.log("Could not figure out what is the time of the label id " + view.getConfiguration().getLabelID());
						janitorTime = new java.util.Date();
					}
				} else {
					janitorTime = new java.util.Date(lastModifiedTime);
				}
				Commit commit = new Commit("git-starteam File Janitor",
						"janitor@" + domain,
						"Cleaning files move along",
						head,
						janitorTime);
				if(null == lastCommit) {
					if(isResume) {
						commit.resumeOnTopOfRef();
					} else if(null != fromRef) {
						commit.setFromRef(fromRef);
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
				Log.log("There was no new revision in the starteam view.");
				Log.log("All the files in the repository are at their latest version");
			} else {
//				Log.log("The starteam view specified was empty.");
			}
		}

		AddedSortedFileList.clear();
		cm = null;
		folder.discardItems(server.getTypeNames().FILE, -1);
	}
	
	@Override
	protected void finalize() throws Throwable {
		exportStream.close();
		while(helper.isFastImportRunning()) {
			Thread.sleep(500); // active wait but leave him a chance to actually finish.
		}
		super.finalize();
	}

	// Determines what happened to files which were in the view during the previous
	// recursiveFilePopulation run but were not found in the current run.
	//
	// Deleted files will be found in the view recycle bin. Otherwise the file was renamed.
	private void recoverDeleteInformation(Set<String> listOfFiles, String head, View view) {
		RecycleBin recycleBin = view.getRecycleBin();
		recycleBin.setIncludeDeletedItems(true);
		Type fileType = server.typeForName(recycleBin.getTypeNames().FILE);
		RenameFinder renameFinder = new RenameFinder();

		// Populate all the deleted file properties together for performance reasons.
		String[] deletedPaths = new String[listOfFiles.size()];
		File[] deletedFiles = new File[listOfFiles.size()];
		int nDeleted = 0;

		// No need to call populateNow on recycleBin.getRootFolder(), as that
		// is done by recycleBin.findItem(). If we called it now, we would
		// incur a long wait which we may not need.
		for(Iterator<String> ith = listOfFiles.iterator(); ith.hasNext(); ) {
			String path = ith.next();
			Integer fileID = helper.getRegisteredFileId(head, path);
			if(null != fileID) {
				File item = (File) recycleBin.findItem(fileType, fileID);
				if(null != item && item.isDeleted()) {
					deletedPaths[nDeleted] = path;
					deletedFiles[nDeleted] = item;
					nDeleted++;
					ith.remove();
				} else {
					item = (File) view.findItem(fileType, fileID);
					if(null != item) {
						CommitInformation deleteInfo = null;
						Item renameEventItem = renameFinder.findEventItem(view, path, pathname(item), item, lastViewTime);
						if(null != renameEventItem) {
							if(verbose) {
								Log.logf("Renamed %s -> %s at %s",
												  path, pathname(item),
												  renameEventItem.getModifiedTime());
							}
							deleteInfo = new CommitInformation(renameEventItem.getModifiedTime().getLongValue(),
															   renameEventItem.getModifiedBy(),
															   "",
															   path);
						} else {
							if(verbose) {
								Log.logf("No rename event found: %s -> %s", path, pathname(item));
							}
							// Not sure how this happens, but fill in with the
							// only information we have: the last view time
							// and the last person to modify the item.
							deleteInfo = new CommitInformation(lastViewTime,
															   item.getModifiedBy(),
															   "",
															   path);
						}
						deleteInfo.setFileDelete(true);
						ith.remove();
						// Cause old file to be deleted.
						sortedFileList.put(deleteInfo, item);
						AddedSortedFileList.put(deleteInfo, item);
						// Replace the existing entries for item if they have an earlier timestamp.
						CommitInformation info = new CommitInformation(deleteInfo.getTime(),
																	   deleteInfo.getUid(),
																	   "",
																	   pathname(item));
						replaceEarlierCommitInfo(sortedFileList, info, item);
						replaceEarlierCommitInfo(AddedSortedFileList, info, item);
					}
				}
			} else {
				Log.log("Never seen the file " + path + " in " + head);
			}
		}

		if (nDeleted > 0) {
			ItemList items = new ItemList();
			for (int i = 0; i < nDeleted; i++) {
				items.addItem(deletedFiles[i]);
			}
			PropertyNames propNames = view.getPropertyNames();
			String[] populateProps = new String[] {
				propNames.FILE_NAME,
				propNames.ITEM_DELETED_TIME,
				propNames.ITEM_DELETED_USER_ID,
			};
			items.populateNow(populateProps);

			for (int i = 0; i < nDeleted; i++) {
				File item = deletedFiles[i];
				CommitInformation info = new CommitInformation(item.getDeletedTime().getLongValue(),
															   item.getDeletedUserID(),
															   "",
															   deletedPaths[i]);
				info.setFileDelete(true);
				// Deleted files won't have entries, so add one here to make the delete end up in a commit.
				sortedFileList.put(info, item);
				AddedSortedFileList.put(info, item);
			}
		}

	}

	private void replaceEarlierCommitInfo(Map<CommitInformation, File> fileList, CommitInformation info, File file) {
		String path = pathname(file);
		// TODO: a better data structure for fileList would make this more efficient.
		for(Iterator<Map.Entry<CommitInformation, File>> ith = fileList.entrySet().iterator(); ith.hasNext(); ) {
			CommitInformation info2 = ith.next().getKey();
			if(path.equals(info2.getPath()) && info2.getTime() < info.getTime()) {
				ith.remove();
				fileList.put(info, file);
				return;
			}
		}
	}

	public void setResume(boolean b) {
		isResume = b;
	}

	public void setVerbose(boolean b) {
		verbose = b;
	}

	public void setCreateCheckpoints(boolean b) {
		createCheckpoints = b;
	}

	private Folder findFirstFolder(Folder f, String folderPattern) {
		//if(verbose) {
		//	Log.log("Looking for folder regex: " + folderPattern);
		//}
		Pattern pattern = Pattern.compile(folderPattern);

		// Bread-first search queue
		Deque<Folder> deque = new ArrayDeque<Folder>();
		deque.addLast(f);
		while(!deque.isEmpty()) {
			Folder folder = deque.removeFirst();
			String path = folder.getFolderHierarchy();
			path = path.replace('\\', '/');
			int indexOfFirstPath = path.indexOf('/');

			path = path.substring(indexOfFirstPath + 1);
			if(pattern.matcher(path).find()) {
				return folder;
			}
			//if(verbose) {
			//	Log.log("Not folder: " + path);
			//}
			for(Folder subfolder : folder.getSubFolders()) {
				deque.addLast(subfolder);
			}
		}
		return null;
	}

	private String pathname(File f) {
		String path = f.getParentFolderHierarchy() + f.getName();
		path = path.replace('\\', '/');
		int indexOfFirstPath = path.indexOf('/');
		int subindex = indexOfFirstPath + 1 + folderNameLength;
		try {
			path = path.substring(subindex);
		} catch (RuntimeException e) {
			Log.logf("Invalid subindex: %d + 1 + %d = %d, %s", indexOfFirstPath, folderNameLength, subindex, path);
		}
		return path;
	}

	private void recursiveFilePopulation(String head, Folder f) {
		for(Item i : f.getItems(f.getTypeNames().FILE)) {
			if(i instanceof File) {
				File historyFile = (File) i;
				String path = pathname(historyFile);

				if(null != head) {
					Integer fileid = helper.getRegisteredFileId(head, path);
					if(null == fileid) {
						// We register with version -1 to be sure to add it. Since this is a discovered file, when we are 
						// going to pass trough the files, we will make sure to get it's version 0.
						helper.registerFileId(head, path, historyFile.getItemID(), -1);
						fileid = historyFile.getItemID();
					}
					
				}
				if(deletedFiles.contains(path)) {
					deletedFiles.remove(path);
				}
				files.add(path);
				//if(verbose) {
				//	Log.log("Added file " + path);
				//}
				String comment = i.getComment();
				if(0 == comment.length()) {
					if(1 == historyFile.getContentVersion())
						comment = historyFile.getDescription();
					else
						comment = "Modification without comments";
				} else if(comment.matches("Merge from .*?, Revision .*")) {
					comment = "Merge from unknown branch";
				}
				CommitInformation info = new CommitInformation(i.getModifiedTime().getLongValue(), i.getModifiedBy(), comment, path);

				//TODO: find a proper solution to file update.
				if(! lastSortedFileList.containsKey(info)) {
					AddedSortedFileList.put(info, historyFile);
				}
				sortedFileList.put(info, historyFile);
			}
		}
		for(Folder subfolder : f.getSubFolders()) {
			recursiveFilePopulation(head, subfolder);
		}
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

	public void generateByLabelImport(View view, Date date, String baseFolder) {
		Label[] viewLabels = view.fetchAllLabels();
		String head = view.getName();
		if(null != alternateHead) {
			head = alternateHead;
		}
		Arrays.sort(viewLabels, new LabelDateComparator());
		int fromLabel = 0;
		if(isResume) {
			fromLabel = -1;
			java.util.Date lastCommit = helper.getLastCommitOfBranch(head);
			if(null != date) {
				lastCommit = date;
			}
			for(int i=0; i < viewLabels.length; ++i) {
				if(viewLabels[i].getRevisionTime().getLongValue() > lastCommit.getTime()) {
					Log.log("Importing from label <" + viewLabels[i].getName() + ">");
					fromLabel = i;
					break;
				}
			}
			if(-1 == fromLabel) {
				Log.log("No newer label are more recent then last commit made at " + lastCommit);
				return;
			}
		}
		setFolder(view, baseFolder);
		for(int i=fromLabel; i<viewLabels.length; ++i) {
			if(viewLabels[i].isViewLabel()) {
				View vc = new View(view, ViewConfiguration.createFromLabel(viewLabels[i].getID()));
				long viewTime = viewLabels[i].getRevisionTime().getLongValue();
				if(i == fromLabel && isResume) {
					setLastFilesLastSortedFileList(vc, head, baseFolder);
				}
				Log.log("View configuration label <" + viewLabels[i].getName() + ">");
				generateFastImportStream(vc, baseFolder);
				writeLabelTag(view, viewLabels[i]);
				checkpoint();
				vc.close();
				lastViewTime = viewTime;
			}
		}
		helper.gc();
	}

	public void generateAllLabelImport(View view, String baseFolder) {
		Label[] viewLabels = view.fetchAllLabels();
		Arrays.sort(viewLabels, new LabelDateComparator());

		for(int i=0; i<viewLabels.length; i++) {
			if(viewLabels[i].isViewLabel()) {
				View vc = new View(view, ViewConfiguration.createFromLabel(viewLabels[i].getID()));
				Log.logf("View configuration label <%s> (%d/%d)", viewLabels[i].getName(), i+1, viewLabels.length);
				generateFastImportStream(vc, baseFolder);
				writeLabelTag(view, viewLabels[i]);
				checkpoint();
				lastViewTime = viewLabels[i].getRevisionTime().getLongValue();

				vc.close();
			}
		}
	}

	private List<View> getAllViews(Project project, View rootView, String skipPatternStr) {
		Pattern skipPattern = null;
		if (skipPatternStr != null) {
			skipPattern = Pattern.compile(skipPatternStr);
		}

		if (rootView == null) {
			rootView = project.getDefaultView();
			while (rootView.getParentView() != null) {
				rootView = rootView.getParentView();
			}
		}

		List<View> views = new ArrayList<View>();

		// add them in bread-first order
		Deque<View> deque = new ArrayDeque<View>();
		deque.addLast(rootView);
		while(!deque.isEmpty()) {
			View view = deque.removeFirst();
			try {
				for (View derivedView: view.getDerivedViews()) {
					deque.addLast(derivedView);
				}
			} catch (RuntimeException e) {
				Log.log("Could not get derived views for " + view.getName() + ": " + e);
			}

			String viewName = null;
			ViewConfiguration baseConfig = null;
			try {
				String tag = getBaseTag(view);
				if (tag == null && view != rootView) {
					Log.log("Skipping non-rooted view " + view.getName());
					continue;
				}
				if (skipPattern != null && skipPattern.matcher(view.getName()).find()) {
					Log.log("Skipping view " + view.getName() + ", base " + tag);
					continue;
				}
				views.add(view);
			} catch (Exception e) {
				Log.log("Skipping view " + viewName + ": " + e);
			}
		}
		return views;
	}

	public void generateAllViewsImport(Project project, View rootView, String baseFolder, String skipPattern) {
		List<View> views = getAllViews(project, rootView, skipPattern);
		int count = 0;

		for (View view: views) {
			String tag = getBaseTag(view);
			Log.log("Will import view " + view.getName() + " onto " + tag);
			if (tag != null) {
				// Must have the mark for each tag that is the base of a branch
				// so that we can create a commit from it.
				tagMarks.put(tag, null);
			}
		}

		for (View view: views) {
			count++;
			DataRef baseRef = null;
			try {
				baseRef = getBaseRef(view);
			} catch (RuntimeException e) {
				Log.log("Could not get base ref for " + view.getName() + ": " + e);
				continue;
			}

			fromRef = baseRef;

			Log.logf("Importing view %s onto %s (%d/%d)", view.getName(), baseRef, count, views.size());
			setHeadName(refName(view.getName())); // TODO: allow user override (Groovy?)

			if(baseRef != null) {
				View baseView = new View(view.getParentView(), view.getBaseConfiguration());
				setLastFilesLastSortedFileList(baseView, baseView.getName(), baseFolder);
				baseView.close();
			} else {
				clearAllFiles();
			}
			lastCommit = null;
			isResume = false;

			generateAllLabelImport(view, baseFolder);

			view.close();
		}
		helper.gc();
	}

	private static String labelRef(View view, Label label) {
		return refName(view.getName() + "." + label.getName());
	}

	public static String refName(String name) {
		// Needs to be acceptable to git-check-ref-format(1).
		// Unfortunately the rules are defined as a black list instead of
		// a simple whitelist.
		return name
			// replace a sequence of '/' with a single '/', strip leading and trailing '/'
			.replaceAll("//+", "/").replaceFirst("^/", "").replaceFirst("/$", "")
			// sanitize @{
			.replaceAll("@\\{", "__")
			// sanitize <= \040, \177, space (\040), ~, ^, :, ?, *, [
			.replaceAll("[\000-\040\177~^:?*\\[]", "_")
			// Sanitize \ for \. restriction
			.replace("\\", "_")
			// Sanitize ..
			.replaceAll("\\.{2,}", "__")
			// Sanitize /.*
			.replace("/.", "/_")
			// Sanitize .lock extension
			.replaceFirst("\\.lock$", "_lock")
			// Sanitize trailing dot
			.replaceFirst("\\.$", "_");
	}

	private void writeLabelTag(View view, Label label) {
		DataRef ref = null;
		if(lastCommit != null) {
			ref = lastCommit.getMarkID();
		} else if(fromRef != null) {
			ref = fromRef;
		}

		if (ref == null) {
			Log.log("Not tagging label " + label.getName() + " because lastCommit and fromRef are null");
			return;
		}

		String tagName = labelRef(view, label);
		if (tagName.length() > 0) {
			// Only store the tags on which branches are based to save memory.
			if (tagMarks.containsKey(tagName)) {
				// Replace the null value with the ref.
				tagMarks.put(tagName, ref);
			}
			Date tagDate = null;
			if (label.isViewLabel()) {
				tagDate = new Date(label.getTime().getLongValue());
			} else if (label.isRevisionLabel()) {
				tagDate = new Date(label.getRevisionTime().getLongValue());
			} else {
				Log.log("No date for label, using today: " + label.getName());
				tagDate = new Date();
			}
			try {
				Tag tag = new Tag(tagName, ref, "StarTeam", "noreply@" + domain, tagDate, label.getDescription());
				helper.writeTag(tag);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void checkpoint() {
		if(createCheckpoints) {
			try {
				helper.writeCheckpoint();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void generateDayByDayImport(View view, Date date, String baseFolder) {
		View vc;
		long hour = 3600000L; // mSec
		long day = 24 * hour; // 86400000 mSec
		long firstTime = 0;
		long viewTime = 0;
		String head = view.getName();
		if(null != alternateHead) {
			head = alternateHead;
		}
		if(isResume) {
			java.util.Date lastCommit = helper.getLastCommitOfBranch(head);
			if(null != lastCommit) {
				firstTime = lastCommit.getTime();
			} else {
				Log.log("Cannot resume an import in a non existing branch");
				return;
			}
		}
		if(firstTime < view.getCreatedTime().getLongValue()) {
			firstTime = view.getCreatedTime().getLongValue();
		}
		Log.log("View Created Time: " + new java.util.Date(firstTime));
		if (null == date){
			if(isResume) {
				// -R is for branch view
				// 2000 mSec here is to avoid side effect in StarTeam View Configuration
				viewTime = firstTime + 2000;
				vc = new View(view, ViewConfiguration.createFromTime(new OLEDate(viewTime)));
				setLastFilesLastSortedFileList(vc, head, baseFolder);
				vc.close();
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
		setFolder(view, baseFolder);
		getFolder().populateNow(server.getTypeNames().FILE, null, -1);
		recursiveLastModifiedTime(getFolder());
		long lastTime = getLastModifiedTime();
		// in case View life less than 24 hours
		if(firstTime > lastTime && lastTime - view.getCreatedTime().getLongValue() < 24*hour) {
			firstTime = view.getCreatedTime().getLongValue();
		}

		Log.log("Commit from " + new java.util.Date(firstTime) + " to " + new java.util.Date(lastTime));
		Calendar timeIncrement = Calendar.getInstance();
		timeIncrement.setTimeInMillis(firstTime);
		for(;timeIncrement.getTimeInMillis() < lastTime; timeIncrement.add(Calendar.DAY_OF_YEAR, 1)) {
			if(lastTime - timeIncrement.getTimeInMillis() <= day) {
				vc = view;
			} else {
				vc = new View(view, ViewConfiguration.createFromTime(new OLEDate(timeIncrement.getTimeInMillis())));
				viewTime = timeIncrement.getTimeInMillis();
			}
			Log.log("View Configuration Time: " + timeIncrement.getTime());
			generateFastImportStream(vc, baseFolder);
			vc.close();
			lastViewTime = viewTime;
		}
		helper.gc();
	}

	private String getBaseTag(View v) {
		ViewConfiguration vc = v.getBaseConfiguration();
		if (vc.isLabelBased()) {
			for (Label label: v.getParentView().getActiveLabels()) {
				if (label.getID() == vc.getLabelID()) {
					return labelRef(v.getParentView(), label);
				}
			}
		} else if (vc.isTimeBased()) {
			//Log.log("View " + v.getName() + " is time-based");
			long baseTime = vc.getTime().getLongValue();

			// Find label nearest to but before baseTime.
			Label nearest = null;
			long nearestTime = 0;
			for (Label label: v.getParentView().getActiveLabels()) {
				if (label.isViewLabel()) {
					long labelTime = label.getTime().getLongValue();
					if (labelTime < baseTime && labelTime > nearestTime) {
						nearest = label;
						nearestTime = label.getTime().getLongValue();
					}
				}
			}

			if (nearest != null) {
				Log.logf("Using %s.%s at %s for view at %s",
						 v.getParentView().getName(), nearest.getName(), nearest.getTime(), vc.getTime());
				return labelRef(v.getParentView(), nearest);
			}
		} else if (vc.isPromotionStateBased()) {
			Log.log("View " + v.getName() + " is promotion-state based, not supported!");
		} else if (vc.isTip()) {
			//Log.log("View " + v.getName() + " is tip");
		} else {
			Log.log("View " + v.getName() + " is ??? based");
		}
		return null;
	}

	private DataRef getBaseRef(View v) {
		String tag = getBaseTag(v);
		if (tag == null) {
			return null;
		}
		return tagMarks.get(tag);
	}
	
	public void dispose() {
		helper.dispose();
	}
}
