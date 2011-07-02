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
