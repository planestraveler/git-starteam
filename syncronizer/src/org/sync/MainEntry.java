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

import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.View;

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
				if(p.getName().equalsIgnoreCase(project)) {
					for(View v : p.getViews()) {
						if(v.getName().equalsIgnoreCase(view)) {
							GitImporter g = new GitImporter(starteam, p, v);
							if(null != head) {
								g.setHeadName(head);
							}
							if(null != resume) {
								g.setResume(resume);
							}
							g.generateFastImportStream();
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
		System.out.println("[-U <user>]\t\tPreselect the user login");
		System.out.println("[-R]\t\t\tResume the file history importation");
		System.out.println("[-H <head>]\t\tSelect the name of the head to use");
		System.out.println("[-X <path to dvcs>]\tSelect the path where to find the dvcs executable");
		System.out.println("[-c]\t\t\tCreate a new repository if one does not exist");
		System.out.println("java -jar Syncronizer.jar -h localhost -P 23456 -p Alpha -v MAIN -U you");
		
	}
}
