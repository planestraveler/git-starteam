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
import org.ossnoize.git.fastimport.Sha1Ref;
import org.ossnoize.git.fastimport.exception.InvalidSha1;

public class Sha1RefTest {

	@Test(expected=InvalidSha1.class)
	public void testException() {
		new Sha1Ref("asdc123456");
	}
	
	@Test
	public void testGetter() {
		Sha1Ref ref = new Sha1Ref("1234567890abcdefabcd1234567890efabcdefab");
		assertEquals("1234567890abcdefabcd1234567890efabcdefab", ref.getId());
	}
	
	@Test
	public void testOutput() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream(40);
		Sha1Ref ref = new Sha1Ref("1234567890987654321abcdefedcba0123456789");
		ref.writeTo(output);
		String sha1ref = new String(output.toByteArray());
		assertEquals("1234567890987654321abcdefedcba0123456789", sha1ref);
	}
	
	@Test
	public void testToSTring() {
		Sha1Ref ref = new Sha1Ref("1234567890abcdefabcd1234567890efabcdefab");
		assertEquals("1234567890abcdefabcd1234567890efabcdefab", ref.toString());
	}
}
