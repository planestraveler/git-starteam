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
package org.ossnoize.fakestarteam.builder;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.ProjectProvider;
import org.ossnoize.fakestarteam.SerializableUser;
import org.ossnoize.fakestarteam.UserProvider;
import org.ossnoize.fakestarteam.exception.InvalidOperationException;

import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.View;

public class StarteamProjectBuilder {
	
	private Server server;
	private String username;
	private String password;
	private int userID;
	private Project buildProject;
	private View currentView;

	private StarteamProjectBuilder() {
		server = new Server("localhost", 23456);
	}

	private void doCheckins(int from, int to) {
		for(int i=from; i <= to; i++) {
			try {
				System.out.println("Doing checkin id " + i + " at " + new java.util.Date());
				@SuppressWarnings("rawtypes")
				Class klass = Class.forName("org.ossnoize.fakestarteam.builder.checkin.Id" + i);
				Object obj = klass.newInstance();
				if (obj instanceof CheckInInstruction) {
					CheckInInstruction instruction = (CheckInInstruction) obj;
					InternalPropertiesProvider.getInstance().setCurrentTime(instruction.getTimeOfCheckIn());
					try {
						instruction.checkin(currentView);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void createProject(String projectName) {
		Calendar projectCreationTime = Calendar.getInstance();
		projectCreationTime.set(2010, 5, 1, 1, 0);
		InternalPropertiesProvider.getInstance().setCurrentTime(projectCreationTime.getTimeInMillis());
		buildProject = ProjectProvider.getInstance().findProject(projectName);
		if(null == buildProject) {
			ProjectProvider.getInstance().createNewProject(server, projectName, File.separator);
			buildProject = ProjectProvider.getInstance().findProject(projectName);
		}
		currentView = buildProject.getDefaultView();
	}
	
	private void connectToServer() {
		server.connect();
		userID = server.logOn(username, password);
		if(userID < 0) {
			throw new InvalidOperationException("User was not created properly");
		}
	}
	
	private void disconnectFromServer() {
		server.disconnect();
	}
	
	/**
	 * Little function that create a test user for use by the builder
	 * @param uid User id to use
	 * @param password The set password
	 */
	private void createTestUser(String uid, String password) {
		this.username = uid;
		this.password = password;
		SerializableUser userObject = UserProvider.getInstance().findUser(uid);
		if(null == userObject)
			UserProvider.getInstance().createNewUser(uid);

		userObject = UserProvider.getInstance().findUser(uid);
		if(null != userObject) {
			userObject.setPassword(password);
			userObject.setName(uid);
			UserProvider.getInstance().applyChanges();
		} else {
			throw new NullPointerException("Could not create find the user in the database");
		}
	}


	/* Static section of the builder */

	private final static int first = 1;
	private final static int last = 10;
	public static void main(String[] args) {
		StarteamProjectBuilder builder = StarteamProjectBuilder.createBuilder();
		builder.createTestUser("Test", "passw0rd");
		builder.connectToServer();
		builder.createProject(args[0]);
		int from = first;
		int to = last;
		if(args.length > 1) {
			from = Integer.parseInt(args[1]);
			if(args.length > 2) {
				to = Integer.parseInt(args[2]);
			}
		}
		builder.doCheckins(from, to);
		builder.disconnectFromServer();
	}

	private static StarteamProjectBuilder createBuilder() {
		return new StarteamProjectBuilder();
	}

}
