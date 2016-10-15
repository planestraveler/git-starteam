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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.regex.Pattern;

import org.ossnoize.git.fastimport.Blob;
import org.ossnoize.git.fastimport.Commit;
import org.ossnoize.git.fastimport.Data;
import org.ossnoize.git.fastimport.DataRef;
import org.ossnoize.git.fastimport.FileDelete;
import org.ossnoize.git.fastimport.FileModification;
import org.ossnoize.git.fastimport.FileOperation;
import org.ossnoize.git.fastimport.GitAttributeKind;
import org.ossnoize.git.fastimport.GitAttributes;
import org.ossnoize.git.fastimport.LFSFilePointer;
import org.ossnoize.git.fastimport.Tag;
import org.ossnoize.git.fastimport.enumeration.GitFileType;
import org.ossnoize.git.fastimport.exception.InvalidPathException;
import org.sync.commitstrategy.BasePopulationStrategy;
import org.sync.util.CommitInformation;
import org.sync.util.LabelDateComparator;
import org.sync.util.LogEntry;
import org.sync.util.RevisionDateComparator;
import org.sync.util.SmallRef;
import org.sync.util.TempFileManager;

import com.starbase.starteam.CheckoutManager;
import com.starbase.starteam.File;
import com.starbase.starteam.Folder;
import com.starbase.starteam.Item;
import com.starbase.starteam.Label;
import com.starbase.starteam.Project;
import com.starbase.starteam.PropertyNames;
import com.starbase.starteam.Server;
import com.starbase.starteam.ServerException;
import com.starbase.starteam.User;
import com.starbase.starteam.UserAccount;
import com.starbase.starteam.View;
import com.starbase.starteam.ViewConfiguration;
import com.starbase.util.OLEDate;

public class GitImporter {
	private Server server;
	private Folder folder;
	private long lastModifiedTime = 0;
	private Commit lastCommit; 
	// get the really old time as base information;
	private CommitInformation lastInformation = null;
	private String alternateHead = null;
	private boolean isResume = false;
	private DataRef fromRef = null;
	private boolean verbose = false;
	private boolean createCheckpoints = false;
	private RepositoryHelper repositoryHelper;
	// Use these sets to find all the deleted files.
	private Set<String> lastFiles = new HashSet<String>();
	// tracks which ref each tag points at
	private Map<String, DataRef> tagMarks = new HashMap<String, DataRef>();
	// email domain to use
	private String domain;
	private UserMapping userMapping;
	private long lfsMinimumSize = Long.MAX_VALUE;
	private Pattern lfsRegex;
	private CommitPopulationStrategy CheckoutStrategy;
	
	private String buildDateToken = "build.date=";

	private Set<String> excludedLabelSet = new HashSet<String>();

