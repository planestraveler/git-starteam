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
package org.ossnoize.fakestarteam.builder.checkin;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;

import org.ossnoize.fakestarteam.builder.CheckInInstruction;

import com.starbase.starteam.File;
import com.starbase.starteam.Folder;
import com.starbase.starteam.View;
import com.starbase.util.OLEDate;

public class Id3 extends CheckInInstruction {

	@Override
	public void checkin(View view) throws IOException {
		Folder root = view.getRootFolder();
		
		Folder src = findFolderIn(root, "src");
		Folder java = findFolderIn(src, "java");
		Folder starteam = findFolderIn(java, "starteam");
		
		URL itemVersion = new URL("https://raw.github.com/planestraveler/git-starteam/a6b73f5bd1a02d86cb70898c5bc11b7fc0259bc1/fake-starteam/src/com/starbase/starteam/Item.java");
		File itemClass = findFileIn(starteam, "Item.java");
		itemClass.checkinFromStream(itemVersion.openStream(), "Upgrade the version", 0, false);
		
		URL fileVersion = new URL("https://raw.github.com/planestraveler/git-starteam/e64fc5eaa377107e794c46785370c3132442ac6b/fake-starteam/src/com/starbase/starteam/File.java");
		File fileClass = findFileIn(starteam, "File.java");
		fileClass.checkinFromStream(fileVersion.openStream(), "Upgrade the version", 0, false);
		
		URL folderVersion = new URL("https://raw.github.com/planestraveler/git-starteam/f4fefd317b81a45b0020d875db8623f65d27ec63/fake-starteam/src/com/starbase/starteam/Folder.java");
		File folderClass = new File(starteam);
		folderClass.addFromStream(folderVersion.openStream(),
				"Folder.java",
				"The basic folder in starteam",
				"", 0);
		
		URL folderVersion2 = new URL("https://raw.github.com/planestraveler/git-starteam/182e12e0f5dcb189241ed09cf8ec23a05188baa3/fake-starteam/src/com/starbase/starteam/Folder.java");
		folderClass.checkinFromStream(folderVersion2.openStream(), "Upgrade the version", 0, false);
		
		view.createViewLabel("Check-in Id 3", "Check Id 3 description", new OLEDate(getTimeOfCheckIn() + 1000), true, true);
	}

	@Override
	public long getTimeOfCheckIn() {
		Calendar time = Calendar.getInstance();
		time.set(2010, 6, 13, 12, 00);
		return time.getTimeInMillis();
	}
}
