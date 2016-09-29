package org.sync.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.Test;

public class UserMappingTest {

	protected class UserMapping extends org.sync.UserMapping {
		public UserMapping(String filename) {
			  super(filename);
			}
		public UserMapping(InputStream stream) {
		  super(stream);
		}
	}
	
	@Test
	public void testNullFile() {
	    final String filename = null;
		UserMapping directory = new UserMapping(filename);
	}

	@Test
	public void testInvalidFile() {
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	    System.setErr(new PrintStream(outContent));

	    final String filename = "some inexistant file.txt";
		UserMapping directory = new UserMapping(filename);
	    assertTrue(outContent.toString().contains("Email mapping file \"" + filename + "\" not found."));		
	}

	@Test
	public void testInvalidEntry() {
		String cases = "John Smith\n"
				+ "1 Invalid User - = \"1w@\"@1.-\n"
				+ "Another Invalid_User = 1w@1._\n"
				+ "=\n"
				+ "John = 1.\n"
				;
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	    System.setErr(new PrintStream(outContent));

		UserMapping directory = new UserMapping(new ByteArrayInputStream(cases.getBytes()));
		assertTrue(outContent.toString().contains("Invalid email mapping at line 1: John Smith"));		
		assertTrue(outContent.toString().contains("Invalid email mapping at line 2: 1 Invalid User - = \"1w@\"@1.-"));		
		assertTrue(outContent.toString().contains("Invalid email mapping at line 3: Another Invalid_User = 1w@1._"));		
		assertTrue(outContent.toString().contains("Invalid email mapping at line 4: ="));		
		assertTrue(outContent.toString().contains("Invalid email mapping at line 5: John = 1."));		
	}
	
	@Test
	public void testGetEmail() {
		String cases = "# Email mapping\n" 
				+ "John Smith = jsmith@email.com\n"
	            + "Jane Doe = jdoe@users.email.com\n"
				+ "1 Weird User _ = \"w@\"@[1.2.3.4]\n"
	            + "Another Weird User = #{`@1.2\n"
				;
		UserMapping directory = new UserMapping(new ByteArrayInputStream(cases.getBytes()));
		assertEquals("jsmith@email.com", directory.getEmail("John Smith"));
		assertEquals("jdoe@users.email.com", directory.getEmail("Jane Doe"));
		assertEquals("\"w@\"@[1.2.3.4]", directory.getEmail("1 Weird User _"));
		assertEquals("#{`@1.2", directory.getEmail("Another Weird User"));
		assertEquals("unknown@noreply.com", directory.getEmail("John Doe"));
	}

	@Test
	public void testGetEmailDefaultDomain() {
		String cases = " # Email mapping\n" 
				+ "John Smith = jsmith@email.com\n"
	            + "Jane Doe = jdoe@users.email.com\n"
		        ;
		UserMapping directory = new UserMapping(new ByteArrayInputStream(cases.getBytes()));
		directory.setDefaultDomain("acme.com");
		assertEquals("jsmith@email.com", directory.getEmail("John Smith"));
		assertEquals("jdoe@users.email.com", directory.getEmail("Jane Doe"));
		assertEquals("John.Doe@acme.com", directory.getEmail("John Doe"));
		assertEquals("J.Random.Hacker@acme.com", directory.getEmail("J. Random Hacker"));
	}
}
