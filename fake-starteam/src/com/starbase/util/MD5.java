/*****************************************************************************
 * All public interface based on Starteam API are a property of Borland, 
 * those interface are reproduced here only for testing purpose. You should
 * never use those interface to create a competitive product to the Starteam
 * Server. 
 * 
 * The implementation is given AS-IS and should not be considered a reference 
 * to the API. The behavior on a lots of method and class will not be the
 * same as the real API. The reproduction only seek to mimic some basic 
 * operation. You will not found anything here that can be deduced by using
 * the real API.
 * 
 * Fake-Starteam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *****************************************************************************/
package com.starbase.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.ossnoize.fakestarteam.FileUtility;

public class MD5 {
	byte[] md5Sum = new byte[16];

	public MD5() {
	}
	
	public MD5(String stringMD5) {
		BigInteger bigInt = new BigInteger(stringMD5, 16);
		byte[] value = bigInt.toByteArray();
		int decal = 0;
		int from = 0;
		if(value.length < md5Sum.length)
			decal = md5Sum.length - value.length;
		else
			from = value.length - md5Sum.length;
		System.arraycopy(value, from, md5Sum, decal, Math.min(value.length, md5Sum.length));
	}
	
	public MD5(byte[] md5Array) {
		System.arraycopy(md5Array, 0, md5Sum, 0, md5Sum.length);
	}

	public long computeStreamMD5Ex(InputStream in) throws java.io.IOException {
		long fileLength = 0;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");

			byte buffer[] = new byte[64*1024];
			int read = in.read(buffer);
			while(read >= 0) {
				fileLength += read;
				digest.update(buffer, 0, read);
				read = in.read(buffer);
			}
			System.arraycopy(digest.digest(), 0, md5Sum, 0, md5Sum.length);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Could not find the algorithm: " + e.getMessage());
		}
		return fileLength;
	}
	
	public long computeFileMD5Ex(File file) throws java.io.IOException {
		long fileLength = 0;
		FileInputStream in = new FileInputStream(file);
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			
			byte buffer[] = new byte[64*1024];
			int read = in.read(buffer);
			while(read >= 0) {
				fileLength += read;
				digest.update(buffer, 0, read);
				read = in.read(buffer);
			}
			System.arraycopy(digest.digest(), 0, md5Sum, 0, md5Sum.length);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Could not find the algorithm: " + e.getMessage());
		} finally {
			FileUtility.close(in);
		}
		return fileLength;
	}

	public byte[] getData() {
		return md5Sum;
	}
	
	public void setData(byte[] digest) {
		System.arraycopy(digest, 0, md5Sum, 0, md5Sum.length);
	}
	
	public String toHexString() {
		BigInteger bigInt = new BigInteger(1, md5Sum);
		String output = bigInt.toString(16);
		while(output.length() < 32) {
			output = "0" + output;
		}
		return output;
	}
	
	@Override
	public String toString() {
		return toHexString();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof MD5) {
			MD5 other = (MD5)o;
			for(int i=0; i < md5Sum.length; i++) {
				if(other.md5Sum[i] != md5Sum[i])
					return false;
			}
			return true;
		}
		return false;
	}
	
}
