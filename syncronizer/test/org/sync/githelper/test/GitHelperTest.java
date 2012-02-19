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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetListOfTrackedFile() {
		test = new GitHelper(null);
		Set<String> listOfFiles = test.getListOfTrackedFile();
		assertNotNull(listOfFiles);
		// Check for self in the list of files always start from the working directory. 
		assertTrue(listOfFiles.contains("test/org/sync/githelper/test/GitHelperTest.java"));
	}

}
