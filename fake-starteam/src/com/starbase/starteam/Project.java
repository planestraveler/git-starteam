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
package com.starbase.starteam;

import org.ossnoize.fakestarteam.InternalPropertiesProvider;

public class Project extends SimpleTypedResource implements ISecurableObject, ISecurableContainer {

	private Server server;
	private boolean isNew;
	private boolean expandKeywords;

	protected Project() {
	}
	
	public Project(Server server, String name, String rootDirectory) {
		if(null == server) {
			throw new NullPointerException("Server cannot be null");
		}
		this.server = server;
		this.isNew = true;
	}
	
	public String getName() {
		return null;
	}
	
	public Server getServer() {
		if(null == server) {
			/// on load of the fake server the property will surly be null, get it back just to be sure.
			server = InternalPropertiesProvider.getInstance().getCurrentServer();
		}
		return server;
	}

	public void update() {
		isNew = false;
	}
	
	public boolean isNew() {
		return isNew;
	}
	
	public View[] getViews() {
		// This need to be overriden by the SerializableProject
		return new View[0];
	}
	
	public View getDefaultView() {
		return null;
	}

	public void setExpandKeywords(boolean b) {
		expandKeywords = b;
	}
	
	public boolean isExpandKeywords() {
		return expandKeywords;
	}
}
