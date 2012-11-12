package org.sync.githelper.test;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sync.githelper.GitHelper;

import com.starbase.util.MD5;

public class GitHelperTest {
	private GitHelper test;

	@Before
	public void setUp() throws Exception {
		test = new GitHelper(null, false);
	}

	@After
	public void tearDown() throws Exception {
		test = null;
	}

	@Test
	public void testGetListOfTrackedFile() {
		Set<String> listOfFiles = test.getListOfTrackedFile();
		assertNotNull(listOfFiles);
		// Check for self in the list of files always start from the working directory. 
		assertTrue(listOfFiles.contains("test/org/sync/githelper/test/GitHelperTest.java"));
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
	
	@Test
	public void testGetMD5Of() {
		assertEquals(new MD5("a7e10f59183aa3c456e9059fb7036c9b"), test.getMD5Of("testfiles/ipsum1.txt", "master"));
		assertEquals(new MD5("de7dbcbebe6373006d292240cee4297e"), test.getMD5Of("testfiles/ipsum2.txt", "master"));
		assertEquals(new MD5("da26078a58263879cb5c55331ae52385"), test.getMD5Of("testfiles/ipsum3.txt", "master"));
	}

}
