package com.starbase.starteam;

public class TypeNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2986121522851909613L;
	private String typeName;

	public TypeNotFoundException(String typeName) {
		this.typeName = typeName;
	}
	
	public String getTypeName() {
		return typeName;
	}
}
