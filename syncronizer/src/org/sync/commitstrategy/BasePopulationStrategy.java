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
package org.sync.commitstrategy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import com.starbase.starteam.*;
import org.sync.CommitPopulationStrategy;
import org.sync.Log;
import org.sync.RenameFinder;
import org.sync.RepositoryHelper;
import org.sync.util.CommitInformation;
import org.sync.util.Pair;

public class BasePopulationStrategy implements CommitPopulationStrategy {

	/// View on which operation of file population will take place.
	protected View currentView;
	protected HashSet<String> lastFiles;
	protected HashSet<String> deletedFiles;
	protected TreeMap<CommitInformation, File> currentCommitList;
	
	private RepositoryHelper helper;
	private int initialFileVersion;
	protected java.util.Date earliestTime;
	
	protected boolean verbose;

	protected boolean lookIntoRecycleBin;
	
	/**
	 * Base Population strategy constructor using a view as its base of operations
	 * to iterate around the files to construct the list of commit to reproduce in
	 * git. The algorithms are pretty generic and have plenty of override point to
	 * complet with more information.
	 * 
	 * @param view
	 *          The view where we shall collect ours commits informations
	 */
	public BasePopulationStrategy(View view) {
		currentView = view;
		lastFiles = new HashSet<String>();
		deletedFiles = new HashSet<String>();
		currentCommitList = new TreeMap<CommitInformation, File>();
		verbose = false;
		earliestTime = new java.util.Date(0);
		// We register new files with version -1 to be sure to add it. Since this is
		// a discovered file, when we are going to pass trough the files, we will
		// make sure to get it's version 1. Setting the following value to 0 would
		// grab all the version of the files since its creation
		initialFileVersion = -1;
		lookIntoRecycleBin = true;
	}

	@Override
	public void filePopulation(String head, Folder root) {
		currentCommitList.clear(); // flush every composed commit from last run.
		deletedFiles.clear();
		deletedFiles.addAll(lastFiles);
		populateStarteamProperties(root);
		doFilePopulation(head, "", root);
		lastFiles.removeAll(deletedFiles); // clean files that was never seen from the last files.
		recoverDeleteInformation(head, root);
		if (currentCommitList.size() > 0) {
			setLastCommitTime(currentCommitList.lastKey().getCommitDate());
		}
	}

	/**
	 * @param root
	 *          The root folder requiring the properties population
	 */
	protected void populateStarteamProperties(Folder root) {
		PropertyNames propNames = root.getPropertyNames();
		// Those are the interesting properties that we need.
		// Those will prevent back-and-forth with the server regarding the
		// collection of information.
		String[] populateProps = new String[] {
				propNames.FILE_NAME,
				propNames.COMMENT,
				propNames.FILE_DESCRIPTION,
				
				propNames.FILE_CONTENT_REVISION,
				propNames.MODIFIED_TIME,
				propNames.MODIFIED_USER_ID,
				propNames.EXCLUSIVE_LOCKER,
				propNames.NON_EXCLUSIVE_LOCKERS,
				propNames.FILE_ENCODING,
				propNames.FILE_EOL_CHARACTER,
				propNames.FILE_EXECUTABLE,
		    propNames.PATH_REVISION,
		};
		root.populateNow(currentView.getServer().getTypeNames().FILE, populateProps, -1);
	}

	/**
	 * Find out all files to be added into the commit list.
	 * 
	 * @param head
	 *          the head in which we should check for file content modification
	 * @param gitpath
	 *          the current path based on the root folder
	 * @param f
	 *          the folder to grab files from
	 */
	protected void doFilePopulation(String head, String gitpath, Folder f) {
		if (null == head) {
			throw new NullPointerException("Head cannot be null");
		}
		if (null == f) {
			throw new NullPointerException("Folder cannot be null");
		}
		for(Item i : f.getItems(f.getTypeNames().FILE)) {
			if(i instanceof File) {
				File historyFile = (File) i;
				String path = gitpath + (gitpath.length() > 0 ? "/" : "") + historyFile.getName();

				processFileForCommit(head, historyFile, path);
			} else {
				Log.log("Item " + f + "/" + i + " is not a file");
			}
		}
		for(Folder subfolder : f.getSubFolders()) {
			String newGitPath = gitpath + (gitpath.length() > 0 ? "/" : "") + subfolder.getName();
			doFilePopulation(head, newGitPath, subfolder);
		}
	}

