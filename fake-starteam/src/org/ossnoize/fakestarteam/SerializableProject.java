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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.View;

public class SerializableProject extends Project implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3283403336147311883L;
	private String name;
	private String rootDir;
	private Integer defaultViewId;
	private Map<Integer, SerializableView> views;
	
	protected SerializableProject() {
	}

	public SerializableProject(Server server, String name, String rootDirectory) {
		super(server, name, rootDirectory);
		this.name = name;
		this.rootDir = rootDirectory;
		createDefaultView();
	}
	
	private void createDefaultView() {
		if(views == null) {
			views = new HashMap<Integer, SerializableView>();
		}
		SerializableView main = new SerializableView(null, "MAIN", "Main view", File.separator);
		main.setProject(this);
		defaultViewId = main.getID();
		views.put(main.getID(), main);
	}
	
	void initProject() {
		if(views == null || defaultViewId == 0) {
			createDefaultView();
		}
		for(Map.Entry<Integer, SerializableView> e : views.entrySet()) {
			e.getValue().setProject(this);
		}
	}
	
	@Override
	public void update() {
		if(isNew()) {
			if(ProjectProvider.getInstance().exist(this)) {
				throw new Error("Duplicate project ID");
			}
			super.update();
			ProjectProvider.getInstance().addNewProject(this);
		} else {
			ProjectProvider.getInstance().writeProjectList();
		}
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public View[] getViews() {
		View[] ret = new View[views.size()];
		views.values().toArray(ret);
		return ret;
	}

	@Override
	public View getDefaultView() {
		return views.get(defaultViewId);
	}
}
