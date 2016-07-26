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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.ossnoize.git.fastimport.CatBlob;

import org.ossnoize.git.fastimport.Commit;
import org.ossnoize.git.fastimport.DataRef;
import org.ossnoize.git.fastimport.Feature;
import org.ossnoize.git.fastimport.FileDelete;
import org.ossnoize.git.fastimport.FileOperation;
import org.ossnoize.git.fastimport.Sha1Ref;
import org.ossnoize.git.fastimport.enumeration.FeatureType;
import org.sync.ErrorEater;
import org.sync.Log;
import org.sync.RepositoryHelper; 
import org.sync.util.FileUtility;
import org.sync.util.LogEntry;
import org.sync.util.SmallRef;
import org.sync.util.StarteamFileInfo;
import org.sync.util.enumeration.FileStatusStyle;

public class GitHelper extends RepositoryHelper {
	
	private final static String STARTEAMFILEINFODIR = "starteam";
	private final static String STARTEAMFILEINFO = "StarteamFileInfo.gz";

	private String gitExecutable;
	private Process gitFastImport;
	private Thread gitFastImportOutputEater;
	private Thread gitFastImportErrorEater;
	private GitFastImportOutputReader gitResponse;
	private int debugFileCounter = 0;
	private Map<String, Map<String, DataRef>> trackedFiles;
	private boolean isBare;

