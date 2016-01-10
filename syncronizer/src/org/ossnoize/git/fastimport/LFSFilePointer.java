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
package org.ossnoize.git.fastimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 *
 * @author steve
 */
public class LFSFilePointer extends Data {
  
  private MessageDigest mDigester;
  private File mFileToDigest;
  private String mGitBaseFolder;
  
  public LFSFilePointer(String baseGitFolder, File temporaryFile) throws NoSuchAlgorithmException {
    super();
    mDigester = MessageDigest.getInstance("SHA-256");
    mGitBaseFolder = baseGitFolder;
    mFileToDigest = temporaryFile;
  }
  
  @Override
  public void writeTo(OutputStream output) throws IOException {
    FileInputStream largeFile = new FileInputStream(mFileToDigest);
    long sizeOfLargeFile = mFileToDigest.length();
    
    File lfsDir = new File(mGitBaseFolder + File.separator + "lfs" + File.separator + "objects");
    
    if (!lfsDir.exists()) {
      Files.createDirectories(lfsDir.toPath());
    }
    File lfsLargeFile = new File(lfsDir.toString(), "starteam-lfs.tmp");
    
    FileOutputStream tempDestination = new FileOutputStream(lfsLargeFile);
    byte[] buffer = new byte[4096 * 20];
    
    int readed = largeFile.read(buffer);
    while (readed >= 0) {
      mDigester.update(buffer, 0, readed);
      tempDestination.write(buffer, 0, readed);
      
      readed = largeFile.read(buffer);
    }
    
    byte[] sha256Digest = mDigester.digest();
    String digest = String.format("%064x", new java.math.BigInteger(1, sha256Digest));
    
    File child1 = new File(lfsDir, digest.substring(0, 2));
    File child2 = new File(child1, digest.substring(2, 4));
    if (!child2.exists()) {
      child2.mkdirs();
    }
    File finalDestination = new File(child2, digest);
    lfsLargeFile.renameTo(finalDestination);

    super.writeData(("version https://git-lfs.github.com/spec/v1\n" +
                     "oid sha256:" + digest + "\n" +
                     "size " + sizeOfLargeFile + "\n").getBytes("UTF-8"));
    super.writeTo(output);
  }
  
  @Override
  public void writeData(byte[] array)
  {
    throw new UnsupportedOperationException("LFSFilePointer should not be used this way");
  }
}
