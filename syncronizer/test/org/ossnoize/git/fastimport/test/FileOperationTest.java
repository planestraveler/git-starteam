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

import org.junit.Test;

import org.ossnoize.git.fastimport.FileDelete;
import org.ossnoize.git.fastimport.FileOperation;
import org.ossnoize.git.fastimport.exception.InvalidPathException;


public class FileOperationTest {

	@Test(expected=InvalidPathException.class)
	public void testPathStartSlash() throws InvalidPathException {
		FileOperation fo = new FileDelete();
		fo.setPath("/file/that is/somewhere");
	}
	
	@Test(expected=InvalidPathException.class)
	public void testPathEndSlash() throws InvalidPathException {
		FileOperation fo = new FileDelete();
		fo.setPath("test/the/ending/slash/");
	}
	
	@Test(expected=InvalidPathException.class)
	public void testStartWithQuote() throws InvalidPathException {
		FileOperation fo = new FileDelete();
		fo.setPath("\"this path/has/quote/in/it\"");
	}
	
	@Test(expected=InvalidPathException.class)
	public void testRelativePath() throws InvalidPathException {
		FileOperation fo = new FileDelete();
		fo.setPath("This/path/./has/something/not/necessary");
	}
	
	@Test(expected=InvalidPathException.class)
	public void testRelativeSomewhereElsePath() throws InvalidPathException {
		FileOperation fo = new FileDelete();
		fo.setPath("This/path/../../has/something/we/could/remove");
	}
	
	@Test(expected=InvalidPathException.class)
	public void testDoubleSlash() throws InvalidPathException {
		FileOperation fo = new FileDelete();
		fo.setPath("test//the/double/slash");
	}
	
	@Test(expected=NullPointerException.class)
	public void testNullPath() throws InvalidPathException {
		FileOperation fo = new FileDelete();
		fo.setPath(null);
	}
	
	@Test
	public void testPathActuallyWork() throws InvalidPathException {
		FileOperation fo = new FileDelete();
		fo.setPath("this/is/a/valid/path.txt");
		
		assertEquals("this/is/a/valid/path.txt", fo.getPath());
	}
	
	@Test
	public void testFileDeleteOperation() throws InvalidPathException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FileOperation fo = new FileDelete();
		fo.setPath("this/is/a/valid/path.txt");
		fo.writeTo(output);
		assertEquals("D this/is/a/valid/path.txt\n", output.toString());
		assertTrue(fo.isInline());
		assertNull(fo.getMark());
	}
	
	@Test(expected=NullPointerException.class)
	public void testFileDeleteWithNoPathSet() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FileOperation fo = new FileDelete();
		fo.writeTo(output);
	}

}
