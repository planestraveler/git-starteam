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

import com.starbase.starteam.User;

public class SerializableUser extends User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6222337883132570087L;
	private String uid;
	private int id;
	private String name;
	private String passwd;

	public SerializableUser(String uid, int id) {
		this.uid = uid;
		this.id = id;
	}
	
	public String getUID() {
		return uid;
	}
	
	@Override
	public int getID() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isCorrectPassword(String passwd) {
		return this.passwd == passwd;
	}
	
	public void setPassword(String passwd) {
		this.passwd = passwd;
	}
}
