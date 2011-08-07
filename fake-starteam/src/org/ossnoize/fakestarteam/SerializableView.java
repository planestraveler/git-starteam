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

import com.starbase.starteam.View;

public class SerializableView extends View implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4548267790199736069L;
	
	private View parent;
	private String name;
	private String description;
	private String defaultWorkingFolder;
	private int id;

	public SerializableView(View parent, String name, String description, String defaultWorkingFolder) {
		super(parent, name, description, defaultWorkingFolder);
		this.parent = parent;
		this.name = name;
		this.description = description;
		this.defaultWorkingFolder = defaultWorkingFolder;
		this.id = SimpleTypedResourceIDProvider.getProvider().registerNew(this);
	}

	@Override
	public int getID() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}

}
