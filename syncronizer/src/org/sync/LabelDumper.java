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
import com.starbase.starteam.Label;
import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.View;
import com.starbase.starteam.ViewConfiguration;
import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

public class LabelDumper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option selectHost = parser.addStringOption('h', "host");
		CmdLineParser.Option selectPort = parser.addIntegerOption('P', "port");
		CmdLineParser.Option selectProject = parser.addStringOption('p', "project");
		CmdLineParser.Option selectUser = parser.addStringOption('U', "user");
		CmdLineParser.Option selectPassword = parser.addStringOption("password");
		CmdLineParser.Option isVerbose = parser.addBooleanOption("verbose");

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
		String user = (String) parser.getOptionValue(selectUser);
		String password = (String) parser.getOptionValue(selectPassword);
		Boolean verboseFlag = (Boolean) parser.getOptionValue(isVerbose);
		boolean verbose = verboseFlag != null && verboseFlag;

		if(host == null || port == null || project == null) {
			printHelp();
			System.exit(3);
		}

		Server starteam = new Server(host, port);
		starteam.connect();

		// try to reconnect at 15 second intervals for 1 hour
		starteam.setAutoReconnectEnabled(true);
		starteam.setAutoReconnectAttempts(60 * 60 / 15);
		starteam.setAutoReconnectWait(15);

		Console con = System.console();
		if(null == user) {
			user = con.readLine("Username:");
		}
		if(null == password) {
			char[] passwordChars = con.readPassword();
			if (passwordChars == null) {
				password = "";
			} else {
				password = new String(passwordChars);
			}
		}

		starteam.logOn(user, password);

		Project p = null;
		for(Project proj : starteam.getProjects()) {
			if(proj.getName().equalsIgnoreCase(project)) {
				p = proj;
				break;
			}
		}

		if(null == p) {
			System.err.println("Project not found: " + project);
			System.exit(3);
		}

		for (View v: p.getViews()) {
			try {
				for (Label l: v.getActiveLabels()) {
					System.out.println("L::" + v.getName() + "::" + l.getName());
				}
			} catch (RuntimeException e) {
				System.err.println("Failed to get labels for " + v.getName() + ": " + e);
			}
		}
		for (View v: p.getViews()) {
			try {
				ViewConfiguration vc = v.getBaseConfiguration();
				if (vc.isLabelBased()) {
					Label label = null;
					for (Label l: v.getParentView().getActiveLabels()) {
						if (l.getID() == vc.getLabelID()) {
							label = l;
							break;
						}
					}
					if (label != null) {
						System.out.printf("LB::%s::%s::%s\n", v.getName(), v.getParentView().getName(), label.getName());
					} else {
						System.err.println("Label not found for " + v.getName());
					}
				} else {
					System.out.println("NLB::" + v.getName());
				}
			} catch (RuntimeException e) {
				System.err.println("Failed to get base labels for " + v.getName() + ": " + e);
			}
		}
	}

	public static void printHelp() {
		System.out.println("-h <host>\t\tDefine on which host the server is hosted");
		System.out.println("-P <port>\t\tDefine the port used to connect to the starteam server");
		System.out.println("-p <project>\t\tSelect the project to import from");
		System.out.println("[-U <user>]\t\tPreselect the user login");
		System.out.println("[--password]\t\tStarTeam password");
		System.out.println("[--verbose]\t\tVerbose output");
		System.out.println("java org.sync.LabelDumper -h localhost -P 23456 -p Alpha -U you");
	}
}
