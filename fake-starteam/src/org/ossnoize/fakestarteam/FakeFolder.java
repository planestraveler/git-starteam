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
import java.io.IOException;
import java.util.Properties;

import com.starbase.starteam.Folder;
import com.starbase.starteam.View;

public class FakeFolder extends Folder {
	
	public FakeFolder(View view, int objectID, Folder parent) {
		this.itemProperties = new Properties();
		this.parent = parent;
		this.view = view;
		File serverArchive = InternalPropertiesProvider.getInstance().getStorageLocation();
		if(!serverArchive.exists()) {
			serverArchive.mkdirs();
		}
		if(!serverArchive.isDirectory()) {
			throw new UnsupportedOperationException("The archive need to be a directory.");
		}
		if(0 == objectID) {
			objectID = SimpleTypedResourceIDProvider.getProvider().registerNew(view, this);
			itemProperties.setProperty(propertyKeys.OBJECT_ID, Integer.toString(objectID));
			itemProperties.setProperty(".View ID", Integer.toString(view.getID()));
			setName(view.getName());
			try {
				holdingPlace = new File(serverArchive.getCanonicalPath() + File.separator + objectID);
				validateHoldingPlace();
				update();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				holdingPlace = new File(serverArchive.getCanonicalPath() + File.separator + objectID);
				validateHoldingPlace();
				loadProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