	/***
	 * Process an individual file to create a commit in relation with its
	 * modifications
	 * 
	 * @param head
	 *          The target branch name
	 * @param historyFile
	 *          The file from the history we need to process
	 * @param path
	 *          The path where the file will be located in the git repository
	 */
	protected void processFileForCommit(String head, File historyFile, String path) {
		Integer fileid = helper.getRegisteredFileId(head, path);
		Integer previousVersion = -1;
		Integer previousContentVersion = -1;
		if (null == fileid) {
			helper.registerFileId(head, path, historyFile.getItemID(), initialFileVersion);
		} else {
			// fetch the previous version we did register so we continue
			// modification from that point in time.
			previousVersion = helper.getRegisteredFileVersion(head, path);
			previousContentVersion = helper.getRegisteredFileContentVersion(head, path);
		}
		if (deletedFiles.contains(path)) {
			deletedFiles.remove(path);
		}
		if (!lastFiles.contains(path)) {
			lastFiles.add(path);
		}
		// prefer content version as it is cached in the File object
		int itemViewVersion = historyFile.getContentVersion();
		if (fileid != null && fileid != historyFile.getItemID()) {
			Log.logf("File %s was replaced", path);			
			createCommitInformation(path, historyFile, 1);
		} else if (previousContentVersion < 0) {
			createCommitInformation(path, historyFile, 1);
		} else if (previousContentVersion > itemViewVersion) {
			Log.logf("File %s was reverted from version %d to %d was skipped", path, previousContentVersion,
					itemViewVersion);
			createCommitInformation(path, historyFile, 1);
		} else {
			// To get a better feel of all modification that did occurs in the history, get each version of the files that we
			// didn't see in the past.
			int iterationCounter = 1;
			int viewVersion = historyFile.getViewVersion();
			for (int ver = previousVersion + 1; ver <= viewVersion;) {
				// If theirs is a concurrent acces to the file, we need to retry again
				// later.
				try {
					File fromHistory = (File) historyFile.getFromHistoryByVersion(ver);
					if (fromHistory != null) {
						// iterationCounter only serve as an helper in case the multiple
						// version of the same file are done in the past
						createCommitInformation(path, fromHistory, iterationCounter++);
					} else {
						Log.logf("File %s doesn't have a view version #%d, started iteration at version %d", path, ver,
						    previousVersion + 1);
					}
					ver++;
				} catch (ServerException e) {
					Log.logf("Failed to get revision %d of file %s will try again", ver, path);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * This method make sure that the found item is part of the current root we
	 * are scanning. Sometime, when a file is still in the view but is present
	 * somewhere else in the view but not as a sub element of the current root.
	 *
	 * This simple iterative method should be able to confirm the file is a child
	 * of the requested folder.
	 *
	 * @param aFile
	 *          the leaf file we are querying
	 * @param aPotentialParent
	 *          the expected parent to find
	 * @return true if the file is the child of the provided parent.
	 **/
	protected boolean isChildOf(File aFile, Folder aPotentialParent) {
		Folder parent = aFile.getParentFolder();
		int searchedVersion = aPotentialParent.getViewVersion();
		while (parent != null) {
			if (parent.getObjectID() == aPotentialParent.getObjectID()) {
				return parent.getViewVersion() == searchedVersion;
			}
			parent = parent.getParentFolder();
		}
		return false;
	}
	
	/**
	 * This method is used to extract the pathname based on the root of the
	 * conversion that we are doing. The path is created by backtracking the
	 * parent until we find the provided root.
	 * 
	 * @param aFile
	 *          the file we need to find the correct path from
	 * @param root
	 *          the base folder to which we need to backtrack the path
	 * @return a git path to the file
	 */
	private String pathname(File aFile, Folder root) {
		ArrayList<CharSequence> pathComponent = new ArrayList<CharSequence>();
		pathComponent.add(0, aFile.getName());
		Folder parent = aFile.getParentFolder();
		boolean foundCommonParent = false;
		while (parent != null) {
			if (parent.getObjectID() == root.getObjectID()) {
				foundCommonParent = true;
				break;
			}
			pathComponent.add(0, parent.getName());
			parent = parent.getParentFolder();
		}
		if (!foundCommonParent) {
			throw new RuntimeException("Could not find the comment path between " + 
					aFile.getParentFolderHierarchy() + "/" + aFile.getName() + 
					" and " + root.getName());
		}
		StringBuilder joiner = new StringBuilder();
		joiner.append(pathComponent.get(0));
		for (int i = 1; i < pathComponent.size(); i++) {
			joiner.append("/").append(pathComponent.get(i));
		}
		return joiner.toString();
	}

	/**
	 * Determines what happened to files which were in the view during the
	 * previous recursiveFilePopulation run but were not found in the current run.
	 *
	 * Deleted files will be found in the view recycle bin. Otherwise the file was
	 * renamed.
	 *
	 * @param head
	 *          which git head we are refering to
	 * @param root
	 *          base root folder we are importing from
	 */
	private void recoverDeleteInformation(String head, Folder root) {
		RecycleBin recycleBin = null;
		Type fileType = null;
		try {
			recycleBin = recoverRecycleBin(root);
		} catch (java.lang.UnsupportedOperationException e) {
			recycleBin = null;
		}
		if (recycleBin != null) {
			fileType = currentView.getServer().typeForName(recycleBin.getTypeNames().FILE);
		} else {
			fileType = currentView.getServer().typeForName(currentView.getTypeNames().FILE);
		}


		RenameFinder renameFinder = new RenameFinder();
		
		ArrayList<Pair<String, File>> deletedpaths = new ArrayList<Pair<String, File>>(deletedFiles.size());

		// No need to call populateNow on recycleBin.getRootFolder(), as that
		// is done by recycleBin.findItem(). If we called it now, we would
		// incur a long wait which we may not need.
		for(Iterator<String> ith = deletedFiles.iterator(); ith.hasNext(); ) {
			String path = ith.next();
			Integer fileID = helper.getRegisteredFileId(head, path);
			if(null != fileID) {
				File item = null;
				if (null == recycleBin ) {
					item = null;
				} else {
					try {
						item = (File) recycleBin.findItem(fileType, fileID);
					} catch (ServerException e) {
						Log.logf("Coulfd not find deleted files <%s> ID: %d [%s]", path, fileID, e.getMessage());
					}
				}
				if(null != item && item.isDeleted()) {
					deletedpaths.add(new Pair<String, File>(path, item));
					ith.remove();
				} else {
					item = (File) root.getView().findItem(fileType, fileID);
					if(null != item && isChildOf(item, root)) {
						CommitInformation deleteInfo;
						String newPath = pathname(item, root);
						Item renameEventItem = renameFinder.findEventItem(currentView, path, newPath, item,
						    item.getModifiedTime().getLongValue());
						if(null != renameEventItem) {
							if (verbose) {
								Log.logf("Renamed %s -> %s at %s",
								    path, newPath,
										renameEventItem.getModifiedTime());
							}
							deleteInfo = new CommitInformation(renameEventItem.getModifiedTime().createDate(),
									renameEventItem.getModifiedBy(),
									"",
									path);
						} else {
							// if it isn't a rename, must be a move operation.
							if (verbose) {
								Log.logf("No rename event found: %s -> %s something has moved", path, newPath);
							}
							declareEarlierCommitAsMoved(item, newPath);

							// Not sure how this happens, but fill in with the
							// only information we have: the last view time
							// and the last person to modify the item.
							deleteInfo = new CommitInformation(earliestTime, item.getModifiedBy(), "", path);
						}
						deleteInfo.setFileDelete(true);
						ith.remove();
						// Cause old file to be deleted..

						currentCommitList.put(deleteInfo, item);
						// Replace the existing entries for item if they have an earlier timestamp.
						CommitInformation info = new CommitInformation(deleteInfo.getCommitDate(), deleteInfo.getUid(), "", newPath);
						replaceEarlierCommitInfo(info, item, root);
					}
				}
			} else {
				Log.log("Never seen the file " + path + " in " + head);
			}
		}
		
		if (deletedpaths.size() > 0) {
			ItemList items = new ItemList();
			for (int i = 0; i < deletedpaths.size(); i++) {
				items.addItem(deletedpaths.get(i).getSecond());
			}
			PropertyNames propNames = currentView.getPropertyNames();
			String[] populateProps = new String[] {
					propNames.FILE_NAME,
					PropertyNames.ITEM_DELETED_TIME,
					PropertyNames.ITEM_DELETED_USER_ID,
			};
			try {
				items.populateNow(populateProps);
			} catch (com.starbase.starteam.NoSuchPropertyException e) {
				Log.log("Could not populate the deleted files information");
			}
			for (int i = 0; i < deletedpaths.size(); i++) {
				File item = deletedpaths.get(i).getSecond();
				CommitInformation info = new CommitInformation(item.getDeletedTime().createDate(),
						item.getDeletedUserID(),
						"",
						deletedpaths.get(i).getFirst());
				if (verbose) {
					Log.logf("Deleting %s at %d", deletedpaths.get(i).getFirst(), item.getDeletedTime().getLongValue());
				}
				info.setFileDelete(true);
				// Deleted files won't have entries, so add one here to make the delete end up in a commit.
				currentCommitList.put(info, item);
			}
		}
	}

	/**
	 * Retrive the Recycle bin for use by the base strategy.
	 * @param root The root folder from where we start searching for deleted items
	 * @return The recycle Bin instance if supported.
	 */
	protected RecycleBin recoverRecycleBin(Folder root) {
		if (lookIntoRecycleBin) {
			RecycleBin recycleBin;
			recycleBin = root.getView().getRecycleBin();
			recycleBin.setIncludeDeletedItems(true);
			return recycleBin;
		}
		return null;
	}

	/**
	 * Find a earlier commit done that match with the given item and path to
	 * declare it as an unexpected move operation
	 * 
	 * @param item
	 *          The file that was detected as moved somewhere else
	 * @param newPath
	 *          The path to check for
	 */
	private void declareEarlierCommitAsMoved(File item, String newPath) {
		CommitInformation replacement = null;
		File originalValue = null;
		for (Iterator<Map.Entry<CommitInformation, File>> it = currentCommitList.entrySet().iterator(); it.hasNext();) {
			Entry<CommitInformation, File> entry = it.next();
			if (entry.getKey().getPath().equals(newPath) && item.getObjectID() == entry.getValue().getObjectID()) {
				originalValue = entry.getValue();
				// Time need to match with the delete instruction to be combined
				// together
				replacement = new CommitInformation(earliestTime, entry.getKey().getUid(), "Unexpected Move",
				    newPath);
				it.remove();
				break;
			}
		}
		if (replacement != null) {
			currentCommitList.put(replacement, originalValue);
		}

	}

	/**
	 * Remove duplicate commit information from the current commit list in such
	 * preventing sending too much information to git and requiring too much
	 * information to Starteam.
	 *
	 * @param info
	 *          commit that is replacing
	 * @param file
	 *          The file which is being targeted
	 * @param root
	 *          The root folder on which the importation is based on
	 */
	private void replaceEarlierCommitInfo(CommitInformation info, File file, Folder root) {
		String path = pathname(file, root);
		// TODO: a better data structure for fileList would make this more efficient.
		for(Iterator<Map.Entry<CommitInformation, File>> ith = currentCommitList.entrySet().iterator(); ith.hasNext(); ) {
			CommitInformation info2 = ith.next().getKey();
			if (path.equals(info2.getPath()) && info2.getCommitDate().before(info.getCommitDate())) {
				ith.remove();
				return;
			}
		}
	}
	
	/**
	 * Correct the comment based on a set of basic rule against the file. If no
	 * comment are set into the file modification, a generic message will be set.
	 * "Modification without comments"
	 * 
	 * @param historyFile
	 *          Starteam history file on which the comment be generated.
	 * @return a corrected and trimmed comment
	 */
	protected String correctedComment(File historyFile) {
		String comment = historyFile.getComment().trim();
		if(0 == comment.length()) { // if the file doesn't have comment
			if(1 == historyFile.getContentVersion()) {
				comment = historyFile.getDescription().trim();
			}
		  if (0 == comment.length()) { // Still has no comment...
				comment = "Modification without comments";
			}
		} else if(comment.matches("Merge from .*?, Revision .*")) {
			comment = "Merge from unknown branch";
		}
		return comment;
	}
	
	/**
	 * Create a commit information based on the path and history file
	 * 
	 * @param path
	 *          The target path in git repository
	 * @param fileToCommit
	 *          The file the commit is based on
	 * @param iterationCounter
	 *          Counter helper to correct the commit date forward 1 second based
	 *          on the last filePopulation pass
	 */
	protected void createCommitInformation(String path, File fileToCommit, int iterationCounter) {
		String comment = correctedComment(fileToCommit);
		// This is a patchup time to prevent commit jumping up in time between view labels
		Date authorDate = new java.util.Date(fileToCommit.getModifiedTime().getLongValue());
		long timeOfCommit = authorDate.getTime();
		if (earliestTime != null && earliestTime.getTime() >= timeOfCommit) {
			// add offset with last commit to keep order. Based on the last commit
			// from the previous pass + 1 second by counter
			long newTime = earliestTime.getTime() + (1000 * iterationCounter);
			if (verbose) {
				Log.logf("Changing commit time of %s from %d to %d", path, timeOfCommit, newTime);
			}
			timeOfCommit = newTime;
		}
		Date commitDate = new java.util.Date(timeOfCommit);

		CommitInformation info = new CommitInformation(commitDate, fileToCommit.getModifiedBy(), comment, path);
		info.setAuthorDate(authorDate);
		if (verbose) {
			Log.log("Discovered commit <" + info + ">");
		}
		currentCommitList.put(info, fileToCommit);
	}
	
	@Override
	public void setInitialPathList(Set<String> initialPaths) {
		lastFiles.addAll(initialPaths);
	}

	@Override
	public NavigableMap<CommitInformation, File> getListOfCommit() {
		return currentCommitList;
	}

	@Override
	public Set<String> pathToDelete() {
		return deletedFiles;
	}

	@Override
	public void setRepositoryHelper(RepositoryHelper helper) {
		this.helper = helper;
	}

	@Override
	public void setLastCommitTime(Date earliestTime) {
		this.earliestTime = earliestTime;
		if (verbose) {
			Log.log("Set earliest commit to do at " + earliestTime);
		}
	}

	@Override
	public void setVerboseLogging(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public List<String> getLastFiles() {
		ArrayList<String> ret = new ArrayList<String>();
		ret.addAll(lastFiles);
		return ret;
	}

	@Override
	public void setCurrentLabel(Label current) {
		// The base population strategy isn't interested by this added information
	}

	@Override
	public boolean isTagRequired() {
		// Tag are always welcome with the base strategy
		return true;
	}

	@Override
	public void setFileRemoveExtendedInformation(boolean enable) {
		lookIntoRecycleBin = enable;
	}
}
