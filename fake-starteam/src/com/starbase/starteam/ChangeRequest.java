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

public class ChangeRequest extends Item {

	private final Folder folder;
	
	public ChangeRequest(Folder folder){
		this.folder = folder;
	}
	
	public String get(String key){
		throw new RuntimeException("Not implemented");
	}
	
	public int getCreatedBy(){
		throw new RuntimeException("Not implemented");
	}
	
	public String getComponent(){
		throw new RuntimeException("Not implemented");
	}
	
    public String getDescription(){
    	throw new RuntimeException("Not implemented");
	}
    public String getExternalReference(){
    	throw new RuntimeException("Not implemented");
	}
    
    public String getFlagDisplayName(int i){
    	throw new RuntimeException("Not implemented");
	}
    
    public String getFix(){
    	throw new RuntimeException("Not implemented");
	}
	
	public int getNumber(){
		throw new RuntimeException("Not implemented");
	}
	
	public int getResponsibility(){
		throw new RuntimeException("Not implemented");
	}
	
	public Type getRequestType(){
		throw new RuntimeException("Not implemented");
	}
	
	public String getRequestTypeDisplayName(Type type){
		throw new RuntimeException("Not implemented");
	}
	
	
	public Server getServer(){
		return folder.getView().getServer();
	}
	
	public String getSynopsis(){
		throw new RuntimeException("Not implemented");
	}
  
  public int getStatus() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int getAddressedIn() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public String getStatusDisplayName(int status) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
	
	
}
