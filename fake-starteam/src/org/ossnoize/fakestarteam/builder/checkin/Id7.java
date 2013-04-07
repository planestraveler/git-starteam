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
import java.util.Calendar;

import org.ossnoize.fakestarteam.builder.CheckInInstruction;

import com.starbase.starteam.Folder;
import com.starbase.starteam.View;
import com.starbase.util.OLEDate;

public class Id7 extends CheckInInstruction {

	@Override
	public void checkin(View view) throws IOException {
		Folder root = view.getRootFolder();
		Folder src = findFolderIn(root, "src");
		Folder cpp = findFolderIn(src, "cpp");
		Folder wine = findFolderIn(cpp, "wine");
		Folder c = new Folder(src, "c", null);
		wine.moveTo(c);
		
		view.createViewLabel("Check-in Id 7", "Check Id 7 description <Move files around>", new OLEDate(getTimeOfCheckIn() + 1000), true, true);
	}

	@Override
	public long getTimeOfCheckIn() {
		Calendar time = Calendar.getInstance();
		time.set(2010, 6, 17, 12, 35);
		return time.getTimeInMillis();
	}
}
