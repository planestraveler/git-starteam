package org.sync.githelper.test;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sync.githelper.GitHelper;

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

}
