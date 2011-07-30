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
import org.ossnoize.fakestarteam.SerializableProject;
import org.ossnoize.fakestarteam.UserProvider;

import com.starbase.starteam.Project;
import com.starbase.starteam.Server;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

public class Creator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option createProject = parser.addStringOption("newProject");
		CmdLineParser.Option createView = parser.addStringOption("newView");
		CmdLineParser.Option parentView = parser.addStringOption("parentView");
		CmdLineParser.Option createUser = parser.addStringOption("createUser");
		CmdLineParser.Option user = parser.addStringOption('U', "user");
		CmdLineParser.Option password = parser.addStringOption("passwd");
		CmdLineParser.Option userFullName = parser.addStringOption("fullName");
		
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
		String projectName = (String) parser.getOptionValue(createProject);
		Server server = new Server("localhost", 23456);
		if(null != projectName) {
			Project prj = new SerializableProject(server, projectName, File.separator);
			prj.update();
		}
		String username = (String) parser.getOptionValue(createUser);
		if(null != username) {
			UserProvider.getInstance().createNewUser(username);
		}
	}

	private static void printHelp() {
		System.out.println("-P <project name>");
	}

}
