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
package org.ossnoize.fakestarteam;

import java.io.File;
import com.starbase.starteam.Server;
import com.starbase.util.OLEDate;

public class InternalPropertiesProvider {

	private static InternalPropertiesProvider Reference = null;
	
	public static InternalPropertiesProvider getInstance() {
		if(null == Reference) {
			Reference = new InternalPropertiesProvider();
		}
		return Reference;
	}

	private File Archive;
	private File StorageLocation;
	private Server server;
	private OLEDate currentTime;
	private String baseFolder;
	
	private InternalPropertiesProvider() {
		currentTime = new OLEDate(System.currentTimeMillis());
	}

	private void validateArchive() {
		if(Archive.isFile()) {
			Archive.delete();
		}
		if(!Archive.exists()) {
			Archive.mkdirs();
		}
		if(StorageLocation.isFile()) {
			StorageLocation.delete();
		}
		if(StorageLocation.exists()) {
			StorageLocation.mkdirs();
		}
	}
	
	public File getFile() {
		return Archive;
	}
	
	/**
	 * Return the object storage of this server
	 * @return the File path to the object storage.
	 */
	public File getStorageLocation() {
		return StorageLocation;
	}
	
	public void setBaseStorageLocation(String baseFolder) {
		this.baseFolder = baseFolder;
	}

	public void setCurrentServer(Server server) {
		this.server = server;
		Archive = new File(baseFolder + File.separator + server.getAddress() + "." + server.getPort());
		StorageLocation = new File(baseFolder + File.separator + server.getAddress() + "." + server.getPort() + 
				File.separator + "ObjectStorage");
		validateArchive();
	}
	
	public Server getCurrentServer() {
		return server;
	}
	
	public void setCurrentTime(long time) {
		currentTime = new OLEDate(time);
	}
	
	public OLEDate getCurrentTime() {
		return currentTime;
	}
}
