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
import com.starbase.starteam.Label;
import com.starbase.starteam.View;
import com.starbase.util.OLEDate;

public class Id8 extends CheckInInstruction {

	@Override
	public void checkin(View view) throws IOException {
		Folder root = view.getRootFolder();
		Folder src = findFolderIn(root, "src");
		Folder c = findFolderIn(src, "c");
		Folder wine = findFolderIn(c, "wine");
		Folder msvcp100 = findFolderIn(wine, "msvcp100");

		URL iosVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/6b5e9e3e3f45fa9f700f6d1904c82a21b3ffd0f8:/dlls/msvcp100/ios.c");
		File iosC = findFileIn(msvcp100, "ios.c");
		iosC.checkinFromStream(iosVersion.openStream(), "Fixed stream-off size definition", 0, false);
		
		URL msvcpVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/6b5e9e3e3f45fa9f700f6d1904c82a21b3ffd0f8:/dlls/msvcp100/msvcp.h");
		File msvcpH = findFileIn(msvcp100, "msvcp.h");
		msvcpH.checkinFromStream(msvcpVersion.openStream(), "Fixed stream-off size definition", 0, false);
		
		view.createViewLabel("Check-in Id 8", "Check Id 8 description", new OLEDate(getTimeOfCheckIn() + 1000), true, true);
		Label wineSrc = view.createRevisionLabel("Wine 4", "New version of the files", true);
		wineSrc.attachToFolder(wine, Label.SCOPE_ITEM_AND_CONTENTS);
		wineSrc.update();
	}

	@Override
	public long getTimeOfCheckIn() {
		Calendar time = Calendar.getInstance();
		time.set(2010, 6, 18, 14, 0);
		return time.getTimeInMillis();
	}

}
