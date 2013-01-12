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

public class Id1 extends CheckInInstruction {

	@Override
	public void checkin(View view) throws IOException {
		Folder root = view.getRootFolder();
		Folder src = new Folder(root, "src", null);
		Folder java = new Folder(src, "java", null);
		Folder cpp = new Folder(src, "cpp", null);
		Folder scons = new Folder(src, "scons", null);
		Folder starteam = new Folder(java, "starteam", null);
		Folder mesa = new Folder(cpp, "mesa", null);
		Folder glsl = new Folder(mesa, "glsl", null);
		
		File projectFile = new File(starteam);
		URL projectVersion = new URL("https://raw.github.com/planestraveler/git-starteam/b3986d6d68b952579941bd943b33c1208fb27f3f/fake-starteam/src/com/starbase/starteam/Project.java");
		projectFile.addFromStream(projectVersion.openStream(),
				"Project.java",
				"This class represent the Project class exist in starteam",
				"This class represent the Project class exist in starteam", 0);
		
		File itemClass = new File(starteam);
		URL itemVersion = new URL("https://raw.github.com/planestraveler/git-starteam/182e12e0f5dcb189241ed09cf8ec23a05188baa3/fake-starteam/src/com/starbase/starteam/Item.java");
		itemClass.addFromStream(itemVersion.openStream(),
				"Item.java",
				"This class represent the Item class exist in starteam",
				"This class represent the Item class exist in starteam", 0);
		
		File fileClass = new File(starteam);
		URL fileVersion = new URL("https://raw.github.com/planestraveler/git-starteam/28e61f5909ed4962af4c3f6a5b06911dd4075f75/fake-starteam/src/com/starbase/starteam/File.java");
		fileClass.addFromStream(fileVersion.openStream(),
				"File.java",
				"This class represent the File class exist in starteam",
				"This class represent the File class exist in starteam", 0);
		
		File mesaSconscript = new File(scons);
		URL sconsVersion = new URL("http://cgit.freedesktop.org/mesa/mesa/plain/SConstruct?id=c61bf363937f40624a5632745630d4f2b9907082");
		mesaSconscript.addFromStream(sconsVersion.openStream(),
				"SConstruct",
				"The initial version of the scconstruct file of mesa",
				"The initial version of the scconstruct file of mesa", 0);
		
		File glslLexer = new File(glsl);
		URL lexerVersion = new URL("http://cgit.freedesktop.org/mesa/mesa/plain/src/glsl/glsl_lexer.ll?id=80ec97af79530dc053770d218cd55ac7dbd74736");
		glslLexer.addFromStream(lexerVersion.openStream(),
				"glsl_lexer.ll",
				"First version of glsl mesa lexer",
				"First version of glsl mesa lexer", 0);
		
		view.createViewLabel("Check-in Id 1", "Check Id 1 description", new OLEDate(getTimeOfCheckIn() + 1000), true, true);
	}

	@Override
	public long getTimeOfCheckIn() {
		Calendar time = Calendar.getInstance();
		time.set(2010, 6, 12, 12, 35);
		return time.getTimeInMillis();
	}

}
