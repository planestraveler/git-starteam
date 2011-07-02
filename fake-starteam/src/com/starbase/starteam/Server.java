package com.starbase.starteam;

import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.ProjectProvider;

public class Server {

	private String Address;
	private int Port;
	private boolean connected;
	private boolean loggedOn;

	public Server(String address, int port) {
		Address = address;
		Port = port;
		InternalPropertiesProvider.getInstance().setCurrentServer(this);
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
	
	public void connect() {
		connected = true;
	}
	
	public boolean isLoggedOn() {
		return loggedOn;
	}
	
	public Project[] getProjects() {
		if(connected && loggedOn) {
			return ProjectProvider.getInstance().listProject();
		}
		return new Project[0];
	}
	
	public int logOn(java.lang.String logOnName, java.lang.String password) {
		return 0;
	}
}
