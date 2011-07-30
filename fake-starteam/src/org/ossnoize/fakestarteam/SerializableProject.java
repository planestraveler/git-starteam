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

import java.io.Serializable;

import com.starbase.starteam.Project;
import com.starbase.starteam.Server;

public class SerializableProject extends Project implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3283403336147311883L;
	
	protected SerializableProject() {
	}

	public SerializableProject(Server server, String name, String rootDirectory) {
		super(server, name, rootDirectory);
	}
	
	@Override
	public void update() {
		if(ProjectProvider.getInstance().exist(this)) {
			ProjectProvider.getInstance().writeProjectList();
		} else {
			ProjectProvider.getInstance().addNewProject(this);
		}
	}

}
