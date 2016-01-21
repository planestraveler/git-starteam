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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.sync.githelper.GitAttributeKind;

public class GitAttributeKindTest {
  
  @Test
  public void testBinary() {
    assertEquals(GitAttributeKind.Binary, GitAttributeKind.fromRepresentation("-text"));
  }
  
  @Test
  public void testFilterLFS() {
    assertEquals(GitAttributeKind.FilterLfs, GitAttributeKind.fromRepresentation("filter=lfs"));
  }
  
  @Test
  public void testDiffLFS() {
    assertEquals(GitAttributeKind.DiffLfs, GitAttributeKind.fromRepresentation("diff=lfs"));
  }
  
  @Test
  public void testMergeLFS() {
    assertEquals(GitAttributeKind.MergeLfs, GitAttributeKind.fromRepresentation("merge=lfs"));
  }
  
  @Test
  public void testCRLF() {
    assertEquals(GitAttributeKind.CRLF, GitAttributeKind.fromRepresentation("crlf"));
    assertEquals(GitAttributeKind.CRLF, GitAttributeKind.fromRepresentation("eol=crlf"));
    assertEquals(GitAttributeKind.CRLF, GitAttributeKind.fromRepresentation("crlf=input"));
  }
  
  @Test
  public void testLF() {
    assertEquals(GitAttributeKind.LF, GitAttributeKind.fromRepresentation("eol=lf"));
  }
  
  @Test
  public void testIdent() {
    assertEquals(GitAttributeKind.Ident, GitAttributeKind.fromRepresentation("ident"));
  }
  
  @Test
  public void testPath() {
    assertEquals(GitAttributeKind.NotAnAttribute, GitAttributeKind.fromRepresentation("this/is/a/path/to/a/file.txt"));
  }
  
  @Test
  public void testPathWithSpace() {
    assertEquals(GitAttributeKind.NotAnAttribute, GitAttributeKind.fromRepresentation("this/is/a/path with space/to/a/file.txt"));
  }
}
