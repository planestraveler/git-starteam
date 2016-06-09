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
	
	
}
