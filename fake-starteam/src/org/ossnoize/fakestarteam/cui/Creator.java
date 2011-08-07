/*****************************************************************************
 * All public interface based on Starteam API are a property of Borland, 
 * those interface are reproduced here only for testing purpose. You should
 * never use those interface to create a competitive product to the Starteam
 * Server. 
 * 
 * The implementation is given AS-IS and should not be considered a reference 
 * to the API. The behavior on a lots of method and class will not be the
 * same as the real API. The reproduction only seek to mimic some basic 
 * operation. You will not found anything here that can be deduced by using
 * the real API.
 * 
 * Fake-Starteam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *****************************************************************************/
package org.ossnoize.fakestarteam.cui;

import java.io.File;

import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.ProjectProvider;
import org.ossnoize.fakestarteam.SerializableProject;
import org.ossnoize.fakestarteam.SerializableUser;
import org.ossnoize.fakestarteam.SerializableView;
import org.ossnoize.fakestarteam.UserProvider;

import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.User;
import com.starbase.starteam.View;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

public class Creator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option createProject = parser.addStringOption("create-project");
		CmdLineParser.Option listProjects = parser.addBooleanOption('l', "list-projects");
		CmdLineParser.Option createView = parser.addStringOption("create-view");
		CmdLineParser.Option parentView = parser.addStringOption("parent-view");
		CmdLineParser.Option listViews = parser.addStringOption('L', "list-views");
		CmdLineParser.Option createUser = parser.addStringOption("create-user");
		CmdLineParser.Option user = parser.addStringOption('U', "user");
		CmdLineParser.Option password = parser.addStringOption('P', "password");
		CmdLineParser.Option project = parser.addStringOption('p', "project");
		CmdLineParser.Option setPassword = parser.addStringOption("set-password");
		CmdLineParser.Option userFullName = parser.addStringOption("fullname");
		
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
		String createProjectName = (String) parser.getOptionValue(createProject);
		Server server = new Server("localhost", 23456);
		if(null != createProjectName) {
			ProjectProvider.getInstance().createNewProject(server, createProjectName, File.separator);
		}
		String createUserName = (String) parser.getOptionValue(createUser);
		if(null != createUserName) {
			UserProvider.getInstance().createNewUser(createUserName);
		}
		String loginName = (String) parser.getOptionValue(user);
		String passwd = (String) parser.getOptionValue(password);
		server.connect();
		if(0 < server.logOn(loginName, passwd)) {
			System.out.println("Connected to the fake server");
		}
		String setUserPassword = (String) parser.getOptionValue(setPassword);
		String fullName = (String) parser.getOptionValue(userFullName);
		if(null != loginName && (null != setUserPassword || null != fullName)) {
			SerializableUser userObject = UserProvider.getInstance().findUser(loginName);
			if(null != userObject) {
				if(null != setUserPassword)
					userObject.setPassword(setUserPassword);
				if(null != fullName)
					userObject.setName(fullName);
				UserProvider.getInstance().applyChanges();
			} else {
				System.err.println("Could not find the user named " + loginName);
			}
		}
		Boolean listProject = (Boolean) parser.getOptionValue(listProjects);
		if(listProject) {
			for(Project p : server.getProjects()) {
				System.out.println("* " + p.getName());
			}
		}
		String projectName = (String) parser.getOptionValue(project);
		if(null != projectName) {
			Project selected = null;
			for(Project p : server.getProjects()) {
				if(p.getName().equalsIgnoreCase(projectName)) {
					selected = p;
					break;
				}
			}
			if(null == selected) {
				System.out.println("Could not find project named :" + projectName);
			}
			Boolean listView = (Boolean) parser.getOptionValue(listViews);
			if(listView && null != selected) {
				System.out.println(selected.getName());
				for(View v : selected.getViews()) {
					System.out.println("- " + v.getName());
				}
			}
			String createViewNamed = (String) parser.getOptionValue(createView);
			String parentViewNamed = (String) parser.getOptionValue(parentView);
			View from = null;
			if(null == parentViewNamed) {
				from = selected.getDefaultView();
			} else {
				for(View v : selected.getViews()) {
					if(v.getName().equalsIgnoreCase(parentViewNamed)) {
						from = v;
						break;
					}
				}
			}
			if(null != createViewNamed) {
				new SerializableView(from, createViewNamed, createViewNamed, File.separator).update();
			}
		}
	}

	private static void printHelp() {
		System.out.println("-p <project name>");
	}

}
