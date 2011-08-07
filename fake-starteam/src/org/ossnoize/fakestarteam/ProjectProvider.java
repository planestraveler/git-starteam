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
package org.ossnoize.fakestarteam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.starbase.starteam.Project;
import com.starbase.starteam.Server;

public class ProjectProvider {

	private static ProjectProvider Reference = null;
	private final static String projectList = "project.list";
	
	public static ProjectProvider getInstance() {
		if(null == Reference) {
			Reference = new ProjectProvider();
		}
		return Reference;
	}
	
	private Map<String, SerializableProject> projects = new HashMap<String, SerializableProject>();
	
	private ProjectProvider() {
		readProjectList();
	}

	public boolean exist(SerializableProject project) {
		return projects.containsKey(project.getName());
	}

	private void readProjectList() {
		projects.clear();
		ObjectInputStream in = null;
		
		try {
			File rootDir = InternalPropertiesProvider.getInstance().getFile();
			File path = new File(rootDir.getCanonicalPath() + File.separator + projectList);
			if(path.exists()) {
				in = new ObjectInputStream(new FileInputStream(path));
				projects = (Map<String, SerializableProject>) in.readObject();
				for(Map.Entry<String, SerializableProject> e : projects.entrySet()) {
					e.getValue().initProject();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(null != in) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public void writeProjectList() {
		ObjectOutputStream out = null;
		
		try {
			File rootDir = InternalPropertiesProvider.getInstance().getFile();
			String path = rootDir.getCanonicalPath() + File.separator + projectList;
			out = new ObjectOutputStream(new FileOutputStream(path));
			out.writeObject(projects);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(null != out) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void addNewProject(SerializableProject project) {
		if(exist(project)) {
			throw new Error("duplicate project name");
		}
		projects.put(project.getName(), project);
		writeProjectList();
	}

	public Project[] listProject() {
		SerializableProject[] ret = new SerializableProject[projects.size()];
		return projects.values().toArray(ret);
	}

	public void createNewProject(Server server, String projectName, String rootDirectory) {
		new SerializableProject(server, projectName, rootDirectory).update();
	}
}
