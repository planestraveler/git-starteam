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
public class Id30 extends CheckInInstruction {
  
  @Override
  public void checkin(View view) throws IOException {
    Folder root = view.getRootFolder();
    
    Folder src = new Folder(root, "src", null);
    Folder doc = new Folder(root, "doc", null);
    Folder src_java = new Folder(src, "java", null);
    Folder doc_java = new Folder(doc, "java", null);
    
    File blob = new File(src_java);
    URL blobVer = new URL("https://raw.githubusercontent.com/planestraveler/git-starteam/6007e74c43272737e5f6ffd18f07a0e29d69827a/syncronizer/src/org/ossnoize/git/fastimport/Blob.java");
    blob.addFromStream(blobVer.openStream(),
      "blob.java",
      "A blob representation for git",
      "A blob representation for git", 0);
    
    File readme = new File(doc_java);
    URL readmeVer = new URL("https://raw.githubusercontent.com/planestraveler/git-starteam/a23b81287112ff67ceadcc7eaeb8880ae71a0422/README.md");
    readme.addFromStream(readmeVer.openStream(),
      "readme.md",
      "Initial readme version",
      "Initial readme version", 0);

    view.createViewLabel("Check-in Id 30", "Check Id 30 description", new OLEDate(getTimeOfCheckIn() + 1000), true, true);
  }

  @Override
  public long getTimeOfCheckIn() {
    Calendar time = Calendar.getInstance();
    time.set(2016, 7, 10, 13, 43);
    return time.getTimeInMillis();
  }
  
}
