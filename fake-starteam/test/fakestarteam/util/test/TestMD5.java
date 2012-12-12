package fakestarteam.util.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ossnoize.fakestarteam.FileUtility;

import com.starbase.util.MD5;

public class TestMD5 {
	
	String md5sum;
	String md5ipsum;
	byte[] md5data;
	InputStream md5IpsumStream;

	@Before
	public void setUp() throws Exception {
		md5sum   = "561a7adc350a2f6ce0d9d97d8b4c1366";
		md5ipsum = "fe02b7577a12a45727a10e565c21de8f";
		md5data  = new byte[] {  86,  26, 122, -36, 
							     53,  10,  47, 108,
							    -32, -39, -39, 125,
							   -117,  76,  19, 102 };
		md5IpsumStream = getClass().getResourceAsStream("/fakestarteam/test/ressources/md5testfile.ipsum");
	}

	@After
	public void tearDown() throws Exception {
		md5sum = null;
		md5ipsum = null;
		md5data = null;
		FileUtility.close(md5IpsumStream);
		md5IpsumStream = null;
	}

	@Test
	public void testMD5String() {
		MD5 test = new MD5(md5sum);
		assertArrayEquals(md5data, test.getData());
	}

	@Test
	public void testMD5ByteArray() {
		MD5 test = new MD5(md5data);
		assertEquals(md5sum, test.toHexString());
	}

	@Test
	public void testComputeStreamMD5Ex() throws IOException {
		MD5 test = new MD5();
		long size = test.computeStreamMD5Ex(md5IpsumStream);
		assertEquals(7075, size);
		assertEquals(md5ipsum, test.toHexString());
	}

	@Test
	public void testComputeFileMD5Ex() throws IOException {
		File ftest = File.createTempFile("md5testcase", "ipsum");
		ftest.deleteOnExit();
		
		FileOutputStream fout = new FileOutputStream(ftest);
		FileUtility.copyStream(fout, md5IpsumStream);
		FileUtility.close(fout);
		
		MD5 test = new MD5();
		long size = test.computeFileMD5Ex(ftest);
		assertEquals(7075, size);
		assertEquals(md5ipsum, test.toHexString());
	}

	@Test
	public void testSetData() {
		MD5 test = new MD5();
		test.setData(md5data);
		assertArrayEquals(md5data, test.getData());
	}
	
	@Test
	public void testEquals() {
		MD5 test = new MD5(md5data);
		assertEquals(test, new MD5(md5data));
		assertFalse(test.equals(new Object()));
		assertFalse(test.equals(new MD5(md5ipsum)));
	}
	
	@Test
	public void testNotSet() {
		MD5 empty = new MD5();
		assertEquals("00000000000000000000000000000000", empty.toHexString());
	}
	
	@Test
	public void testSetAnything() {
		MD5 almostEmpty = new MD5("0000000000000000000000123456789a");
		assertEquals("0000000000000000000000123456789a", almostEmpty.toHexString());
		assertEquals("0000000000000000000000123456789a", almostEmpty.toString());
	}

}
