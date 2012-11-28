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
package org.sync.githelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ossnoize.fakestarteam.exception.InvalidOperationException;
import org.sync.ErrorEater;
import org.sync.RepositoryHelper; 
import org.sync.util.MD5Builder;
import org.sync.util.StarteamFileInfo;

import com.starbase.util.MD5;

import java.io.FilenameFilter;

public class GitHelper extends RepositoryHelper {
	
	private final static String STARTEAMFILEINFO = "StarteamFileInfo.gz";

	private Thread gitQueryWorker;
	private Thread gitErrorStreamEater;
	private HashSet<String> trackedFiles;
	private int trackedFilesReturnCode;
	private String gitExecutable;
	private Process gitFastImport;
	private Thread gitFastImportOutputEater;
	private Thread gitFastImportErrorEater;
	private String gitRepositoryDir;
	private Map<String, StarteamFileInfo> fileInformation;
	private int debugFileCounter = 0;
	
	private final FilenameFilter gitFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.equals(".git");
		}
	};
	
	public GitHelper(String preferedPath, boolean createRepo) throws Exception {
		gitRepositoryDir = System.getProperty("user.dir");
		
		if (!findExecutable(preferedPath)) {
			throw new Exception("Git executable not found.");
		}
		if (!repositoryExists(createRepo)) {
			throw new Exception("Destination repository not found in '" + gitRepositoryDir + "'");
		}
		
		grabTrackedFiles();
		loadFileInformation();
	}

	private boolean findExecutable(String preferedPath) {
		String os = System.getProperty("os.name");
		if(null != preferedPath) {
			String fileExtension = "";
			if(os.contains("indow")) {
				fileExtension = ".exe";
			}
			File gitExec = new File(preferedPath + File.separator + "git" + fileExtension);
			if(gitExec.exists() && gitExec.canExecute()) {
				try {
					gitExecutable = gitExec.getCanonicalPath();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			if(os.contains("indow")) {
				File gitExec = new File("C:" + File.separator + "Program Files" + File.separator + 
						"Git" + File.separator + "bin" + File.separator + "git.exe");
				if(gitExec.exists() && gitExec.canExecute()) {
					try {
						gitExecutable = gitExec.getCanonicalPath();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					gitExec = new File("C:" + File.separator + "Program Files (x86)" + File.separator + 
							"Git" + File.separator + "bin" + File.separator + "git.exe");
					if(gitExec.exists() && gitExec.canExecute()) {
						try {
							gitExecutable = gitExec.getCanonicalPath();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				gitExecutable = "git";
			}
		}
		return (null != gitExecutable);
	}

	private boolean repositoryExists(boolean create) {
		if (isValidGitRepository()) {
			return true;
		} else if (create) {
			ProcessBuilder process = new ProcessBuilder();
			process.command(gitExecutable, "init");
			process.directory(new File(gitRepositoryDir));
			try {
				process.start();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}
	
	private void grabTrackedFiles() {
		trackedFilesReturnCode = Integer.MAX_VALUE;
		trackedFiles = null;
		ProcessBuilder process = new ProcessBuilder();
		process.command(gitExecutable, "ls-files");
		process.directory(new File(gitRepositoryDir));
		try {
			Process lsFiles = process.start();
			gitQueryWorker = new Thread(new GitLsFilesReader(lsFiles.getInputStream()));
			gitErrorStreamEater = new Thread(new ErrorEater(lsFiles.getErrorStream()));
			gitQueryWorker.start();
			gitErrorStreamEater.start();
			trackedFilesReturnCode = lsFiles.waitFor();
			gitQueryWorker.join();
			gitQueryWorker = null;
			gitErrorStreamEater.join();
			gitErrorStreamEater = null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private boolean isValidGitRepository() {
		ProcessBuilder process = new ProcessBuilder();
		process.command(gitExecutable, "branch");
		process.directory(new File(gitRepositoryDir));
		try {
			Process status = process.start();
			Thread statusOut = new Thread(new ErrorEater(status.getInputStream(), true));
			Thread statusErr = new Thread(new ErrorEater(status.getErrorStream(), true));
			statusOut.start();
			statusErr.start();
			int result = status.waitFor();
			statusOut.join();
			statusErr.join();
			return (result == 0);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getListOfTrackedFile() {
		if(Integer.MAX_VALUE == trackedFilesReturnCode) {
			return null;
		}
		return (Set<String>) trackedFiles.clone();
	}

	@Override
	public boolean isSpecialFile(String filename) {
		File testFile = new File(filename);
		if(testFile.getName().equalsIgnoreCase(".gitignore"))
			return true;
		else if (testFile.getName().equalsIgnoreCase(".gitattributes"))
			return true;
		return false;
	}
	
	@Override
	public void gc() {
		ProcessBuilder process = new ProcessBuilder();
		process.command(gitExecutable, "gc");
		process.directory(new File(gitRepositoryDir));
		try {
			Process gitGc = process.start();
			gitErrorStreamEater = new Thread(new ErrorEater(gitGc.getErrorStream()));
			gitQueryWorker = new Thread(new ErrorEater(gitGc.getInputStream()));
			gitErrorStreamEater.start();
			gitQueryWorker.start();
			gitGc.waitFor();
			gitErrorStreamEater.join();
			gitErrorStreamEater = null;
			gitQueryWorker.join();
			gitQueryWorker = null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public OutputStream getFastImportStream() {
		if(null == fastExportOverrideToFile) {
			if(null == gitFastImport) {
				ProcessBuilder process = new ProcessBuilder();
				process.command(gitExecutable, "fast-import");
				process.directory(new File(gitRepositoryDir));
				try {
					gitFastImport = process.start();
					gitFastImportOutputEater = new Thread(new ErrorEater(gitFastImport.getInputStream()));
					gitFastImportErrorEater = new Thread(new ErrorEater(gitFastImport.getErrorStream()));
					gitFastImportOutputEater.start();
					gitFastImportErrorEater.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return gitFastImport.getOutputStream();
		} else {
			try {
				return new FileOutputStream(fastExportOverrideToFile.getPath() + "." + (debugFileCounter++));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	@Override
	public boolean isFastImportRunning() {
		if(null == fastExportOverrideToFile) {
			try {
				if(0 == gitFastImport.exitValue()) {
					gitFastImport = null;
					return false;
				} else {
					return true;
				}
			} catch (IllegalThreadStateException e) {
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean registerFileId(String filename, int fileId, int fileVersion) {
		if(null == fileInformation) {
			if(!loadFileInformation()) {
				fileInformation = new HashMap<String, StarteamFileInfo>();
			}
			if(!fileInformation.containsKey(filename)) {
				fileInformation.put(filename, new StarteamFileInfo(filename, fileId, fileVersion));
				saveFileInformation();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean updateFileVersion(String filename, int fileVersion) {
		if(null != fileInformation) {
			if(fileInformation.containsKey(filename)) {
				fileInformation.get(filename).setVersion(fileVersion);
				saveFileInformation();
				return true;
			}
		}
		return false;
	}

	@Override
	public void unregisterFileId(String filename) {
		if(null != fileInformation) {
			fileInformation.remove(filename);
			saveFileInformation();
		}
	}

	@Override
	public Integer getRegisteredFileId(String filename) {
		if(null != fileInformation) {
			if(fileInformation.containsKey(filename)) {
				return fileInformation.get(filename).getId();
			}
		} else if (loadFileInformation()) {
			return getRegisteredFileId(filename);
		}
		return null;
	}
	
	@Override
	public Integer getRegisteredFileVersion(String filename) {
		if(null != fileInformation) {
			if(fileInformation.containsKey(filename)) {
				return fileInformation.get(filename).getVersion();
			}
		} else if (loadFileInformation()) {
			return getRegisteredFileVersion(filename);
		}
		return null;
	}
	
	@Override
	public MD5 getMD5Of(String filename, String branchName) {
		ProcessBuilder process = new ProcessBuilder();
		process.command(gitExecutable, "show", branchName + ":" + filename);
		process.directory(new File(gitRepositoryDir));
		try {
			Process show = process.start();
			MD5Builder md5Builder = new MD5Builder(show.getInputStream());
			Thread md5Thread = new Thread(md5Builder);
			Thread errorEater = new Thread(new ErrorEater(show.getErrorStream()));
			md5Thread.start();
			errorEater.start();
			show.waitFor();
			md5Thread.join();
			errorEater.join();
			return md5Builder.getMD5();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Date getLastCommitOfBranch(String branchName) {
		ProcessBuilder process = new ProcessBuilder();
		process.command(gitExecutable, "log", "-1", "--date=iso8601", branchName);
		process.directory(new File(gitRepositoryDir));
		try {
			Process log = process.start();
			GitFirstLogInformationReader logReader = new GitFirstLogInformationReader(log.getInputStream());
			Thread logReaderThread = new Thread(logReader);
			Thread errorEater = new Thread(new ErrorEater(log.getErrorStream()));
			logReaderThread.start();
			errorEater.start();
			log.waitFor();
			logReaderThread.join();
			errorEater.join();
			return logReader.getCommitDate();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private boolean loadFileInformation() {
		FileInputStream fin = null;
		ObjectInputStream objin = null;
		
		File objFile = new File(gitRepositoryDir + File.separator + ".git" + File.separator + STARTEAMFILEINFO);
		if(!objFile.exists())
			return false;
		
		try {
			fin = new FileInputStream(objFile);
			objin = new ObjectInputStream(fin);
			
			Object tempObject = objin.readObject();
			if(tempObject instanceof Map) {
				fileInformation = (Map<String, StarteamFileInfo>) tempObject;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(objin != null) {
				try {
					objin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
	private void saveFileInformation() {
		if(null != fileInformation) {
			FileOutputStream fout = null;
			ObjectOutputStream objout = null;
			
			File objFile = new File(gitRepositoryDir + File.separator + ".git" + File.separator + STARTEAMFILEINFO);
			
			try {
				fout = new FileOutputStream(objFile);
				objout = new ObjectOutputStream(fout);
				
				objout.writeObject(fileInformation);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(null != objout) {
					try {
						objout.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(null != fout) {
					try {
						fout.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private class GitLsFilesReader implements Runnable {

		private InputStream input;
		private GitLsFilesReader(InputStream in) {
			input = in;
		}
		
		@Override
		public void run() {
			HashSet<String> listOfTrackedFiles = new HashSet<String>();
			InputStreamReader reader = null;
			BufferedReader buffer = null;
			
			try {
				reader = new InputStreamReader(input);
				buffer = new BufferedReader(reader);
				
				String file = null;
				while(null != (file = buffer.readLine())) {
					listOfTrackedFiles.add(file);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(null != buffer) {
					try {
						buffer.close();
					} catch (IOException e) {
					}
				}
				if(null != reader) {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
			}
			trackedFiles = listOfTrackedFiles;
		}
		
	}

	private class GitFirstLogInformationReader implements Runnable {
		private static final String dateKey = "Date:";
		private static final String shaKey = "commit";
		private static final String authorKey = "Author:";
		private java.util.Date commitDate;
		private String author;
		private String commitSHA;
		private String comment;
		private InputStream logStream;
		
		public GitFirstLogInformationReader(InputStream stream) {
			logStream = stream;
		}
		
		public Date getCommitDate() {
			return commitDate;
		}

		public String getAuthor() {
			return author;
		}
		
		public String getCommitSHA() {
			return commitSHA;
		}
		
		public String getComment() {
			return comment;
		}
		@Override
		public void run() {
			SimpleDateFormat dateFormatIso =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			InputStreamReader reader = null;
			BufferedReader buffer = null;
			try {
				reader = new InputStreamReader(logStream);
				buffer = new BufferedReader(reader);
				String line;
				while((line = buffer.readLine()) != null) {
					if(line.trim().startsWith(dateKey)) {
						String isoDate = line.substring(dateKey.length()).trim();
						try {
							commitDate = dateFormatIso.parse(isoDate);
						} catch (ParseException e) {
							throw new InvalidOperationException("The date " + isoDate + " is not a valid (git wise) iso 8601 date");
						}
					} else if (line.trim().startsWith(shaKey)) {
						commitSHA = line.substring(shaKey.length()).trim();
						comment = ""; // new Commit
					} else if (line.trim().startsWith(authorKey)) {
						author = line.substring(authorKey.length()).trim();
					} else {
						comment = line.trim() + " ";
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(buffer != null) {
					try {
						buffer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
