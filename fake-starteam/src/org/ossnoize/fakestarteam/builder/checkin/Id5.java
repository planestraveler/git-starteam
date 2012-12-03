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

public class Id5 extends CheckInInstruction {

	@Override
	public void checkin(View view) throws IOException {
		Folder root = view.getRootFolder();
		Folder src = findFolderIn(root, "src");
		Folder cpp = findFolderIn(src, "cpp");
		Folder wine = new Folder(cpp, "wine", null);
		Folder msvcp100 = new Folder(wine, "msvcp100", null);
		
		URL makefileVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/dfbf47b00c80932af579df771d45373638332a84:/dlls/msvcp100/Makefile.in");
		File makefileIn = new File(msvcp100);
		makefileIn.addFromStream(makefileVersion.openStream(),
				"Makefile.in",
				"Basic construction file",
				"", 0);
		
		URL msvcp100Version = new URL("http://source.winehq.org/git/wine.git/blob_plain/dfbf47b00c80932af579df771d45373638332a84:/dlls/msvcp100/msvcp100.c");
		File msvcp100C = new File(msvcp100);
		msvcp100C.addFromStream(msvcp100Version.openStream(),
				"msvcp100.c",
				"Stub of msvcp100 dlls",
				"", 0);
	}

	@Override
	public long getTimeOfCheckIn() {
		Calendar time = Calendar.getInstance();
		time.set(2010, 6, 15, 10, 0);
		return time.getTimeInMillis();
	}

}
