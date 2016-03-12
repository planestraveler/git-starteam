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
public class Id13 extends CheckInInstruction {

  @Override
  public void checkin(View view) throws IOException {
    Folder root = view.getRootFolder();
		Folder archive = findFolderIn(root, "archive");
    
    
    File fileRollerv3163 = new File(archive);
    URL fileRollerDL = new URL("http://gentoo.mirrors.tera-byte.com/distfiles/file-roller-3.16.3.tar.xz");
    fileRollerv3163.addFromStream(fileRollerDL.openStream(),
      "file-roller-3.16.3.tar.xz",
      "Source archive of file-roller 3.16.3",
      "Source archive of file-roller 3.16.3", 0);
    
    File imageMagic = new File(archive);
    URL imageMagicDL = new URL("http://gentoo.mirrors.tera-byte.com/distfiles/ImageMagick-6.9.2-10.tar.xz");
    imageMagic.addFromStream(imageMagicDL.openStream(),
      "ImageMagick-6.9.2-10.tar.xz",
      "Source archive of ImageMagick 6.9.2",
      "Source archive of ImageMagick 6.9.2", 0);
    
    File appliance = new File(archive);
    URL applianceDL = new URL("http://gentoo.mirrors.tera-byte.com/distfiles/appliance-1.28.1.tar.xz");
    appliance.addFromStream(applianceDL.openStream(),
      "appliance-1.28.1.tar.xz",
      "Source archive of appliance",
      "Source archive of appliance", 0);
    
    view.createViewLabel("Check-in Id 13", "Check Id 13 description", new OLEDate(getTimeOfCheckIn() + 1000), true, true);
  }

  @Override
  public long getTimeOfCheckIn() {
    Calendar time = Calendar.getInstance();
    time.set(2016, 1, 29, 9, 10);
    return time.getTimeInMillis();
  }
  
}
