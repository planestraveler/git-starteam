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
import org.ossnoize.git.fastimport.FileModification;
import org.ossnoize.git.fastimport.enumeration.GitFileType;
import org.ossnoize.git.fastimport.exception.InvalidPathException;

public class FileModificationTest {

	private Blob aBlob;
	private Data someData;

	@Before
	public void setUp() throws Exception {
		aBlob = new Blob();
		someData = new Data();
		someData.writeData("this is some data on one line".getBytes());
		aBlob.setData(someData);
	}

	@After
	public void tearDown() throws Exception {
		someData = null;
		aBlob = null;
	}

	@Test
	public void testFileModificationInline() throws InvalidPathException, IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		FileModification testA = new FileModification(someData);
		testA.setPath("some/path/information.txt");
		testA.setFileType(GitFileType.Normal);
		testA.writeTo(byout);
		
		assertTrue(testA.isInline());
		assertEquals("M 100644 inline some/path/information.txt\ndata 29\nthis is some data on one line\n",
				new String(byout.toByteArray(), 0, byout.size()));
		assertNull(testA.getMark());
	}
	
	@Test
	public void testFileModificationReferenced() throws InvalidPathException, IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		FileModification testB = new FileModification(aBlob);
		testB.setPath("some/other/path/information.txt");
		testB.setFileType(GitFileType.Executable);
		testB.writeTo(byout);
		
		assertFalse(testB.isInline());
		assertNotNull(testB.getMark());
		assertEquals("M 100755 " + aBlob.getMarkID().getId() + " some/other/path/information.txt\n",
				new String(byout.toByteArray(), 0, byout.size()));
	}

	@Test(expected=NullPointerException.class)
	public void testFileModificationNoPath() throws IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		FileModification testC = new FileModification(aBlob);
		testC.setFileType(GitFileType.Normal);
		testC.writeTo(byout);
	}
	
	@Test(expected=NullPointerException.class)
	public void testFileModificationNoFileType() throws IOException, InvalidPathException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		FileModification testD = new FileModification(aBlob);
		testD.setPath("some/non/important/path.txt");
		testD.writeTo(byout);
	}
	
	@Test(expected=IOException.class)
	public void testFileModificationNullBlob() throws IOException, InvalidPathException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		Blob fakeBlob = null;
		FileModification testE = new FileModification(fakeBlob);
		testE.setFileType(GitFileType.Normal);
		testE.setPath("some/other/path.txt");
		testE.writeTo(byout);
	}
}
