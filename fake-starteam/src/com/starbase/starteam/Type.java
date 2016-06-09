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

public class Type extends NamedCacheRef {

	private String typeName;
	private Server server;

	protected Type(String typeName, Server server) {
		this.typeName = typeName;
		this.server = server;
	}
	
	public String getName() {
		return typeName;
	}
	
	public Server getServer() {
		return server;
	}
	
	public boolean isEqualTo(Type type) {
		return typeName.equals(type.typeName);
	}
	
	
	public Property propertyForName(String propertyName){
		throw new RuntimeException("Method not implemented.");
	}
	
	
	@Override
	public boolean equals(Object type) {
		if(type instanceof Type) {
			Type other = (Type)type;
			return other.isEqualTo(this);
		}
		return false;
	}
}
