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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sync.RepositoryHelper;
import org.sync.githelper.GitHelper;
import org.sync.util.FileUtility;

import com.starbase.util.MD5;

public class GitHelperTest {
	private RepositoryHelper test;
	private File bareRepo;
	
	@Before
	public void setUp() throws Exception {
		test = new GitHelper(null, false);
		bareRepo = new File(System.getProperty("java.io.tmpdir") + File.separator + "bareRepo");
	}

	@After
	public void tearDown() throws Exception {
		test.dispose();
		test = null;
		FileUtility.rmDir(bareRepo);
		bareRepo = null;
	}

	@Test(timeout=1000)
	public void testGetListOfTrackedFile() {
		Set<String> listOfFiles = test.getListOfTrackedFile("master");
		assertNotNull(listOfFiles);
		// Check for self in the list of files always start from the working directory. 
		assertTrue(listOfFiles.contains("syncronizer/test/org/sync/githelper/test/GitHelperTest.java"));
	}
	
	@Test
	public void testSpecialFiles() {
		assertTrue(test.isSpecialFile(".gitignore"));
		assertTrue(test.isSpecialFile("a/deep/down/git/directory/.gitignore"));
		assertTrue(test.isSpecialFile(".gitattributes"));
		assertTrue(test.isSpecialFile("a/deep/down/git/directory/.gitattributes"));
		assertFalse(test.isSpecialFile("aFile.txt"));
		assertFalse(test.isSpecialFile("some/random/directory/file.gitignore"));
	}
	
	@Test(timeout=1000)
	public void testGetMD5Of() throws IOException {
		assertEquals(new MD5("a7e10f59183aa3c456e9059fb7036c9b"), test.getMD5Of("testfiles/ipsum1.txt", "master"));
		assertEquals(new MD5("de7dbcbebe6373006d292240cee4297e"), test.getMD5Of("testfiles/ipsum2.txt", "master"));
		assertEquals(new MD5("da26078a58263879cb5c55331ae52385"), test.getMD5Of("testfiles/ipsum3.txt", "master"));
	}

	@Test
	public void testGC() {
		assertEquals(0, test.gc());
		test.setWorkingDirectory(System.getProperty("java.io.tmpdir"), false);
		assertEquals(128, test.gc());
	}
	
	@Test
	public void testIsBare() throws IOException {
		assertEquals(false, test.isBareRepository());
		bareRepo.mkdir();
		test.setWorkingDirectory(bareRepo.getAbsolutePath(), true);
		assertEquals(true, test.isBareRepository());
		bareRepo.delete();
	}
}
