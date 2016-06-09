package org.sync.changerequests;

public class ChangeRequestsHelperFactory {

	private static ChangeRequestsHelperFactory instance = null;
	
	private ChangeRequestsHelper helper;
	
	private ChangeRequestsHelperFactory(){
		
	}
	
	public static ChangeRequestsHelperFactory getFactory(){
		if(instance == null){
			instance = new ChangeRequestsHelperFactory();
		}
		
		return instance;
	}
	
	
	public ChangeRequestsHelper createHelper(){
		if(helper == null){
			helper = new ChangeRequestsHelper();
		}
		
		return helper;
	}
}
