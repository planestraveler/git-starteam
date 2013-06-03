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

import java.io.Console;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.View;
import com.starbase.starteam.vts.comm.NetMonitor;
import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

public class MainEntry {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option selectHost = parser.addStringOption('h', "host");
		CmdLineParser.Option selectPort = parser.addIntegerOption('P', "port");
		CmdLineParser.Option selectProject = parser.addStringOption('p', "project");
		CmdLineParser.Option selectView = parser.addStringOption('v', "view");
		CmdLineParser.Option isAllViews = parser.addBooleanOption('A', "all-views");
		CmdLineParser.Option selectTimeBasedImport = parser.addBooleanOption('T', "time-based");
		CmdLineParser.Option selectLabelBasedImport = parser.addBooleanOption('L', "label-based");
		CmdLineParser.Option selectTime = parser.addStringOption('t', "time");
		CmdLineParser.Option selectFolder = parser.addStringOption('f', "folder");
		CmdLineParser.Option selectDomain = parser.addStringOption('d', "domain");
		CmdLineParser.Option isExpandKeywords = parser.addBooleanOption('k', "keyword");
		CmdLineParser.Option selectUser = parser.addStringOption('U', "user");
		CmdLineParser.Option isResume = parser.addBooleanOption('R', "resume");
		CmdLineParser.Option selectHead = parser.addStringOption('H', "head");
		CmdLineParser.Option selectPath = parser.addStringOption('X', "path-to-program");
		CmdLineParser.Option isCreateRepo = parser.addBooleanOption('c', "create-new-repo");
		CmdLineParser.Option selectPassword = parser.addStringOption("password");
		CmdLineParser.Option dumpToFile = parser.addStringOption('D', "dump");
		CmdLineParser.Option selectWorkingFolder = parser.addStringOption('W', "working-folder");
		CmdLineParser.Option isVerbose = parser.addBooleanOption("verbose");
		CmdLineParser.Option isCheckpoint = parser.addBooleanOption("checkpoint");
		CmdLineParser.Option selectSkipViewsPattern = parser.addStringOption("skip-views");

		try {
			parser.parse(args);
		} catch (IllegalOptionValueException e) {
			System.err.println(e.getMessage());
			printHelp();
			System.exit(1);
		} catch (UnknownOptionException e) {
			System.err.println(e.getMessage());
			printHelp();
			System.exit(2);
		}
		
		String host = (String) parser.getOptionValue(selectHost);
		Integer port = (Integer) parser.getOptionValue(selectPort);
		String project = (String) parser.getOptionValue(selectProject);
		String view = (String) parser.getOptionValue(selectView);
		Boolean allViewsFlag = (Boolean) parser.getOptionValue(isAllViews);
		boolean allViews = allViewsFlag != null && allViewsFlag;
		String skipViewsPattern = (String) parser.getOptionValue(selectSkipViewsPattern);
		String time = (String) parser.getOptionValue(selectTime);
		Boolean timeBased = (Boolean) parser.getOptionValue(selectTimeBasedImport);
		Boolean labelBased = (Boolean) parser.getOptionValue(selectLabelBasedImport);
		String folder = (String) parser.getOptionValue(selectFolder);
		String domain = (String) parser.getOptionValue(selectDomain);
		Boolean keyword = (Boolean) parser.getOptionValue(isExpandKeywords);
		String user = (String) parser.getOptionValue(selectUser);
		Boolean resume = (Boolean) parser.getOptionValue(isResume);
		String head = (String) parser.getOptionValue(selectHead);
		String pathToProgram = (String) parser.getOptionValue(selectPath);
		Boolean createNewRepo = (Boolean) parser.getOptionValue(isCreateRepo);
		String password = (String) parser.getOptionValue(selectPassword);
		String dumpTo = (String) parser.getOptionValue(dumpToFile);
		String workingFolder = (String) parser.getOptionValue(selectWorkingFolder);
		Boolean verboseFlag = (Boolean) parser.getOptionValue(isVerbose);
		boolean verbose = verboseFlag != null && verboseFlag;
		Boolean checkpointFlag = (Boolean) parser.getOptionValue(isCheckpoint);
		boolean createCheckpoints = checkpointFlag != null && checkpointFlag;
		
		if(host == null || port == null || project == null || (view == null && !allViews)) {
			printHelp();
			System.exit(3);
		}

		Date date = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(null != time) {
			if(!((null != timeBased && timeBased) ||
				 (null != labelBased && labelBased))) {
				System.out.println("-t option can only be used with -T or -L options.");
				System.exit(3);
			}
			try {
				date = dateFormat.parse(time);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				System.exit(3);
			}
		}

		if(null == domain) {
			domain = "cie.com";
		}

