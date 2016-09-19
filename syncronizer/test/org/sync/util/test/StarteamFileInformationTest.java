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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sync.util.StarteamFileInfo;

public class StarteamFileInformationTest {

	private StarteamFileInfo file1;
	private StarteamFileInfo file2;
	
	@Before
	public void setUp() throws Exception {
		file1 = new StarteamFileInfo("test1.txt", 1, 0, 0);
		file2 = new StarteamFileInfo("test2.txt", 2, 0, 0);
	}

	@After
	public void tearDown() throws Exception {
		file1 = null;
		file2 = null;
	}

	@Test
	public void test() {
		assertEquals("test1.txt", file1.getFilename());
		assertEquals("test2.txt", file2.getFilename());
		assertEquals(1, file1.getId());
		assertEquals(2, file2.getId());
		assertEquals(0, file1.getVersion());
		assertEquals(0, file1.getContentVersion());
		
		file1.setId(4);
		assertEquals(4, file1.getId());
		
		file2.setVersion(2);
		assertEquals(2, file2.getVersion());
		assertEquals(0, file2.getContentVersion());
		
		file1.setFilename("renameTest1.txt");
		assertEquals("renameTest1.txt", file1.getFilename());
		assertEquals("test2.txt", file2.getFilename());
	}

}
