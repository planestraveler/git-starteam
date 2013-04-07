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

public class Id9 extends CheckInInstruction {

	@Override
	public void checkin(View view) throws IOException {
		Folder root = view.getRootFolder();
		Folder src = findFolderIn(root, "src");
		Folder scons = findFolderIn(src, "scons");
		
		scons.remove();
		

		view.createViewLabel("Check-in Id 9", "Check Id 9 description <remove a folder scons>", new OLEDate(getTimeOfCheckIn() + 1000), true, true);
	}

	@Override
	public long getTimeOfCheckIn() {
		Calendar time = Calendar.getInstance();
		time.set(2010, 6, 19, 15, 15);
		return time.getTimeInMillis();
	}

}
