package org.ossnoize.fakestarteam;

import java.io.Serializable;

import com.starbase.starteam.Project;
import com.starbase.starteam.Server;

public class SerializableProject extends Project implements Serializable {

	protected SerializableProject() {
	}
	
	public SerializableProject(Server server, String name, String rootDirectory) {
		super(server, name, rootDirectory);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3283403336147311883L;
	
	@Override
	public void update() {
		if(ProjectProvider.getInstance().exist(this)) {
			ProjectProvider.getInstance().writeProjectList();
		} else {
			ProjectProvider.getInstance().addNewProject(this);
		}
	}

}
