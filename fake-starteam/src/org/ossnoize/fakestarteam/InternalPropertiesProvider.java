package org.ossnoize.fakestarteam;

import java.io.File;
import java.io.IOException;
import com.starbase.starteam.Server;

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
