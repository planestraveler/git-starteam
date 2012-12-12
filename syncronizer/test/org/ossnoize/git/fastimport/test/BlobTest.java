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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ossnoize.git.fastimport.Blob;
import org.ossnoize.git.fastimport.Data;

public class BlobTest {

	private Blob blob1;
	private Blob blob2;
	private Data tinyData;
	@Before
	public void setUp() throws Exception {
		tinyData = new Data();
		tinyData.writeData("This is like a small file\nwith some line\n".getBytes());
		blob1 = new Blob(tinyData);
		blob2 = new Blob();
	}

	@After
	public void tearDown() throws Exception {
		blob1 = null;
		blob2 = null;
		tinyData = null;
	}

	@Test
	public void testDoubleOutput() throws IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		blob1.writeTo(byout);
		// First output check if it is ok
		String content = new String(byout.toByteArray(), 0, byout.size());
		assertTrue(content.startsWith("blob\nmark "));
		byout.reset();
		blob1.writeTo(byout);
		assertEquals("", new String(byout.toByteArray(), 0, byout.size()));
	}

	@Test
	public void testSettingContent() throws IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		blob2.setData(tinyData);
		blob2.writeTo(byout);
		String content = new String(byout.toByteArray(), 0, byout.size());
		assertTrue(content.startsWith("blob\nmark "));
	}
}