		RepositoryHelperFactory.getFactory().setPreferedPath(pathToProgram);
		RepositoryHelperFactory.getFactory().setCreateRepo((null != createNewRepo));
		if(null != workingFolder) {
			RepositoryHelperFactory.getFactory().setWorkingFolder(workingFolder);
		}

		Server starteam = new Server(host, port);
		starteam.connect();

		Console con = System.console();
		if(null != con && null == user) {
			user = con.readLine("Username:");
		}
		if(null != con && null == password) {
			char[] passwordChars = con.readPassword("Password:");
			if(null != passwordChars) {
				password = new String(passwordChars);
			} else {
				password = null;
			}
		}
		int userid = starteam.logOn(user, password);
		if(userid > 0) {
			// try to reconnect at 15 second intervals for 1 hour
			starteam.setAutoReconnectEnabled(true);
			starteam.setAutoReconnectAttempts(60 * 60 / 15);
			starteam.setAutoReconnectWait(15);

			boolean projectFound = false;
			for(Project p : starteam.getProjects()) {
				if(p.getName().equalsIgnoreCase(project)) {
					projectFound = true;
					if(null == keyword) {
						p.setExpandKeywords(false);
					} else {
						p.setExpandKeywords(true);
					}
					GitImporter importer = new GitImporter(starteam, p);
					try {
						if(null != head) {
							importer.setHeadName(head);
						}
						if(null != resume) {
							importer.setResume(resume);
						}
						if(null != dumpTo) {
							importer.setDumpFile(new File(dumpTo));
						}
						importer.setVerbose(verbose);
						importer.setCreateCheckpoints(createCheckpoints);
						importer.setDomain(domain);

						NetMonitor.onFile(new java.io.File("netmon.out"));

						if(allViews && view == null) {
							importer.generateAllViewsImport(p, null, folder, skipViewsPattern);
						} else {
							boolean viewFound = false;
							for(View v : p.getViews()) {
								if(v.getName().equalsIgnoreCase(view)) {
									viewFound = true;
									if(allViews) {
										importer.generateAllViewsImport(p, v, folder, skipViewsPattern);
									} else if(null != timeBased && timeBased) {
										importer.generateDayByDayImport(v, date, folder);
									} else if (null != labelBased && labelBased) {
										importer.generateByLabelImport(v, date, folder);
									} else {
										importer.generateFastImportStream(v, folder);
									}
									break;
								} else if(verbose) {
									System.err.println("Not view: " + v.getName());
								}
							}
							if (!viewFound) {
								System.err.println("View not found: " + view);
							}
						}
					} finally {
						importer.dispose();
					}
					break;
				} else if(verbose) {
					System.err.println("Not project: " + p.getName());
				}
			}
			if (!projectFound) {
				System.err.println("Project not found: " + project);
			}
		} else {
			System.err.println("Could not log in user: " + user);
		}
	}

	public static void printHelp() {
		System.out.println("-h <host>\t\tDefine on which host the server is hosted");
		System.out.println("-P <port>\t\tDefine the port used to connect to the starteam server");
		System.out.println("-p <project>\t\tSelect the project to import from");
		System.out.println("-v <view>\t\tSelect the view used for importation");
		System.out.println("-A\t\tImport all views");
		System.out.println("-d <domain>\t\tSelect the email domain (format like gmail.com) of the user");
		System.out.println("[-t <time>]\t\tSelect the time (format like \"2012-07-11 23:59:59\") to import from");
		System.out.println("[-f <folder regex>]\t\tSelect the folder (in Java regex format) to import from");
		System.out.println("[-T]\t\t\tDo a day by day importation of the starteam view");
		System.out.println("[-L]\t\t\tDo a label by label importation of the starteam view");
		System.out.println("[-k]\t\t\tSet to enable keyword expansion in text files");
		System.out.println("[-U <user>]\t\tPreselect the user login");
		System.out.println("[-R]\t\t\tResume the file history importation for branch view");
		System.out.println("[-H <head>]\t\tSelect the name of the head to use");
		System.out.println("[-X <path to git>]\tSelect the path to the git executable");
		System.out.println("[-c]\t\t\tCreate a new repository if one does not exist");
		System.out.println("[-W <folder>]\t\tSelect where the repository is located");
		System.out.println("[--password]\t\tStarTeam password");
		System.out.println("[-D <dump file prefix>]\tDump fast-import data to files");
		System.out.println("[--checkpoint]\t\tCreate git fast-import checkpoints after each label");
		System.out.println("[--skip-views <regex>]\t\tSkip views matching regex when using -A");
		System.out.println("[--verbose]\t\tVerbose output");
		System.out.println("java org.sync.MainEntry -h localhost -P 23456 -p Alpha -v MAIN -d email.com -U you");
		
	}
}
