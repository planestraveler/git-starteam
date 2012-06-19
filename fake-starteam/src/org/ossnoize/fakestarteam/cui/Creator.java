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
import java.io.IOException;

import org.ossnoize.fakestarteam.ProjectProvider;
import org.ossnoize.fakestarteam.SerializableUser;
import org.ossnoize.fakestarteam.SerializableView;
import org.ossnoize.fakestarteam.UserProvider;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Item;
import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.Status;
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
		CmdLineParser.Option selectProject = parser.addStringOption('p', "project");
		CmdLineParser.Option createView = parser.addStringOption("create-view");
		CmdLineParser.Option parentView = parser.addStringOption("parent-view");
		CmdLineParser.Option listViews = parser.addBooleanOption('L', "list-views");
		CmdLineParser.Option selectView = parser.addStringOption('v', "view");
		CmdLineParser.Option createUser = parser.addStringOption("create-user");
		CmdLineParser.Option user = parser.addStringOption('U', "user");
		CmdLineParser.Option password = parser.addStringOption('P', "password");
		CmdLineParser.Option setPassword = parser.addStringOption("set-password");
		CmdLineParser.Option userFullName = parser.addStringOption("fullname");
		CmdLineParser.Option createFolders = parser.addStringOption("create-folders");
		CmdLineParser.Option addFileInPath = parser.addStringOption("path");
		CmdLineParser.Option comment = parser.addStringOption('m', "comment");
		CmdLineParser.Option hierarchy = parser.addBooleanOption('H', "hierarchy");
		
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
		Server server = new Server("localhost", 23456);
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
		if(!server.isLoggedOn()) {
			System.err.println("The user " + loginName + " has not logged in.");
			System.exit(3);
		}
		String createProjectName = (String) parser.getOptionValue(createProject);
		if(null != createProjectName) {
			ProjectProvider.getInstance().createNewProject(server, createProjectName, File.separator);
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
		if(null != listProject && listProject) {
			for(Project p : server.getProjects()) {
				System.out.println("* " + p.getName());
			}
		}
		String projectName = (String) parser.getOptionValue(selectProject);
		if(null != projectName) {
			Project selectedProject = null;
			for(Project p : server.getProjects()) {
				if(p.getName().equalsIgnoreCase(projectName)) {
					selectedProject = p;
					break;
				}
			}
			if(null == selectedProject) {
				System.out.println("Could not find project named :" + projectName);
			}
			Boolean listView = (Boolean) parser.getOptionValue(listViews);
			if(null != listView && listView && null != selectedProject) {
				System.out.println(selectedProject.getName());
				for(View v : selectedProject.getViews()) {
					String line = "- " + v.getName();
					if(null != v.getParentView()) {
						line += "(Child of:" + v.getParentView() + ")";
					}
					System.out.println(line);
				}
			}
			String createViewNamed = (String) parser.getOptionValue(createView);
			String parentViewNamed = (String) parser.getOptionValue(parentView);
			View from = null;
			if(null == parentViewNamed) {
				from = selectedProject.getDefaultView();
			} else {
				for(View v : selectedProject.getViews()) {
					if(v.getName().equalsIgnoreCase(parentViewNamed)) {
						from = v;
						break;
					}
				}
			}
			if(null != createViewNamed) {
				new SerializableView(from, createViewNamed, createViewNamed, File.separator).update();
			}
			String selectedViewNamed = (String) parser.getOptionValue(selectView);
			if(null != selectedViewNamed) {
				View selectedView = null;
				for(View v : selectedProject.getViews()) {
					if(v.getName().equalsIgnoreCase(selectedViewNamed)) {
						selectedView = v;
					}
				}
				if(selectedView == null) {
					System.out.println("Could not find the view named " + selectedViewNamed);
				} else {
					String folderToCreate = (String) parser.getOptionValue(createFolders);
					if(null != folderToCreate) {
						String path[] = folderToCreate.split("/");
						Folder stFolder = selectedView.getRootFolder();
						for(String folder : path) {
							boolean found = false;
							for(Folder f : stFolder.getSubFolders()) {
								if(f.getName().equalsIgnoreCase(folder)) {
									stFolder = f;
									found = true;
								}
							}
							if(!found) {
								stFolder = new Folder(stFolder, folder, "");
							}
						}
					}
					String addInPath = (String) parser.getOptionValue(addFileInPath);
					if(null != addInPath) {
						String path[] = addInPath.split("/");
						Folder stFolder = selectedView.getRootFolder();
						boolean found = false;
						for(String folder : path) {
							found = false;
							for(Folder f : stFolder.getSubFolders()) {
								if(f.getName().equalsIgnoreCase(folder)) {
									stFolder = f;
									found = true;
								}
							}
						}
						if(found) {
							String commentToModification = (String) parser.getOptionValue(comment);
							if(null == commentToModification) {
								commentToModification = "";
							}
							String files[] = parser.getRemainingArgs();
							for(String filename : files) {
								File importFile = new File(filename);
								if(importFile.exists()) {
									Item[] fileItems = stFolder.getItems(stFolder.getTypeNames().FILE);
									boolean foundFile = false;
									for(Item i : fileItems) {
										if(i instanceof com.starbase.starteam.File) {
											com.starbase.starteam.File f = (com.starbase.starteam.File) i;
											if(null != f && f.getName().equals(importFile.getName())) {
												foundFile = true;
												try {
													int status = f.getStatus(importFile);
													if(status == Status.MODIFIED) {
														f.checkinFrom(importFile, commentToModification, Item.LockType.UNCHANGED, false, true);
													} else {
														System.out.println("Status of file is not MODIFIED but " + status);
													}
												} catch (IOException e) {
													e.printStackTrace();
												}
											}
										}
									}
									if(!foundFile) {
										try {
											com.starbase.starteam.File f = new com.starbase.starteam.File(stFolder);
											f.add(importFile, importFile.getName(), "", commentToModification, Item.LockType.UNLOCKED, true);
										} catch (IOException e) {
											System.out.println("Could not add file " + importFile);
											e.printStackTrace();
										}
									}
								} else {
									System.out.println("Cannot find the file: " + filename);
								}
							}
						}
						else
						{
							System.out.println("Cannot find the specified path: " + addInPath);
						}
					}
					Boolean showHierarchy = (Boolean)parser.getOptionValue(hierarchy);
					if(null != showHierarchy && showHierarchy.booleanValue()) {
						System.out.println("Showing content of View");
						showContentOfFolder(selectedView.getRootFolder(), "");
					}
				}
			}
		}
	}

	private static void showContentOfFolder(Folder rootFolder, String ident) {
		System.out.println(ident + "+" + rootFolder.getName());
		for(Folder f : rootFolder.getSubFolders()) {
			showContentOfFolder(f, ident + " ");
		}
		for(Item i : rootFolder.getItems(rootFolder.getTypeNames().FILE)) {
			com.starbase.starteam.File f = (com.starbase.starteam.File)i;
			System.out.println(ident + "-" + f.getName() + "\t" + f.getRevisionNumber() + 
					                   "\t" + f.getSizeEx() + "\t" + f.getObjectID());
		}
		System.out.println(ident + ">" + rootFolder.getName());
	}

	private static void printHelp() {
		System.out.println("-p <project name>");
	}

}
