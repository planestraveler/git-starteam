package com.starbase.starteam;

public class User extends CacheRef {

	public String getName() {
		return null;
	}
	
	public int getID() {
		return 0;
	}
	
	@Override
	public int hashCode() {
		return getID();
	}
}
