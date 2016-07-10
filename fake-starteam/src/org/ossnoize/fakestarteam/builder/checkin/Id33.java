/*
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
 */
package org.ossnoize.fakestarteam.builder.checkin;

import com.starbase.starteam.File;
import com.starbase.starteam.Folder;
import com.starbase.starteam.View;
import com.starbase.util.OLEDate;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import org.ossnoize.fakestarteam.builder.CheckInInstruction;

/**
 *
 * @author steve
 */
public class Id33 extends CheckInInstruction {
  
  @Override
  public void checkin(View view) throws IOException {
    Folder root = view.getRootFolder();
    
    Folder src = findFolderIn(root, "src");
    Folder doc = findFolderIn(root, "doc");
    Folder src_java = findFolderIn(src, "java");
    Folder doc_java = findFolderIn(doc, "java");
    
    File blob = findFileIn(src_java, "blob.java");
    URL blobVer = new URL("https://raw.githubusercontent.com/planestraveler/git-starteam/a492cd98d03ddeda86f6ac9da5a8437667c234d1/syncronizer/src/org/ossnoize/git/fastimport/Blob.java");
    blob.checkinFromStream(blobVer.openStream(), "Only write a blob once", 0, false);
    
    File readme = findFileIn(doc_java, "readme.md");
    URL readmeVer = new URL("https://raw.githubusercontent.com/planestraveler/git-starteam/54bcccfcc54b0af5f11d64c1c1b0542d68918ccf/README.md");
    readme.checkinFromStream(readmeVer.openStream(), "Correct some details", 0, false);
    
    view.createViewLabel("Check-in Id 33", "Check Id 33 description", new OLEDate(getTimeOfCheckIn() + 1000), true, true);
  }
  
  @Override
  public long getTimeOfCheckIn() {
    Calendar time = Calendar.getInstance();
    time.set(2016, 7, 10, 14, 42);
    return time.getTimeInMillis();
  }
}
