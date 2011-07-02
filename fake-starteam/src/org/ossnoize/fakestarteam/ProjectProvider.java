package org.ossnoize.fakestarteam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

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

	public void readProjectList() {
		projects.clear();
		ObjectInputStream in = null;
		
		try {
			File rootDir = InternalPropertiesProvider.getInstance().getFile();
			File path = new File(rootDir.getCanonicalPath() + File.separator + projectList);
			if(path.exists()) {
				in = new ObjectInputStream(new FileInputStream(path));
				projects = (Map<String, SerializableProject>) in.readObject();
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
		readProjectList();
		if(exist(project)) {
			throw new Error("duplicate project name");
		}
		projects.put(project.getName(), project);
		writeProjectList();
	}
}
