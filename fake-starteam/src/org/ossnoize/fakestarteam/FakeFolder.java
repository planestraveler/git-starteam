package org.ossnoize.fakestarteam;

import java.io.File;
import java.io.IOException;

import com.starbase.starteam.Folder;
import com.starbase.starteam.View;

public class FakeFolder extends Folder {
	
	public FakeFolder(View view, int objectID) {
		this.parent = null;
		this.view = view;
		File serverArchive = InternalPropertiesProvider.getInstance().getStorageLocation();
		if(!serverArchive.isDirectory()) {
			throw new UnsupportedOperationException("The archive need to be a directory.");
		}
		try {
			holdingPlace = new File(serverArchive.getCanonicalPath() + File.separator + objectID);
			if(holdingPlace.exists() && holdingPlace.isDirectory()) {
				validateHoldingPlace();
				loadFolderProperties();
			}
			setName(view.getName());
			update();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
