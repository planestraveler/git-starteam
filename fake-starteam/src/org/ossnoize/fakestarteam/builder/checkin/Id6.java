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

public class Id6 extends CheckInInstruction {

	@Override
	public void checkin(View view) throws IOException {
		Folder root = view.getRootFolder();
		Folder src = findFolderIn(root, "src");
		Folder cpp = findFolderIn(src, "cpp");
		Folder wine = findFolderIn(cpp, "wine");
		Folder msvcp100 = findFolderIn(wine, "msvcp100");
		
		URL makefileVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/4fe6e583752915161142089c3c0d916eefde5ef8:/dlls/msvcp100/Makefile.in");
		File makefileIn = findFileIn(msvcp100, "Makefile.in");
		makefileIn.checkinFromStream(makefileVersion.openStream(), "Copy files from msvcp90", 0, false);
		
		URL cxxVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/4fe6e583752915161142089c3c0d916eefde5ef8:/dlls/msvcp100/cxx.h");
		File cxxH = new File(msvcp100);
		cxxH.addFromStream(cxxVersion.openStream(),
				"cxx.h", 
				"Copy files from msvcp90",
				"Copy files from msvcp90", 0);
		
		URL exceptionVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/4fe6e583752915161142089c3c0d916eefde5ef8:/dlls/msvcp100/exception.c");
		File exceptionC = new File(msvcp100);
		exceptionC.addFromStream(exceptionVersion.openStream(),
				"exception.c",
				"Copy files from msvcp90",
				"Copy files from msvcp90", 0);
		
		URL iosVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/4fe6e583752915161142089c3c0d916eefde5ef8:/dlls/msvcp100/ios.c");
		File iosC = new File(msvcp100);
		iosC.addFromStream(iosVersion.openStream(),
				"ios.c",
				"Copy files from msvcp90",
				"Copy files from msvcp90", 0);
		
		URL localeVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/4fe6e583752915161142089c3c0d916eefde5ef8:/dlls/msvcp100/locale.c");
		File localeC = new File(msvcp100);
		localeC.addFromStream(localeVersion.openStream(),
				"locale.c",
				"Copy files from msvcp90",
				"Copy files from msvcp90", 0);
		
		URL mathVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/4fe6e583752915161142089c3c0d916eefde5ef8:/dlls/msvcp100/math.c");
		File mathC = new File(msvcp100);
		mathC.addFromStream(mathVersion.openStream(),
				"math.c",
				"Copy files from msvcp90",
				"Copy files from msvcp90", 0);
		
		URL memoryVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/4fe6e583752915161142089c3c0d916eefde5ef8:/dlls/msvcp100/memory.c");
		File memoryC = new File(msvcp100);
		memoryC.addFromStream(memoryVersion.openStream(),
				"memory.c",
				"Copy files from msvcp90",
				"Copy files from msvcp90", 0);
		
		URL miscVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/4fe6e583752915161142089c3c0d916eefde5ef8:/dlls/msvcp100/misc.c");
		File miscC = new File(msvcp100);
		miscC.addFromStream(miscVersion.openStream(),
				"misc.c",
				"Copy files from msvcp90",
				"Copy files from msvcp90", 0);
		
		URL msvcphVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/4fe6e583752915161142089c3c0d916eefde5ef8:/dlls/msvcp100/msvcp.h");
		File msvcpH = new File(msvcp100);
		msvcpH.addFromStream(msvcphVersion.openStream(),
				"msvcp.h",
				"Copy files from msvcp90",
				"Copy files from msvcp90", 0);
		
		URL stringVersion = new URL("http://source.winehq.org/git/wine.git/blob_plain/4fe6e583752915161142089c3c0d916eefde5ef8:/dlls/msvcp100/string.c");
		File stringC = new File(msvcp100);
		stringC.addFromStream(stringVersion.openStream(),
				"string.c",
				"Copy files from msvcp90",
				"Copy files from msvcp90", 0);
	}

}
