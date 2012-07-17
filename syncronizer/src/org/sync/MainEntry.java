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

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.View;
import com.starbase.util.OLEDate;

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
		CmdLineParser.Option selectTime = parser.addStringOption('t', "time");
		CmdLineParser.Option selectFolder = parser.addStringOption('f', "folder");
		CmdLineParser.Option selectDomain = parser.addStringOption('d', "domain");
		CmdLineParser.Option selectUser = parser.addStringOption('U', "user");
		CmdLineParser.Option isResume = parser.addBooleanOption('R', "resume");
		CmdLineParser.Option selectHead = parser.addStringOption('H', "head");
		CmdLineParser.Option selectPath = parser.addStringOption('X', "path-to-program");
		CmdLineParser.Option isCreateRepo = parser.addBooleanOption('c', "create-new-repo");
		CmdLineParser.Option selectPassword = parser.addStringOption("password");

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
		String time = (String) parser.getOptionValue(selectTime);
		String folder = (String) parser.getOptionValue(selectFolder);
		String domain = (String) parser.getOptionValue(selectDomain);
		String user = (String) parser.getOptionValue(selectUser);
		Boolean resume = (Boolean) parser.getOptionValue(isResume);
		String head = (String) parser.getOptionValue(selectHead);
		String pathToProgram = (String) parser.getOptionValue(selectPath);
		Boolean createNewRepo = (Boolean) parser.getOptionValue(isCreateRepo);
		String password = (String) parser.getOptionValue(selectPassword);
		
		if(host == null || port == null || project == null || view == null) {
			printHelp();
			System.exit(3);
		}
		
		Date date = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(null != time) {
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

		Server starteam = new Server(host, port);
		starteam.connect();
		Console con = System.console();
		if(null == user) {
			user = con.readLine("Username:");
		}
		if(null == password) {
			password = new String(con.readPassword("Password:"));
		}
		int userid = starteam.logOn(user, password);
		if(userid > 0) {
			for(Project p : starteam.getProjects()) {
				GitImporter g = new GitImporter(starteam, p);
				if(p.getName().equalsIgnoreCase(project)) {
					for(View v : p.getViews()) {
						if(v.getName().equalsIgnoreCase(view)) {
//							View lastView = v;
							long hour = 3600000L; // mSec
							long day = 24 * hour; // 86400000 mSec
//							long firstTime = 1263427200000L; // test
							long firstTime = v.getCreatedTime().getLongValue();
							System.err.println("View Created Time: " + new java.util.Date(firstTime));
							if (null == date){
								date = new java.util.Date(firstTime);
								date.setHours(23);
								date.setMinutes(59);
								date.setSeconds(59);
							}
							firstTime = date.getTime();
							
							View vc;
							GitImporter gi = new GitImporter(starteam, p);
							gi.setFolder(v, folder);
							gi.recursiveLastModifiedTime(gi.getFolder());
							long lastTime = gi.getLastModifiedTime();
							long vcTime;
							System.err.println("Commit from " + new java.util.Date(firstTime) + " to " + new java.util.Date(lastTime));
							g.init();
							for(;firstTime < lastTime; firstTime += day) {
//								System.err.println(firstTime + ":" + lastTime);
								if(lastTime - firstTime <= day) {
									vc = v;
									vcTime = lastTime;
								} else {
									vc = new View(v, v.getConfiguration().createFromTime(new OLEDate(firstTime)));
									vcTime = firstTime;
								}
//								if(vc.isEqualTo(lastView)) {
//									lastView = vc;
//									continue;
//								}
								if(null != head) {
									g.setHeadName(head);
								}
								if(null != resume) {
									g.setResume(resume);
								}
								System.err.println("View Configuration Time: " + new java.util.Date(vcTime));
								g.generateFastImportStream(vc, vcTime, folder, domain);
								vc.discard();
							}
							g.end();
							break;
						}
					}
					break;
				}
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
		System.out.println("-t <time>\t\tSelect the time (format like \"2012-07-11 23:59:59\") to import from");
		System.out.println("-f <folder>\t\tSelect the folder (format like Src/apps/vlc2android/) to import from");
		System.out.println("-d <domain>\t\tSelect the email domain (format like cie.com) of the user");
		System.out.println("[-U <user>]\t\tPreselect the user login");
		System.out.println("[-R]\t\t\tResume the file history importation");
		System.out.println("[-H <head>]\t\tSelect the name of the head to use");
		System.out.println("[-X <path to dvcs>]\tSelect the path where to find the dvcs executable");
		System.out.println("[-c]\t\t\tCreate a new repository if one does not exist");
		System.out.println("java -jar Syncronizer.jar -h localhost -P 23456 -p Alpha -v MAIN -U you");
		
	}
}
