package org.ossnoize.fakestarteam;

import java.io.File;
import java.io.IOException;
import com.starbase.starteam.Server;

public class InternalPropertiesProvider {

	private static InternalPropertiesProvider Reference = null;
	
	public static InternalPropertiesProvider getInstance() {
		if(null == Reference) {
			Reference = new InternalPropertiesProvider();
		}
		return Reference;
	}

	private File Archive;
	private Server server;
	
	private InternalPropertiesProvider() {
		Archive = new File("localhost");
		validateArchive();
	}

	private void validateArchive() {
		if(Archive.isFile()) {
			Archive.delete();
			Archive.mkdir();
		}
		if(!Archive.exists()) {
			Archive.mkdirs();
		}
	}
	
	public File getFile() {
		return Archive;
	}

	public void setCurrentServer(Server server) {
		this.server = server;
		Archive = new File(server.getAddress() + "." + server.getPort());
		validateArchive();
	}
	
	public Server getCurrentServer() {
		return server;
	}
}
