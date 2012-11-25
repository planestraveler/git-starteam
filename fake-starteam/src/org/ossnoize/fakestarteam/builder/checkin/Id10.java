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

import org.ossnoize.fakestarteam.builder.CheckInInstruction;

import com.starbase.starteam.File;
import com.starbase.starteam.Folder;
import com.starbase.starteam.View;

public class Id10 extends CheckInInstruction {

	@Override
	public void checkin(View view) throws IOException {
		Folder root = view.getRootFolder();
		Folder src = findFolderIn(root, "src");
		Folder java = findFolderIn(src, "java");
		Folder starteam = findFolderIn(java, "starteam");
		
		URL projectVersion = new URL("https://raw.github.com/planestraveler/git-starteam/71e3bcd28e638ea80097b24a0291a5917afa4511/fake-starteam/src/com/starbase/starteam/Project.java");
		File projectClass = findFileIn(starteam, "Project.java");
		projectClass.checkinFromStream(projectVersion.openStream(), "Improve Logic of project creation", 0, false);
		
		URL itemVersion = new URL("https://raw.github.com/planestraveler/git-starteam/139b8e2ac0bd455b83a4c18ac3caf947518f8c5f/fake-starteam/src/com/starbase/starteam/Item.java");
		File itemClass = findFileIn(starteam, "Item.java");
		itemClass.checkinFromStream(itemVersion.openStream(), "Added getParentFolder property", 0, false);
		
		URL fileVersion = new URL("https://raw.github.com/planestraveler/git-starteam/837877c31822cf4dd5d4ee28711aba3569aad955/fake-starteam/src/com/starbase/starteam/File.java");
		File fileClass = findFileIn(starteam, "File.java");
		fileClass.checkinFromStream(fileVersion.openStream(), "Load from history and Return it", 0, false);
		
		URL folderVersion = new URL("https://raw.github.com/planestraveler/git-starteam/6c4ed90eed3f1be9f8de16261bff8c2308b50adc/fake-starteam/src/com/starbase/starteam/Folder.java");
		File folderClass = findFileIn(starteam, "Folder.java");
		folderClass.checkinFromStream(folderVersion.openStream(), "Added subfolder listing capacity", 0, false);
		
		URL viewVersion = new URL("https://raw.github.com/planestraveler/git-starteam/ae3b555c8f5df0e7db03022ae6d32fa51b7a7727/fake-starteam/src/com/starbase/starteam/View.java");
		File viewClass = new File(starteam);
		viewClass.addFromStream(viewVersion.openStream(), "View.java", "Class reprensenting the view of starteam", "", 0);
		
	}

}
