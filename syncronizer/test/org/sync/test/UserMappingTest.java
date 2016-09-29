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
		String cases = "John Smith\n";
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	    System.setErr(new PrintStream(outContent));

		UserMapping directory = new UserMapping(new ByteArrayInputStream(cases.getBytes()));
		assertTrue(outContent.toString().contains("Invalid email mapping at line 1: John Smith"));		
	}
	
	@Test
	public void testGetEmail() {
		String cases = "# Email mapping\n" 
				+ "John Smith = jsmith@email.com\n"
	            + "Jane Doe = jdoe@users.email.com # some user\n"
				;
		UserMapping directory = new UserMapping(new ByteArrayInputStream(cases.getBytes()));
		assertEquals("jsmith@email.com", directory.getEmail("John Smith"));
		assertEquals("jdoe@users.email.com", directory.getEmail("Jane Doe"));
		assertEquals("unknown@noreply.com", directory.getEmail("John Doe"));
	}

	@Test
	public void testGetEmailDefaultDomain() {
		String cases = "# Email mapping\n" 
				+ "John Smith = jsmith@email.com\n"
	            + "Jane Doe = jdoe@users.email.com # some user\n"
		        ;
		UserMapping directory = new UserMapping(new ByteArrayInputStream(cases.getBytes()));
		directory.setDefaultDomain("acme.com");
		assertEquals("jsmith@email.com", directory.getEmail("John Smith"));
		assertEquals("jdoe@users.email.com", directory.getEmail("Jane Doe"));
		assertEquals("John.Doe@acme.com", directory.getEmail("John Doe"));
		assertEquals("J.Random.Hacker@acme.com", directory.getEmail("J. Random Hacker"));
	}
}
