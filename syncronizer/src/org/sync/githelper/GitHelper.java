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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.sync.ErrorEater;
import org.sync.RepositoryHelper;

import java.io.File;
import java.io.FilenameFilter;

public class GitHelper implements RepositoryHelper {

	private Thread gitQueryWorker;
	private Thread gitErrorStreamEater;
	private HashSet<String> trackedFiles;
	private int trackedFilesReturnCode;
	private String gitExecutable;
	private Process gitFastImport;
	private Thread gitFastImportOutputEater;
	private Thread gitFastImportErrorEater;
	private String gitRepositoryDir;
	
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
		File dir = new File(gitRepositoryDir);
		String[] gitDir = dir.list(gitFilter);
		if(null != gitDir) {
			if (gitDir.length == 1) {
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
	public OutputStream getFastImportStream() {
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
	}
	
	public boolean isGitFastImportRunning() {
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

}
