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
			objectID = SimpleTypedResourceIDProvider.getProvider().registerNew(this);
			itemProperties.setProperty(propertyKeys.OBJECT_ID, Integer.toString(objectID));
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
				loadFolderProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
