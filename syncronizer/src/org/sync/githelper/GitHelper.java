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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import org.sync.RepositoryHelper; 
import org.sync.util.FileUtility;
import org.sync.util.StarteamFileInfo;

import com.starbase.util.MD5;

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
	private Map<String, Map<String, MD5>> md5Cache;
	private MD5 catBlobMD5;
	private boolean isBare;

	public GitHelper(String preferedPath, boolean createRepo) throws Exception {
		if (!findExecutable(preferedPath)) {
			throw new Exception("Git executable not found.");
		}
		setWorkingDirectory(System.getProperty("user.dir"), createRepo);
		trackedFiles = Collections.synchronizedMap(new HashMap<String, Map<String, DataRef>>());
		md5Cache = new HashMap<String, Map<String, MD5>>();

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
			int endCode = gitFastImport.waitFor();
			if(endCode != 0) {
				System.err.println("Git fast-import has finished anormally with code:" + endCode);
			}
			gitFastImportOutputEater.join();
			gitFastImportErrorEater.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
	public void writeCommit(Commit commit) throws IOException {
		super.writeCommit(commit);
		
		String headName = commit.getReference();
		for(FileOperation ops : commit.getFileOperation()) {
			if(null != ops.getMark()) {
				if(!trackedFiles.containsKey(headName)) {
					trackedFiles.put(headName, new HashMap<String, DataRef>());
					md5Cache.put(headName, new HashMap<String, MD5>());
				}
				trackedFiles.get(headName).put(ops.getPath(), ops.getMark());
			} else if(ops instanceof FileDelete) {
				trackedFiles.get(headName).remove(ops.getPath());
			}
			md5Cache.get(headName).remove(ops.getPath());
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
				process.command(gitExecutable, "fast-import");
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
					Feature feature = new Feature(FeatureType.CatBlob);
					feature.writeTo(out);
					feature = new Feature(FeatureType.DateFormat, "raw");
					feature.writeTo(out);
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
	public MD5 getMD5Of(String filename, String head) throws IOException {
		if(!trackedFiles.containsKey(head)) {
			grabTrackedFiles(head);
			md5Cache.put(head, new HashMap<String, MD5>());
		}
		if(!trackedFiles.containsKey(head))
			return new MD5();
		if(trackedFiles.get(head).containsKey(filename)) {
			if(md5Cache.get(head).containsKey(filename)) {
				return md5Cache.get(head).get(filename);
			}
			catBlobMD5 = new MD5();
			OutputStream fastImport = getFastImportStream();
			gitResponse.setCurrentHead(head);
			CatBlob blobRequest = new CatBlob(trackedFiles.get(head).get(filename));
			blobRequest.writeTo(fastImport);
			synchronized (catBlobMD5) {
				if(catBlobMD5.toHexString().equals("00000000000000000000000000000000")) {
					try {
						catBlobMD5.wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			md5Cache.get(head).put(filename, catBlobMD5);
			return catBlobMD5;
		}
		return new MD5();
	}

	@Override
	public Date getLastCommitOfBranch(String branchName) {
		ProcessBuilder process = new ProcessBuilder();
		process.command(gitExecutable, "log", "-1", "--date=iso8601", branchName);
		process.directory(new File(repositoryDir));
		try {
			Process log = process.start();
			GitFirstLogInformationReader logReader = new GitFirstLogInformationReader(log.getInputStream());
			Thread logReaderThread = new Thread(logReader);
			Thread errorEater = new Thread(new ErrorEater(log.getErrorStream(), "log"));
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
			System.err.println("Destination repository not found in '" + repositoryDir + "'");
		}
		isBare = isBareRepository();
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

	private class GitFirstLogInformationReader implements Runnable {
		private static final String dateKey = "Date:";
		private static final String shaKey = "commit";
		private static final String authorKey = "Author:";
		private java.util.Date commitDate;
		private String author;
		private DataRef commitSHA;
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
		
		public DataRef getCommitSHA() {
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
							throw new Error("The date " + isoDate + " is not a valid (git wise) iso 8601 date");
						}
					} else if (line.trim().startsWith(shaKey)) {
						commitSHA = new Sha1Ref(line.substring(shaKey.length()).trim());
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
				FileUtility.close(buffer, reader);
			}
		}
	}

	private class GitFastImportOutputReader implements Runnable {
		
		private InputStream stream;
		private String currentHead;
		private Pattern catBlob = Pattern.compile("^[0-9a-fA-F]{40} blob [0-9]+$");
		private Pattern ls = Pattern.compile("^[0-9]{6} [a-z]+ [0-9a-fA-F]{40}\t.+$");

		public GitFastImportOutputReader(InputStream stream) {
			this.stream = stream;
		}
		
		public void setCurrentHead(String currentHead) {
			this.currentHead = currentHead;
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
					if(catBlob.matcher(firstResponse).matches()) {
						MessageDigest digest = MessageDigest.getInstance("MD5");
						String length = firstResponse.substring(46);
						int qtyOfBytes = Integer.parseInt(length);
						byte[] buffer = new byte[1024];
						int read = stream.read(buffer, 0, Math.min(qtyOfBytes, buffer.length));
						while(qtyOfBytes > 0) {
							digest.update(buffer, 0, read);
							qtyOfBytes -= read;
							if(qtyOfBytes > 0)
								read = stream.read(buffer, 0, Math.min(qtyOfBytes, buffer.length));
						}
						synchronized (catBlobMD5) {
							catBlobMD5.setData(digest.digest());
							catBlobMD5.notify();
						}
					} else if (ls.matcher(firstResponse).matches()) {
						String type = firstResponse.substring(6, 10);
						if(type.equalsIgnoreCase("blob")) {
							String sha1 = firstResponse.substring(13,53);
							String filename = firstResponse.substring(54).trim();
							trackedFiles.get(currentHead).put(filename, new Sha1Ref(sha1));
						}
					}
					firstResponse.setLength(0);
				} while(character >= 0);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
	}
}
