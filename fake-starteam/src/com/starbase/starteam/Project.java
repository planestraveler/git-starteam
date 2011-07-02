package com.starbase.starteam;

import org.ossnoize.fakestarteam.InternalPropertiesProvider;

public class Project extends SimpleTypedResource implements ISecurableObject, ISecurableContainer {

	private volatile Server server;
	private String name;
	private String rootDir;

	protected Project() {
	}
	
	public Project(Server server, String name, String rootDirectory) {
		this.server = server;
		this.name = name;
		this.rootDir = rootDirectory;
	}
	
	public String getName() {
		return name;
	}
	
	public Server getServer() {
		if(null == server) {
			InternalPropertiesProvider.getInstance().getCurrentServer();
		}
		return server;
	}

	public void update() {
	}
}
