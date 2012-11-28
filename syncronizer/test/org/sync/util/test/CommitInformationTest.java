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
package org.sync.util.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sync.util.CommitInformation;

public class CommitInformationTest {
	
	private CommitInformation commitB;
	private CommitInformation commitC;
	private CommitInformation commitA;
	private CommitInformation commitD;
	private CommitInformation commitE;
	private CommitInformation commitF;
	private CommitInformation commitG;

	@Before
	public void setUp() throws Exception {
		commitA = new CommitInformation(12345, 12, "TestA", "/path/to/file/testA");
		commitB = new CommitInformation(12347, 12, "TestA", "/path/to/file/testB");
		commitC = new CommitInformation(12378, 12, "", "/path/to/file/testC");
		commitD = new CommitInformation(12378, 11, "Conflict", "/path/to/file/testD");
		commitE = new CommitInformation(12347, 12, "TestA", "/path/to/file/testE");
		commitF = new CommitInformation(12347, 12, "", "/path/to/file/testA");
		commitG = new CommitInformation(12378, 11, "", "/path/to/file/testD");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEqualsObject() {
		assertEquals(commitA, new CommitInformation(78557, 12, "TestA", "/path/to/an/other/File"));
		assertNotSame(commitD, commitA);
	}

	@Test
	public void testTransitifLessThan() {
		assertTrue(commitA.compareTo(commitB) < 0);
		assertTrue(commitB.compareTo(commitC) < 0);
		assertTrue(commitA.compareTo(commitC) < 0);
	}
	
	@Test
	public void testTransitifGreaterThan() {
		assertTrue(commitC.compareTo(commitB) > 0);
		assertTrue(commitB.compareTo(commitA) > 0);
		assertTrue(commitC.compareTo(commitA) > 0);
	}
	
	@Test
	public void testCompareSameCommit() {
		assertEquals(commitD.compareTo(commitD), 0);
	}
	
	@Test
	public void testAlmostSameTimeUIDDifferentFile() {
		assertTrue(commitB.compareTo(commitE) < 0);
		assertTrue(commitE.compareTo(commitB) > 0);
	}
	
	@Test
	public void testAlmostSameTimeDifferentUID() {
		assertTrue(commitD.compareTo(commitC) < 0);
		assertTrue(commitC.compareTo(commitD) > 0);
	}
	
	@Test
	public void testAlmostSameTimeUIDFileNoComment() {
		assertEquals(0, commitG.compareTo(commitD));
	}
	
	@Test
	public void testAlmostSameTimeUIDNoCommentDifferentFile() {
		assertTrue(commitF.compareTo(commitE) < 0);
		assertTrue(commitE.compareTo(commitF) > 0);
	}

}
