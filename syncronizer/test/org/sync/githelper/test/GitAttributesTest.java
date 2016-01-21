/*****************************************************************************
    This file is part of Git-Starteam.

    Git-Starteam is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Git-Starteam is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Git-Starteam.  If not, see <http://www.gnu.org/licenses/>.
******************************************************************************/

package org.sync.githelper.test;

import java.io.ByteArrayInputStream;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.sync.githelper.GitAttributeKind;
import org.sync.githelper.GitAttributes;

public class GitAttributesTest {
  
  private GitAttributes test;
  
  @Before
  public void setUp() {
    test = new GitAttributes();
    ByteArrayInputStream stream = new ByteArrayInputStream(
      ("# some comment that need to be kept\n"
        + "# which are multiline\n"
        + "path/to/file.bin filter=lfs diff=lfs merge=lfs -text\n"
        + "UnixScript.sh eol=lf\n"
        + "WindowsScript.bat eol=crlf").getBytes());
    test.parse(stream);
  }
  
  @After
  public void tearDown() {
    test = null;
  }
  
  @Test
  public void testEmptyAttributes() {
    test = new GitAttributes();
    test.setTopLevelComment("This file is auto generated\nPlease do not modify it.");
    
    assertEquals("#This file is auto generated\n"
        + "#Please do not modify it.", 
      test.toString());
  }
  
  @Test
  public void testPreservedAttributes() {
    assertEquals("# some comment that need to be kept\n"
        + "# which are multiline\n"
        + "path/to/file.bin filter=lfs diff=lfs merge=lfs -text\n"
        + "UnixScript.sh eol=lf\n"
        + "WindowsScript.bat eol=crlf",
      test.toString());
  }
  
  @Test
  public void testNewLFSTrack()
  {
    test.addAttributeToPath("path/to/hugeFile.zip", GitAttributeKind.Binary, GitAttributeKind.FilterLfs, GitAttributeKind.DiffLfs, GitAttributeKind.MergeLfs);
    assertEquals("# some comment that need to be kept\n"
      + "# which are multiline\n"
      + "path/to/file.bin filter=lfs diff=lfs merge=lfs -text\n"
      + "path/to/hugeFile.zip -text filter=lfs diff=lfs merge=lfs\n"
      + "UnixScript.sh eol=lf\n"
      + "WindowsScript.bat eol=crlf",
      test.toString());
  }
}
