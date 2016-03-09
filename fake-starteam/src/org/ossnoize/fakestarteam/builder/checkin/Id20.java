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
public class Id20 extends CheckInInstruction {

  @Override
  public void checkin(View view) throws IOException {
    Folder root = view.getRootFolder();
    Folder src = new Folder(root, "src", null);
    
    File blob = new File(src);
    URL blobVer = new URL("https://raw.githubusercontent.com/planestraveler/git-starteam/c0d5d26a24f8592421c043523bdd411fb1055e76/syncronizer/src/org/ossnoize/git/fastimport/Blob.java");
    blob.addFromStream(blobVer.openStream(),
      "blob.java",
      "A blob representation for git",
      "A blob representation for git", 0);
    
    File data = new File(src);
    URL dataVer = new URL("https://raw.githubusercontent.com/planestraveler/git-starteam/fa3525613a2e2bf358f2a8742794761a797b26a9/syncronizer/src/org/ossnoize/git/fastimport/Data.java");
    data.addFromStream(dataVer.openStream(),
      "data.java",
      "A data representation for git",
      "A data representation for git", 0);

    view.createViewLabel("Check-in Id 20", "Check Id 20 description", new OLEDate(getTimeOfCheckIn() + 1000), true, true);
  }

  @Override
  public long getTimeOfCheckIn() {
    Calendar time = Calendar.getInstance();
    time.set(2015, 3, 6, 12, 35);
    return time.getTimeInMillis();
  }
  
}
