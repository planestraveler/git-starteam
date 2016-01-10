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
public class Id12 extends CheckInInstruction {

  @Override
  public void checkin(View view) throws IOException {
    Folder root = view.getRootFolder();
		Folder archive = findFolderIn(root, "archive");
    
    
		File boostV155 = findFileIn(archive, "boost_1_55_0.tar.bz2");
    boostV155.remove();
    
    File boostV156 = new File(archive);
    URL boostDL = new URL("http://gentoo.mirrors.tera-byte.com/distfiles/boost_1_56_0.tar.bz2");
    boostV156.addFromStream(boostDL.openStream(),
      "boost_1_56_0.tar.bz2",
      "Source archive of boost 1.56.0",
      "Source archive of boost 1.56.0", 0);
    
    view.createViewLabel("Check-in Id 12", "Check Id 12 description", new OLEDate(getTimeOfCheckIn() + 1000), true, true);
  }

  @Override
  public long getTimeOfCheckIn() {
    Calendar time = Calendar.getInstance();
    time.set(2015, 6, 2, 15, 12);
    return time.getTimeInMillis();
  }
  
}
