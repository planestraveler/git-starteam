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
		
		if(host == null || port == null || project == null || view == null) {
			printHelp();
			System.exit(3);
		}
		
		try {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
			Server starteam = new Server(host, port);
			starteam.connect();
			if(null == user) {
				System.err.print("Username:");
				user = inputReader.readLine();
			}
			System.err.print("Password:");
			String password = inputReader.readLine();
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printHelp() {
		System.out.println("-h <host>\tDefine on which host the server is hosted");
		System.out.println("-P <port>\tDefine the port used to connect to the starteam server");
		System.out.println("-p <project>\tSelect the project to import from");
		System.out.println("-v <view>\tSelect the view used for importation");
		System.out.println("[-U <user>]\tPreselect the user login");
		System.out.println("[-R]\t\tResume the file history importation");
		System.out.println("java -jar Syncronizer.jar -h localhost -P 23456 -p Alpha -v MAIN -U you");
		
	}
}
