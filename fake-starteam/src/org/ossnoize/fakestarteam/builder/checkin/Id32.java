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
public class Id32 extends CheckInInstruction {
  
  @Override
  public void checkin(View view) throws IOException {
    Folder root = view.getRootFolder();
    
    Folder src = findFolderIn(root, "src");
    Folder doc = findFolderIn(root, "doc");
    Folder src_java = findFolderIn(src, "java");
    Folder doc_java = findFolderIn(doc, "java");
    
    File blob = findFileIn(src_java, "blob.java");
    URL blobVer = new URL("https://raw.githubusercontent.com/planestraveler/git-starteam/c0d5d26a24f8592421c043523bdd411fb1055e76/syncronizer/src/org/ossnoize/git/fastimport/Blob.java");
    blob.checkinFromStream(blobVer.openStream(), "Add assignation data", 0, false);
    
    File readme = findFileIn(doc_java, "readme.md");
    URL readmeVer = new URL("https://raw.githubusercontent.com/planestraveler/git-starteam/f89120f040689b7a07d6b15404885f9a6fcad702/README.md");
    readme.checkinFromStream(readmeVer.openStream(), "Instruct on how to run unit-test", 0, false);
    
    view.createViewLabel("Check-in Id 32", "Check Id 32 description", new OLEDate(getTimeOfCheckIn() + 1000), true, true);
  }
  
  @Override
  public long getTimeOfCheckIn() {
    Calendar time = Calendar.getInstance();
    time.set(2016, 7, 10, 14, 14);
    return time.getTimeInMillis();
  }
}
