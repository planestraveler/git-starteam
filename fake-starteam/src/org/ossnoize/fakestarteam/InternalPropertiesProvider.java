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
	
	public void setFileName(String filename) throws IOException {
		Archive = new File(filename);
		if(!Archive.exists()) {
			Archive.mkdirs();
		}
	}

	public void setCurrentServer(Server server) {
		this.server = server;
	}
	
	public Server getCurrentServer() {
		return server;
	}
}
