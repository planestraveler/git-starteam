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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.SimpleTypedResourceIDProvider;
import org.ossnoize.fakestarteam.exception.InvalidOperationException;

public class Folder extends Item {
	
	private static final String FOLDER_PROPERTIES = "folder.properties";

	public Folder(Server server) {
		throw new UnsupportedOperationException("Unknown goal for this constructor");
	}
	
	protected Folder(View currentView) {
		File serverArchive = InternalPropertiesProvider.getInstance().getFile();
		if(!serverArchive.isDirectory()) {
			throw new UnsupportedOperationException("The archive need to be a directory.");
		}
		try {
			String rootFolder = serverArchive.getCanonicalPath() + File.separator +
					currentView.getProject().getName() + File.separator + currentView.getName();
			holdingPlace = new File(rootFolder);
			if(holdingPlace.exists()) {
				if(holdingPlace.isFile()) {
					holdingPlace.delete();
					holdingPlace.mkdirs();
				}
			} else {
				holdingPlace.mkdirs();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadFolderProperties();
		// We don't want the root folder to have any default name.
		// so overwrite the view name with a blank one and update.
		setName("");
		update();
	}
	
	public void setName(java.lang.String name) {
		if(itemProperties == null) {
			throw new InvalidOperationException("The properties are not initialized");
		}
		itemProperties.setProperty(propertyKeys.FOLDER_NAME, name);
	}
	
	public String getName() {
		if(itemProperties == null) {
			throw new InvalidOperationException("The properties are not initialized");
		}
		return itemProperties.getProperty(propertyKeys.FOLDER_NAME);
	}
	
	@Override
	public void update() {
		if(itemProperties == null) {
			throw new InvalidOperationException("Properties are not initialized yet!!!");
		}
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(holdingPlace.getCanonicalPath() + File.separator + FOLDER_PROPERTIES);
			itemProperties.store(fout, "Folders properties");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fout != null) {
				try {
					fout.close();
				} catch(IOException e) {
				}
			}
		}
	}
	
	private void loadFolderProperties() {
		itemProperties = new Properties();
		FileInputStream fin = null;
		try {
			File folderProperty = new File(holdingPlace.getCanonicalPath() + File.separator + FOLDER_PROPERTIES);
			if(folderProperty.exists()) {
				fin = new FileInputStream(folderProperty);
				itemProperties.load(fin);
				int id = Integer.parseInt(itemProperties.getProperty(propertyKeys.OBJECT_ID));
				SimpleTypedResourceIDProvider.getProvider().registerExisting(id, this);
			} else {
				// initialize the basic properties of the folder.
				itemProperties.setProperty(propertyKeys.OBJECT_ID, 
						Integer.toString(SimpleTypedResourceIDProvider.getProvider().registerNew(this)));
				setName(holdingPlace.getName());
				update();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
