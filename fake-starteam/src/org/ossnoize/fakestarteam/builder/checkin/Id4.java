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

public class Id4 extends CheckInInstruction {

	@Override
	public void checkin(View view) throws IOException {
		Folder root = view.getRootFolder();
		
		Folder src = findFolderIn(root, "src");
		Folder cpp = findFolderIn(src, "cpp");
		Folder mesa = findFolderIn(cpp, "mesa");
		Folder glsl = findFolderIn(mesa, "glsl");
		
		URL lexerVersion = new URL("http://cgit.freedesktop.org/mesa/mesa/plain/src/glsl/glsl_lexer.ll?id=f52660c3dc85632b4dce76d16bf6d78266c35173");
		File glslLexer = findFileIn(glsl, "glsl_lexer.ll");
		glslLexer.checkinFromStream(lexerVersion.openStream(), "Updated lexer", 0, false);
		
		URL parserVersion = new URL("http://cgit.freedesktop.org/mesa/mesa/plain/src/glsl/glsl_parser.yy?id=547212d963c70161915c46d64e8020617199fb8d");
		File glslParser = new File(glsl);
		glslParser.addFromStream(parserVersion.openStream(),
				"glsl_parser.yy", "GLSL Parser", "Parser should always be with lexer", 0);
	}

	@Override
	public long getTimeOfCheckIn() {
		Calendar time = Calendar.getInstance();
		time.set(2010, 6, 14, 9, 15);
		return time.getTimeInMillis();
	}
}