	public GitImporter(Server s, Project p) {
		server = s;
		repositoryHelper = RepositoryHelperFactory.getFactory().createHelper();
	}

	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setFolder(View view, String folderPattern) {
		if(null != folderPattern) {
			folder = findFirstFolder(view.getRootFolder(), folderPattern);
		} else {
			folder = view.getRootFolder();
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

	public GitAttributes readAttributes(String head)
	{
		GitAttributes ret = new GitAttributes();
		ByteArrayOutputStream tempBuffer = new ByteArrayOutputStream(4096);
		repositoryHelper.getFileContent(head, ".gitattributes", tempBuffer);
		if (tempBuffer.size() == 0) {
			ret.setTopLevelComment("This files was auto-generated by git-starteam");
		} else {
			ret.parse(new ByteArrayInputStream(tempBuffer.toByteArray()));
		}
		return ret;
	}

	public void setDomain(String domain) {
		this.domain = domain;
		if (this.userMapping != null) {
			this.userMapping.setDefaultDomain(this.domain);
		}
	}

	public void setUserMapping(UserMapping userDirectory) {
		this.userMapping = userDirectory;
		if (this.userMapping != null) {
			this.userMapping.setDefaultDomain(this.domain);
		}
	}

	private boolean dontTryServerAdministrationAgain = false;
	public void generateFastImportStream(View view, String folderPath) {
		PropertyNames propNames = view.getPropertyNames();

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

		String head = refName(view.getName());
		if(null != alternateHead) {
			head = refName(alternateHead);
		}

		if(verbose) {
			Log.out("Populating files");
		}
		CheckoutStrategy.filePopulation(head, folder);

		NavigableMap<CommitInformation, File> commitList = CheckoutStrategy.getListOfCommit();
		if(verbose) {
			Log.out("Creating commits");
			// this is helpful for debugging out-of-order commits
			if (!commitList.isEmpty()) {
				CommitInformation earliest = commitList.firstKey();
				CommitInformation latest = commitList.lastKey();
				Log.log("Earliest file: " + earliest.getPath() + " @ " + new java.util.Date(earliest.getTime()));
				Log.log("Latest file: " + latest.getPath() + " @ " + new java.util.Date(latest.getTime()));
			}
		}

		Commit commit = null;
		GitAttributes fattributes = null;
		for (Map.Entry<CommitInformation, File> e : commitList.entrySet()) {
			File f = e.getValue();
			CommitInformation current = e.getKey();
			String userName = "";
			String userEmail = "";
			if (!dontTryServerAdministrationAgain) {
				try {
					UserAccount userAccount = server.getAdministration().findUserAccount(current.getUid());
					userName = userAccount.getName();
					userEmail = userAccount.getEmailAddress();
				} catch (ServerException ex) {
					Log.log("Could not retrieve user from Administration Server. You probably do not have the right");
					dontTryServerAdministrationAgain = true;
				}
			}
			if (dontTryServerAdministrationAgain) {
				User userAccount = server.getUser(current.getUid());
				userName = userAccount.getName();
				userEmail = userMapping.getEmail(userName);
			}
			String path = current.getPath();

			try {
				FileOperation fo;
				if(current.isFileDelete()) {
					fo = new FileDelete();
					// path may be the new file, but current.getPath() is the old deleted path.
					fo.setPath(current.getPath());
					if (null != lastCommit) {
						fattributes = lastCommit.getAttributes();
					}
					boolean justRead = false;
					if (null == fattributes) {
						fattributes = readAttributes(head);
						justRead = true;
					}
					if (fattributes.pathHasAttributes(current.getPath())) {
						fattributes.removePath(current.getPath());
					} else if (justRead) {
						fattributes = null;
					}
					repositoryHelper.unregisterFileId(head, current.getPath());
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
					Data fileData;
					boolean matchPattern = false;
					if(lfsRegex != null) {
						matchPattern = lfsRegex.matcher(current.getPath()).matches();
					}
					if(aFile.length() < lfsMinimumSize && !matchPattern) {
						fileData = new Data(aFile);
					} else {
						try {
							fileData = new LFSFilePointer(repositoryHelper.getWorkingDirectory(), aFile);
							if (null != lastCommit) {
								fattributes = lastCommit.getAttributes();
							}
							if (null == fattributes) {
								fattributes = readAttributes(head);
							}
							fattributes.addAttributeToPath(current.getPath(),
									GitAttributeKind.DiffLfs, GitAttributeKind.FilterLfs, GitAttributeKind.MergeLfs,
									GitAttributeKind.Binary);
						} catch (NoSuchAlgorithmException ex) {
							Log.logf("Failed to add the file as a largefile %s, reverting to basic behavior", ex);
							lfsMinimumSize = Long.MAX_VALUE;
							fileData = new Data(aFile);
						}
					}
					Blob fileToStage = new Blob(fileData);
					repositoryHelper.writeBlob(fileToStage);

					Integer revision = repositoryHelper.getRegisteredFileContentVersion(head, path);
					if(null != revision) {
						if (revision != f.getContentVersion()) {
							repositoryHelper.updateFileVersion(head, path, f.getViewVersion(), f.getContentVersion());
							if(verbose) {
								Log.log("File was updated " + revision + " => " + f.getViewVersion() + ": " + path);
							}
						} else {
							if(verbose) {
								Log.log("File revision : " + revision + " is same: " + path);
							}
						}
					} else {
						if(verbose)	{
							Log.log("No file revision was found for : " + path);
						}
					}

					FileModification fm = new FileModification(fileToStage);
					boolean executable = false;
					try	{
						if (f.get(propNames.FILE_EXECUTABLE) != null)
						{
							executable = (Boolean) f.get(propNames.FILE_EXECUTABLE);
						}
					}
					catch (Exception ex) {
					}
					if(executable) {
						fm.setFileType(GitFileType.Executable);
					} else {
						fm.setFileType(GitFileType.Normal);
					}
					//TODO: Detect that the EOL property from Starteam is properly transfered to a gitattribute
					//TOOD: file to prevent spurious conversion.
					fm.setPath(path);
					fo = fm;
				}
				
				//Need to check this here !
				
				if(null != lastCommit && lastInformation.equivalent(current)) {
					if(lastInformation.getComment().trim().length() == 0 && current.getComment().trim().length() > 0) {
						lastInformation = current;
						lastCommit.setComment(current.getComment());
					}
					lastCommit.addFileOperation(fo);
					if (fattributes != null) {
						lastCommit.setAttributes(fattributes);
					}
				} else {
					java.util.Date commitDate = new java.util.Date(current.getTime());
					// validate that the last commit done wasn't newer than the commit we will be doing
					if (null != lastCommit && lastCommit.getCommitDate().getTime() >= current.getTime()) {
						// we add a seconds for each time we see a commit that is newer or same as the previous commit.
						commitDate = new java.util.Date(lastCommit.getCommitDate().getTime() + 1000);
					}
					commit = new Commit(userName, userEmail, current.getComment(), head, commitDate);
					
					commit.addFileOperation(fo);
					if (fattributes != null) {
						commit.setAttributes(fattributes);
					}
					if(null == lastCommit) {
						if(isResume) {
							commit.resumeOnTopOfRef();
						} else if(null != fromRef) {
							commit.setFromRef(fromRef);
						}
					} else if (!lastCommit.isWritten()) {
						repositoryHelper.writeCommit(lastCommit);
						TempFileManager.getInstance().deleteTempFiles();
						commit.setFromCommit(lastCommit);
					}
					if(fattributes != null) {
						fattributes = null;
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
		// TODO: Simple hack to make deletion of unseen files. Since starteam does
		// TODO: not carry some kind of delete event. (as known from now)
		Set<String> deletedFiles = CheckoutStrategy.pathToDelete();
		if(deletedFiles.size() > 0) {
			try {
				Log.log("Janitor was needed for cleanup");
				java.util.Date janitorTime;
				if(view.getConfiguration().isTimeBased()) {
					janitorTime = new java.util.Date(view.getConfiguration().getTime().getLongValue());
					if (janitorTime.before(lastCommit.getCommitDate())) {
						janitorTime = new java.util.Date(lastCommit.getCommitDate().getTime() + 1000);
					}
				} else {
					janitorTime = lastCommit.getCommitDate(); //At this stage, there should always be a last commit
				}
				commit = new Commit("git-starteam File Janitor",
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
				} else if (!lastCommit.isWritten()){
					repositoryHelper.writeCommit(lastCommit);
					commit.setFromCommit(lastCommit);
					TempFileManager.getInstance().deleteTempFiles();
				}
				for(String path : deletedFiles) {
					if(!repositoryHelper.isSpecialFile(path)) {
						FileOperation fileDelete = new FileDelete();
						try {
							fileDelete.setPath(path);
							commit.addFileOperation(fileDelete);
						} catch (InvalidPathException e1) {
							e1.printStackTrace();
						}
						repositoryHelper.unregisterFileId(head, path);
					}
				}
				lastCommit = commit;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(null != commit) {
			try {
				repositoryHelper.writeCommit(commit);
				TempFileManager.getInstance().deleteTempFiles();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			if (commitList.size() > 0) {
				Log.log("There was no new revision in the starteam view.");
				Log.log("All the files in the repository are at their latest version");
			} else {
				//				Log.log("The starteam view specified was empty.");
			}
		}
		cm = null;
		folder.discardItems(server.getTypeNames().FILE, -1);
	}

	private void finish() {
		while(repositoryHelper.isFastImportRunning()) {
			try {
				Thread.sleep(500); // active wait but leave him a chance to actually finish.
			} catch (InterruptedException ex) {
				Log.log(ex.getMessage());
			}
		}
		repositoryHelper.gc();
	}

	public void setResume(boolean b) {
		isResume = b;
	}

	public void setVerbose(boolean b) {
		if (CheckoutStrategy != null) {
			CheckoutStrategy.setVerboseLogging(b);
		}
		verbose = b;
	}

	public void setCreateCheckpoints(boolean b) {
		createCheckpoints = b;
	}

	private Folder findFirstFolder(Folder f, String folderPattern) {
		// Bread-first search queue
		Deque<Folder> deque = new ArrayDeque<Folder>();
		deque.addLast(f);
		while(!deque.isEmpty()) {
			Folder folder = deque.removeFirst();
			String path = folder.getFolderHierarchy();
			path = path.replace('\\', '/');
			int indexOfFirstPath = path.indexOf('/');

			path = path.substring(indexOfFirstPath + 1);
			if(path.startsWith(folderPattern) && Math.abs(path.length() - folderPattern.length()) < 2) {
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

	public void setHeadName(String head) {
		alternateHead = head;
	}

	public void setDumpFile(java.io.File file) {
		if(null != repositoryHelper) {
			repositoryHelper.setFastExportDumpFile(file);
		} else {
			throw new NullPointerException("Ensure that the helper is correctly started.");
		}
	}

	private Label[] fetchAllViewLabels(View view, String filteringLabelPattern){
		Label[] labels = view.fetchAllLabels();
		List<Label> viewLabels = new ArrayList<Label>();
		
		for(Label label : labels){
			if (label.isViewLabel() && !excludedLabelSet.contains(label.getName())) {
				if (filteringLabelPattern != null && !filteringLabelPattern.isEmpty()){
					if (Pattern.matches(filteringLabelPattern, label.getName())) {
						viewLabels.add(label);
					}
				}
				else{
					viewLabels.add(label);
				}
			}
		}
		
		return viewLabels.toArray(new Label[0]);
	}
	
	private Label[] fetchAllRevisionLabels(View view, String filteringLabelPattern){
		Label[] labels = view.fetchAllLabels();
		List<Label> revisionLabels = new ArrayList<Label>();
		
		for(Label label : labels){
			if (label.isRevisionLabel() && label.getDescription().contains(buildDateToken)
			    && !excludedLabelSet.contains(label.getName())) {
				if (filteringLabelPattern != null && !filteringLabelPattern.isEmpty()) {
					if (Pattern.matches(filteringLabelPattern, label.getName())) {
						revisionLabels.add(label);
					}
				}
				else{
					revisionLabels.add(label);
				}
			}
		}
		
		return revisionLabels.toArray(new Label[0]);
	}	
	
	public void generateByRevisionLabelImport(View view, Date date, String baseFolder, String revisionLabelPattern){
		Label[] revisionLabels = fetchAllRevisionLabels(view, revisionLabelPattern);
	
		String head = view.getName();
		if(null != alternateHead) {
			head = alternateHead;
		}
		
		Arrays.sort(revisionLabels, new RevisionDateComparator());
		int fromLabel = 0;
		
		if(isResume) {
			fromLabel = -1;
			java.util.Date lastCommit = null ;
			if(null != date) {
				lastCommit = date;
			}
			else{
				lastCommit = repositoryHelper.getLastCommitOfBranch(head);
			}
			
			for(int i=0; i < revisionLabels.length; ++i) {
				try {
					if(RevisionDateComparator.getLabelDate(revisionLabels[i]).getLongValue() > lastCommit.getTime()) {
						Log.log("Importing from label <" + revisionLabels[i].getName() + ">");
						fromLabel = i;
						break;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if(-1 == fromLabel) {
				Log.log("No newer label are more recent then last commit made at " + lastCommit);
				return;
			}
		} else if (null != date) {
			for(int i=0; i < revisionLabels.length; ++i) {
				OLEDate revisionDate = null;
				try {
					revisionDate = RevisionDateComparator.getLabelDate(revisionLabels[i]);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				long revisionTime = revisionDate.getLongValue();
				long requestStart = date.getTime();
				if(revisionTime > requestStart) {
					Log.log("Start import from label <" + revisionLabels[i].getName() + "> at time <" + revisionDate.toString() + "> index:" + i);
					fromLabel = i;
					break;
				} else {
					Log.log("Skipping label <" + revisionLabels[i].getName() + "> at time <" + revisionDate.toString() + "> index:" + i);
				}
			}
		}
		
		setFolder(view, baseFolder);
		for(int i=fromLabel; i<revisionLabels.length ; ++i) {
			OLEDate rollbackDate;
			
			try {
				rollbackDate = RevisionDateComparator.getLabelDate(revisionLabels[i]);
			} catch (ParseException e) {
				Log.log("Could not retreive build date from revision label :" + revisionLabels[i]);
				e.printStackTrace();
				continue;
			}
			
			View vc = new View(view, ViewConfiguration.createFromTime(rollbackDate));
			vc.refresh();
			
			if(i == fromLabel && isResume) {
				SmallRef targettedHead = new SmallRef(head);
				List<LogEntry> oldCommits = repositoryHelper.getCommitLog(targettedHead);
				if (oldCommits.size() > 0) {
					CheckoutStrategy.setLastCommitTime(oldCommits.get(0).getTimeOfCommit());
				}
				else {
					if (verbose) {
						Log.log("No parent commit found while resuming importation for <" + head + ">");
					}					
				}
				CheckoutStrategy.setInitialPathList(repositoryHelper.getListOfTrackedFile(head));
			}

			Log.logf("Revision configuration label <%s> : %1.3f %%", revisionLabels[i].getName(),
			    ((double) i / (double) revisionLabels.length) * 100);
			CheckoutStrategy.setCurrentLabel(revisionLabels[i]);
			generateFastImportStream(vc, baseFolder);
			if (CheckoutStrategy.isTagRequired()) {
				writeLabelTag(view, revisionLabels[i]);
			}
			checkpoint();
			vc.close();
		}
	}

	public void generateByLabelImport(View view, Date date, String baseFolder, String labelFilteringPattern, String changeRequestFilePattern) {
		Label[] viewLabels = fetchAllViewLabels(view, labelFilteringPattern);
		String head = view.getName();
		if(null != alternateHead) {
			head = alternateHead;
		}
		Arrays.sort(viewLabels, new LabelDateComparator());
		int fromLabel = 0;
		if(isResume) {
			fromLabel = -1;
			
			java.util.Date lastCommit = null ;
			if(null != date) {
				lastCommit = date;
			}
			else{
				lastCommit = repositoryHelper.getLastCommitOfBranch(head);
			}
			
			for(int i=0; i < viewLabels.length; ++i) {
				if(viewLabels[i].getTime().getLongValue() > lastCommit.getTime()) {
					Log.log("Importing from label <" + viewLabels[i].getName() + ">");
					fromLabel = i;
					break;
				}
			}
			if(-1 == fromLabel) {
				Log.log("No newer view label are more recent then last commit made at " + lastCommit);
				return;
			}
		} else if (null != date) {
			for(int i=0; i < viewLabels.length; ++i) {
				long labelTime = viewLabels[i].getTime().getLongValue();
				long requestStart = date.getTime();
				if(labelTime > requestStart) {
					Log.log("Start import from view label <" + viewLabels[i].getName() + ">");
					fromLabel = i;
					break;
				}
			}
		}
		setFolder(view, baseFolder);
		for(int i=fromLabel; i<viewLabels.length; ++i) {
			View vc = new View(view, ViewConfiguration.createFromLabel(viewLabels[i].getID()));
			if(i == fromLabel && isResume) {
				CheckoutStrategy.setInitialPathList(repositoryHelper.getListOfTrackedFile(head));
				SmallRef targetHead = new SmallRef(head);
				List<LogEntry> lastCommitList = repositoryHelper.getCommitLog(targetHead.back(1), targetHead);
				if (lastCommitList.size() > 0) {
					CheckoutStrategy.setLastCommitTime(lastCommitList.get(0).getTimeOfCommit());
				}
			}
			Log.logf("View configuration label <%s> : %1.3f %%", viewLabels[i].getName(),
			    ((double) i / (double) viewLabels.length) * 100);
			
			CheckoutStrategy.setCurrentLabel(viewLabels[i]);
			generateFastImportStream(vc, baseFolder);
			if (CheckoutStrategy.isTagRequired()) {
				writeLabelTag(view, viewLabels[i]);
			}
			checkpoint();
			vc.close();
		}
	}

	public void generateAllLabelImport(View view, String baseFolder) {
		Label[] viewLabels = view.fetchAllLabels();
		Arrays.sort(viewLabels, new LabelDateComparator());

		for(int i=0; i<viewLabels.length; i++) {
			if (viewLabels[i].isViewLabel() && !excludedLabelSet.contains(viewLabels[i].getName())) {
				View vc = new View(view, ViewConfiguration.createFromLabel(viewLabels[i].getID()));
				Log.logf("View configuration label <%s> (%d/%d)", viewLabels[i].getName(), i+1, viewLabels.length);
				generateFastImportStream(vc, baseFolder);
				writeLabelTag(view, viewLabels[i]);
				checkpoint();

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

			HashSet<String> lastFiles = new HashSet<String>();
			java.util.Date startDate = new java.util.Date(0); // initialize with most
			                                                  // initial date
			if(baseRef != null) {
				View baseView = new View(view.getParentView(), view.getBaseConfiguration());
				setFolder(baseView, baseFolder);
				CommitPopulationStrategy baseStrategy = new BasePopulationStrategy(baseView);
				baseStrategy.filePopulation(alternateHead, folder);
				lastFiles.addAll(baseStrategy.getLastFiles());
				startDate = new java.util.Date(baseStrategy.getListOfCommit().lastKey().getTime());
				baseView.close();
			}
			lastCommit = null;
			isResume = false;

			setCheckoutStrategy(new BasePopulationStrategy(view));
			CheckoutStrategy.setInitialPathList(lastFiles);
			CheckoutStrategy.setLastCommitTime(startDate);
			generateAllLabelImport(view, baseFolder);

			view.close();
		}
	}

	private static String labelRef(View view, Label label) {
		return refName(view.getName() + "/" + label.getName());
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
				.replaceFirst("\\.$", "_")
				// Sanitize "
		    .replaceAll("\"", "_")
		    // Remove Starting dot
		    .replaceAll("^\\.", "_");
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
				repositoryHelper.writeTag(tag);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void checkpoint() {
		if(createCheckpoints) {
			try {
				repositoryHelper.writeCheckpoint();
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
		String head = view.getName();
		if(null != alternateHead) {
			head = alternateHead;
		}
		if(isResume) {
			java.util.Date lastCommit = repositoryHelper.getLastCommitOfBranch(head);
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
				lastFiles.addAll(repositoryHelper.getListOfTrackedFile(head));
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
		java.util.Date previousTime = new java.util.Date(firstTime);
		CheckoutStrategy.setLastCommitTime(previousTime);

		Log.log("Commit from " + new java.util.Date(firstTime) + " to " + new java.util.Date(lastTime));
		Calendar timeIncrement = Calendar.getInstance();
		timeIncrement.setTimeInMillis(firstTime);
		for(;timeIncrement.getTimeInMillis() < lastTime; timeIncrement.add(Calendar.DAY_OF_YEAR, 1)) {
			if(lastTime - timeIncrement.getTimeInMillis() <= day) {
				vc = view;
			} else {
				vc = new View(view, ViewConfiguration.createFromTime(new OLEDate(timeIncrement.getTimeInMillis())));
			}
			Log.log("View Configuration Time: " + timeIncrement.getTime());
			generateFastImportStream(vc, baseFolder);
			vc.close();
		}
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
		repositoryHelper.dispose();
		finish();
	}

	public void setMinimumLFSSize(long startTrackingAtSize) {
		lfsMinimumSize = startTrackingAtSize;
	}

	public void setLFSPattern(Pattern lfsRegexPattern) {
		lfsRegex = lfsRegexPattern;
	}

	public void setCheckoutStrategy(CommitPopulationStrategy strategy) {
		CheckoutStrategy = strategy;
		CheckoutStrategy.setVerboseLogging(verbose);
		CheckoutStrategy.setRepositoryHelper(repositoryHelper);
	}

	/**
	 * Set a list of labels that shall be ignored during the conversion pass. This
	 * is most useful using revision labels when attached files aren't all
	 * expected to be present on selected labels regular expression
	 * 
	 * @param excludedLabels
	 *          An untyped list of labels that shall translatable to string with
	 *          the toString() method.
	 */
	public void setLabelExclusion(@SuppressWarnings("rawtypes") List excludedLabels) {
		// TODO: Change the input parameter to a typed list when the Options API
		// will be changed to more modern apache-common
		for (Object o : excludedLabels) {
			String label = o.toString();
			if (!excludedLabelSet.contains(label)) {
				excludedLabelSet.add(label);
			}
		}
	}
}
