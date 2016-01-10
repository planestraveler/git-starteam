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
package org.ossnoize.git.fastimport.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.ossnoize.git.fastimport.LFSFilePointer;

/**
 *
 * @author steve
 */
public class LFSFilePointerTest {
  
  private ByteArrayOutputStream mOutput;
  private LFSFilePointer mPointer;
  
  @Before
  public void SetUp() throws NoSuchAlgorithmException, IOException  {
    mOutput = new ByteArrayOutputStream(2048);
    Path tempDirectory = Files.createTempDirectory("LFSTest");
    
    mPointer = new LFSFilePointer(tempDirectory.toString(), new File("../testfiles/bigfile.bin"));
  }
  
  @After
  public void TearDown() {
    mOutput = null;
    mPointer = null;
  }
  
  @Test
  public void testGenerateFromBigFile() throws IOException {
    mPointer.writeTo(mOutput);
    
    assertEquals(
      "data 134\n" + // Since this is a data extension we use it to calculate the actual pointer size
      "version https://git-lfs.github.com/spec/v1\n" +
      "oid sha256:de73c67cc17aba8cad0561ad6391d4a1146a81ed7af70efbfd42987599a04761\n" +
      "size 104857600\n\n", // a data command put an extra line feed at the end of the command 
      mOutput.toString("UTF-8"));
  }
  
  @Test(expected=UnsupportedOperationException.class)
  public void testWriteArray() throws UnsupportedEncodingException {
    mPointer.writeData("Some fake inforamtion".getBytes("UTF-8"));
  }
  
}