	public GitHelper(String preferedPath, boolean createRepo, String workingDir) throws Exception {
		if (!findExecutable(preferedPath)) {
			throw new Exception("Git executable not found.");
		}
		if(workingDir == null) {
			workingDir = System.getProperty("user.dir");
		}
		setWorkingDirectory(workingDir, createRepo);
		trackedFiles = Collections.synchronizedMap(new HashMap<String, Map<String, DataRef>>());

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
			process.command(gitExecutable, "init", "--bare");
			process.directory(new File(repositoryDir));
			try {
				Process init = process.start();
				Thread errorEater = new Thread(new ErrorEater(init.getErrorStream(), "init"));
				Thread stdOutEater = new Thread(new ErrorEater(init.getInputStream(), "init", true));
				init.waitFor();
				errorEater.join();
				stdOutEater.join();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}
	
	private void grabTrackedFiles(String head) {
		ProcessBuilder process = new ProcessBuilder();
		process.command(gitExecutable, "ls-tree", "--full-tree" ,"-r", head);
		process.directory(new File(repositoryDir));
		try {
			Process lsFiles = process.start();
			Thread gitQueryWorker = new Thread(new GitLsFilesReader(lsFiles.getInputStream(), head));
			Thread gitErrorStreamEater = new Thread(new ErrorEater(lsFiles.getErrorStream(), "ls-tree"));
			gitQueryWorker.start();
			gitErrorStreamEater.start();
			int returnCode = lsFiles.waitFor();
			gitQueryWorker.join();
			gitErrorStreamEater.join();
			if(0 != returnCode) {
				trackedFiles.put(head, new HashMap<String, DataRef>());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private boolean isValidGitRepository() {
		ProcessBuilder process = new ProcessBuilder();
		process.command(gitExecutable, "branch");
		process.directory(new File(repositoryDir));
		try {
			Process status = process.start();
			Thread statusOut = new Thread(new ErrorEater(status.getInputStream(), true));
			Thread statusErr = new Thread(new ErrorEater(status.getErrorStream(), "branch"));
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
	public boolean isBareRepository() {
		File repo = new File(repositoryDir);
		if(repo.isDirectory()) {
			for(File f : repo.listFiles()) {
				// first possible case, it is a normal repository.
				if(f.getName().equalsIgnoreCase(".git") && f.isDirectory()) {
					File config = new File(f.getAbsolutePath() + File.separator + "config");
					if(config.exists()) {
						return checkIsBare(config);
					}
				}
				if(f.getName().equalsIgnoreCase("config") && f.isFile()) {
					return checkIsBare(f);
				}
			}
		}
		return false;
	}

	private boolean checkIsBare(File config) {
		FileReader freader = null;
		BufferedReader buffer = null;

		try {
			freader = new FileReader(config);
			buffer = new BufferedReader(freader);

			String line;
			while(null != (line = buffer.readLine())) {
				int equalsPosition = line.indexOf('=');
				if(equalsPosition >= 0) {
					String var = line.substring(0, equalsPosition).trim();
					String value = line.substring(equalsPosition+1).trim();

					if(var.equalsIgnoreCase("bare")) {
						return value.equalsIgnoreCase("true");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtility.close(buffer, freader);
		}
		return false;
	}

	@Override
	public void dispose() {
    super.dispose();
		try {
			if(null != gitFastImport) {
				int endCode = gitFastImport.waitFor();
				if(endCode != 0) {
					Log.log("Git fast-import has finished anormally with code:" + endCode);
				}
				gitFastImportOutputEater.join();
				gitFastImportErrorEater.join();
        gitFastImport = null;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isSpecialFile(String filename) {
		File testFile = new File(filename);
		if(testFile.getName().equalsIgnoreCase(".gitignore")) {
			return true;
    } else if (testFile.getName().equalsIgnoreCase(".gitattributes")) {
			return true;
    }
		return false;
	}
	
	@Override
	public void writeCommit(Commit commit) throws IOException {
		super.writeCommit(commit);
		
		String headName = commit.getReference();
		for(FileOperation ops : commit.getFileOperation()) {
			if(null != ops.getMark()) {
				if(!trackedFiles.containsKey(headName)) {
					trackedFiles.put(headName, new HashMap<String, DataRef>());
				}
				trackedFiles.get(headName).put(ops.getPath(), ops.getMark());
			} else if(ops instanceof FileDelete) {
				// This shouldn't be null, but sometimes is. I think the
				// file janitor is getting invoked, too. The problems may
				// have the same cause.
				Map<String, DataRef> headTracked = trackedFiles.get(headName);
				if (headTracked != null) {
					headTracked.remove(ops.getPath());
				}
			}
		}
	}
	
	@Override
	public int gc() {
		int ret = Integer.MIN_VALUE;
		ProcessBuilder process = new ProcessBuilder();
		process.command(gitExecutable, "gc");
		process.directory(new File(repositoryDir));
		try {
			Process gitGc = process.start();
			Thread gitErrorStreamEater = new Thread(new ErrorEater(gitGc.getErrorStream(), "gc"));
			Thread gitQueryWorker = new Thread(new ErrorEater(gitGc.getInputStream(), "gc"));
			gitErrorStreamEater.start();
			gitQueryWorker.start();
			ret = gitGc.waitFor();
			gitErrorStreamEater.join();
			gitQueryWorker.join();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	@Override
	protected OutputStream getFastImportStream() {
		if(null == fastExportOverrideToFile) {
      if(null == gitFastImport) {
        ProcessBuilder process = new ProcessBuilder();
        process.command(gitExecutable, "fast-import", "--done");
        process.directory(new File(repositoryDir));
        try {
          gitFastImport = process.start();
          gitResponse = new GitFastImportOutputReader(gitFastImport.getInputStream());
          gitFastImportOutputEater = new Thread(gitResponse);
          gitFastImportErrorEater = new Thread(new ErrorEater(gitFastImport.getErrorStream(), "fast-import"));
          gitFastImportOutputEater.start();
          gitFastImportErrorEater.start();
          OutputStream out = gitFastImport.getOutputStream();
          // Validate Feature needed;
          Feature feature = new Feature(FeatureType.DateFormat, "raw");
          feature.writeTo(out);
          Feature catBlb = new Feature(FeatureType.CatBlob);
          catBlb.writeTo(out);
        } catch (IOException e) {
          e.printStackTrace();
          return null;
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
		if(null == fastExportOverrideToFile && null != gitFastImport ) {
      try {
        gitFastImport.exitValue();
      } catch (IllegalThreadStateException e) {
        return true;
      }
		}
		return false;
	}

	@Override
	public Set<String> getListOfTrackedFile(String head) {
		if(!trackedFiles.containsKey(head)) {
			grabTrackedFiles(head);
		}
		if(!trackedFiles.containsKey(head)) {
			return null;
		}
		return trackedFiles.get(head).keySet();
	}
	
	@Override
	public Date getLastCommitOfBranch(String branchName) {
		SmallRef to = new SmallRef(branchName);
		return getCommitLog(to.back(1), to).get(0).getTimeOfCommit();
	}
	
	@Override
	public List<LogEntry> getCommitLog(SmallRef from, SmallRef to) {
		ProcessBuilder process = new ProcessBuilder();
		String refs;
		if(null == from) {
			refs = to.getRef();
		} else {
			refs = from.getRef() + ".." + to.getRef();
		}
		process.command(gitExecutable, "log", "--date=iso8601", "--find-renames=75", "--full-index", "--find-copies=75", "--raw", refs);
		process.directory(new File(repositoryDir));
		try {
			Process log = process.start();
			GitLogReader logReader = new GitLogReader(log.getInputStream());
			Thread logReaderThread = new Thread(logReader);
			Thread errorEater = new Thread(new ErrorEater(log.getErrorStream(), "log:=" + to.getRef()));
			logReaderThread.start();
			errorEater.start();
			log.waitFor();
			logReaderThread.join();
			errorEater.join();
			return logReader.getEntries();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<LogEntry> getCommitLog(SmallRef to) {
		return getCommitLog(null, to);
	}

	private File buildStarteamInfoDir() {
		File objDir = new File(repositoryDir + (isBare?"":File.separator + ".git") + File.separator + STARTEAMFILEINFODIR);
		if(!objDir.exists()) {
			objDir.mkdir();
		}
		File objFile = new File(objDir.getAbsolutePath() + File.separator + STARTEAMFILEINFO);
		return objFile;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected boolean loadFileInformation() {
		FileInputStream fin = null;
		ObjectInputStream objin = null;

		File objFile = buildStarteamInfoDir();
		if(!objFile.exists())
			return false;
		
		try {
			fin = new FileInputStream(objFile);
			objin = new ObjectInputStream(fin);
			
			Object tempObject = objin.readObject();
			if(tempObject instanceof Map) {
				fileInformation = (Map<String, Map<String, StarteamFileInfo>>) tempObject;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} finally {
			FileUtility.close(objin, fin);
		}
		return true;
	}

	@Override
	protected void saveFileInformation() {
		if(null != fileInformation) {
			FileOutputStream fout = null;
			ObjectOutputStream objout = null;
			
			File objFile = buildStarteamInfoDir();
			try {
				fout = new FileOutputStream(objFile);
				objout = new ObjectOutputStream(fout);
				
				objout.writeObject(fileInformation);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				FileUtility.close(objout, fout);
			}
		}
	}

	@Override
	public void setWorkingDirectory(String dir, boolean create) {
		super.setWorkingDirectory(dir, create);
		
		if (!repositoryExists(create)) {
			Log.log("Destination repository not found in '" + repositoryDir + "'");
		}
		isBare = isBareRepository();
	}
  
  @Override
  public String getWorkingDirectory() {
    if(isBare) {
      return repositoryDir;
    }
    return repositoryDir + java.io.File.separator + ".git";
  }
  
  @Override
  public void getFileContent(String head, String path, OutputStream whereToStore) {
    if(!trackedFiles.containsKey(head)) {
      grabTrackedFiles(head);
    }
    if(trackedFiles.containsKey(head)) {
      if(trackedFiles.get(head).containsKey(path))
      {
        OutputStream gitFastImportStream = getFastImportStream(); // Make sure fast import process is started
        DataRef fileRef = trackedFiles.get(head).get(path);
        CatBlob request = new CatBlob(fileRef);
        try {
          gitResponse.setCatBlobStream(whereToStore);
          request.writeTo(gitFastImportStream);
          gitResponse.waitForCatBlob();
        } catch (IOException ex) {
          Log.logf("Failed to cat-blob the path <%s> on head <%s>:%s", path, head, ex);
        }
        gitResponse.setCatBlobStream(null);
      }
    }
  }
	
	private class GitLsFilesReader implements Runnable {

		private InputStream input;
		private String head;
		private GitLsFilesReader(InputStream in, String head) {
			this.input = in;
			this.head = head;
		}
		
		@Override
		public void run() {
			synchronized (trackedFiles) {
				InputStreamReader reader = null;
				BufferedReader buffer = null;
				
				try {
					reader = new InputStreamReader(input);
					buffer = new BufferedReader(reader);
					
					String file = null;
					while(null != (file = buffer.readLine())) {
						String path = file.substring(53).trim();
						String sha1 = file.substring(12, 52).trim();
						String type = file.substring(6,12).trim();
						if(type.equalsIgnoreCase("blob")) {
							if(!trackedFiles.containsKey(head)) {
								trackedFiles.put(head, new HashMap<String, DataRef>());
							}
							trackedFiles.get(head).put(path, new Sha1Ref(sha1));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					FileUtility.close(buffer, reader);
				}
			}
		}
		
	}

	private class GitLogReader implements Runnable {
		private static final String dateKey = "Date:";
		private static final String shaKey = "commit";
		private static final String authorKey = "Author:";
		private List<LogEntry> entries;
		private DataRef commitSHA;
		private InputStream logStream;
		
		public GitLogReader(InputStream stream) {
			logStream = stream;
			entries = Collections.synchronizedList(new ArrayList<LogEntry>());
		}

		public List<LogEntry> getEntries() {
			return entries;
		}
		
		@Override
		public void run() {
			SimpleDateFormat dateFormatIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			InputStreamReader reader = null;
			BufferedReader buffer = null;
			try {
				reader = new InputStreamReader(logStream);
				buffer = new BufferedReader(reader);
				LogEntry entry = null;
				String line;
				while((line = buffer.readLine()) != null) {
					if(line.startsWith(dateKey)) {
						String isoDate = line.substring(dateKey.length()).trim();
						try {
							entry.setTimeOfCommit(dateFormatIso.parse(isoDate));
						} catch (ParseException e) {
							throw new Error("The date " + isoDate + " is not a valid (git wise) iso 8601 date");
						}
					} else if (line.startsWith(shaKey)) {
						// Git log entry always start with the commit SHA
						commitSHA = new Sha1Ref(line.substring(shaKey.length()).trim());
						entry = new LogEntry(commitSHA);
						synchronized (entries) {
							entries.notify();
							entries.add(entry);
						}
					} else if (line.startsWith(authorKey)) {
						entry.setAuthor(line.substring(authorKey.length()).trim());
					} else if (line.startsWith(":")) {
						entry.parseStatusLine(FileStatusStyle.GitRaw, line);
					} else {
						entry.appendComment(line.trim() + "\n");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				FileUtility.close(buffer, reader);
			}
		}
	}

	private class GitFastImportOutputReader implements Runnable {
		
    private final Lock outputReaderLock = new ReentrantLock();
    private final Condition outputCatBlobCondition = outputReaderLock.newCondition();
		private InputStream stream;
		private String currentHead;
		private Pattern ls = Pattern.compile("^[0-9]{6} [a-z]+ [0-9a-fA-F]{40}\t.+$");
    private Pattern catblob = Pattern.compile("^[0-9a-fA-F]{40} blob [0-9]+$");
    private OutputStream catBlobStream;
    private boolean catBlobInProgress;

		public GitFastImportOutputReader(InputStream stream) {
			this.stream = stream;
		}
		
		public void setCurrentHead(String currentHead) {
			this.currentHead = currentHead;
		}
    
    public void setCatBlobStream(OutputStream stream) {
      catBlobInProgress = true;
      catBlobStream = stream;
    }
    
    public void waitForCatBlob()
    {
      outputReaderLock.lock();
      try {
        while (catBlobInProgress)
        {
          try {
            outputCatBlobCondition.await();
          } catch (InterruptedException ex) {
            Logger.getLogger(GitHelper.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      } finally {
        outputReaderLock.unlock();
      }
    }
		
		public void run() {
			StringBuilder firstResponse = new StringBuilder(50);
			try {
				int character;
				do {
					character = stream.read();
					while(character >= 0) {
						if(character == '\n') {
							break;
						}
						firstResponse.append((char)character);
						character = stream.read();
					}
					if (ls.matcher(firstResponse).matches()) {
            outputReaderLock.lock();
            try {
              String type = firstResponse.substring(6, 10);
              if(type.equalsIgnoreCase("blob")) {
                String sha1 = firstResponse.substring(13,53);
                String filename = firstResponse.substring(54).trim();
                trackedFiles.get(currentHead).put(filename, new Sha1Ref(sha1));
              }
            } finally {
              outputReaderLock.unlock();
            }
          } else if(catblob.matcher(firstResponse).matches()) {
            outputReaderLock.lock();
            try {
              // In case we have a cat-blob response
              String strSize = firstResponse.substring(45).trim();
              long size;
              try
              {
                size = Long.parseLong(strSize);
              } catch(NumberFormatException ex) {
                continue; // Invalid number go to the next iteration
              }
              // we need to read the full object regardless of it's size
              byte[] buffer = new byte[Math.min((int)size, 4096)];
              int readSize = stream.read(buffer);
              while(readSize >= 0) {
                catBlobStream.write(buffer, 0, readSize);
                size -= readSize;
                if(size <= 0) {
                  break;
                }
                readSize = stream.read(buffer);
              }
              // Notify we are finished
              catBlobInProgress = false;
              outputCatBlobCondition.signalAll();
            } finally {
              outputReaderLock.unlock();
            }
					} else if (firstResponse.toString().trim().length() > 0) {
						System.err.println("Unknown response <" + firstResponse + ">");
					}
					firstResponse.setLength(0);
				} while(character >= 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
