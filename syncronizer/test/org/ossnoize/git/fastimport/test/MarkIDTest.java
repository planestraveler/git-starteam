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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ossnoize.git.fastimport.Mark;

public class MarkIDTest {
	private Pattern markIdPattern = Pattern.compile(":[0-9]+");
	private Pattern markPattern = Pattern.compile("mark :[0-9]+$");
	private Mark mark1;
	private Mark mark2;

	@Before
	public void setUp() {
		mark1 = new Mark();
		mark2 = new Mark();
	}
	@After
	public void tearDown() {
		mark1 = null;
		mark2 = null;
	}
	
	@Test
	public void testMarkPattern() {
		assertNotSame(":0", mark1.getID().getId());
		assertTrue(markIdPattern.matcher(mark1.getID().getId()).matches());
		assertTrue(markIdPattern.matcher(mark1.getID().toString()).matches());
	}
	
	@Test
	public void testOutputMark() throws IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		mark2.writeTo(byout);
		String markString = new String(byout.toByteArray(), 0, byout.size()).trim();
		assertTrue(markPattern.matcher(markString).matches());
	}

	@Test
	public void testNotSame() {
		assertNotSame(mark1, mark2);
	}
}
