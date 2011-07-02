package com.starbase.starteam;

import org.ossnoize.fakestarteam.InternalPropertiesProvider;

public class Server {

	private String Address;
	private int Port;

	public Server(String address, int port) {
		InternalPropertiesProvider.getInstance().setCurrentServer(this);
		Address = address;
		Port = port;
	}
	
	public int getPort() {
		return Port;
	}
	
	public String getAddress() {
		return Address;
	}
	
	public boolean ping() {
		return true;
	}
	
	public Project[] getProjects() {
		return new Project[0];
	}
	
	public int logOn(java.lang.String logOnName, java.lang.String password) {
		return 0;
	}
}
