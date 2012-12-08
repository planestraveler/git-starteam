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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ossnoize.git.fastimport.Data;

public class DataTest {
	public Data data1;
	public Data data2;

	@Before
	public void setUp() throws Exception {
		data1 = new Data();
		data2 = new Data(new File("../testfiles/ipsum1.txt"));
	}

	@After
	public void tearDown() throws Exception {
		data1 = null;
		data2 = null;
	}

	@Test
	public void testOutput() throws IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		String test = "abcdefghijklmnopqrstuvwxyz";
		data1.writeData(test.getBytes());
		data1.writeTo(byout);
		assertEquals("data 26\nabcdefghijklmnopqrstuvwxyz\n", new String(byout.toByteArray(), 0, byout.size()));
	}
	
	@Test
	public void testOuput2() throws IOException {
		Pattern tester = Pattern.compile("data 3527\n[a-zA-Z \n\\p{Punct}]{3527}\n");
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		data2.writeTo(byout);
		String bigString = new String(byout.toByteArray(),0,byout.size());
		assertTrue(tester.matcher(bigString).matches());
	}

	@Test
	public void testOutput3() throws IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		OutputStream out = data1.getOutputStream();
		out.write("zyxwvutsrqponmlkjihgfedcba".getBytes());
		data1.writeTo(byout);
		assertEquals("data 26\nzyxwvutsrqponmlkjihgfedcba\n", new String(byout.toByteArray(), 0, byout.size()));
	}
	
	@Test
	public void testNull() {
		assertNull(data2.getOutputStream());
	}
}
