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
import java.util.Calendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ossnoize.git.fastimport.Blob;
import org.ossnoize.git.fastimport.Commit;
import org.ossnoize.git.fastimport.Data;
import org.ossnoize.git.fastimport.FileDelete;
import org.ossnoize.git.fastimport.FileModification;
import org.ossnoize.git.fastimport.enumeration.GitFileType;

public class CommitTest {

	private Blob simpleBlob;
	private Data smallData;
	private Commit cmt;
	private Commit nextCmt;
	private Commit inline;
	private Commit fromSomeone;
	private Commit merge;

	@Before
	public void setUp() throws Exception {
		Calendar aDate = Calendar.getInstance();
		aDate.setTimeInMillis(1355795869);
		smallData = new Data();
		smallData.writeData("This is a small file\nwith multiple line".getBytes());
		simpleBlob = new Blob(smallData);
		
		cmt = new Commit("Me Tester", "Me.tester@domain.com", "Small commit message", "master", aDate.getTime());
		FileModification fo = new FileModification(simpleBlob);
		fo.setPath("a random file.txt");
		fo.setFileType(GitFileType.Normal);
		cmt.addFileOperation(fo);
		
		aDate.add(Calendar.MINUTE, 30);
		
		nextCmt = new Commit("Me Tester", "Me.tester@domain.com", "Other message", "master", aDate.getTime());
		nextCmt.setFromCommit(cmt);
		FileModification fo2 = new FileModification(simpleBlob);
		fo2.setPath("a random file - Copy.txt");
		fo2.setFileType(GitFileType.Executable);
		nextCmt.addFileOperation(fo2);
		
		aDate.add(Calendar.MINUTE, 30);
		
		inline = new Commit("Me Tester", "Me.tester@domain.com", "Inline Data", "master", aDate.getTime());
		inline.resumeOnTopOfRef();
		FileModification fo3 = new FileModification(smallData);
		fo3.setPath("a random file - Copy - Copy.txt");
		fo3.setFileType(GitFileType.Normal);
		inline.addFileOperation(fo3);

		aDate.add(Calendar.MINUTE, 30);
		
		fromSomeone = new Commit("Me Tester", "Me.tester@domain.com", "An other branch", "branchA", aDate.getTime());
		fromSomeone.setAuthor("Other Tester", "Other.Tester@domain.com");
		FileDelete fd1 = new FileDelete();
		fd1.setPath("a random file - Copy - Copy.txt");
		fromSomeone.addFileOperation(fd1);
		FileDelete fd2 = new FileDelete();
		fd2.setPath("a random file - Copy.txt");
		fromSomeone.addFileOperation(fd2);

		aDate.add(Calendar.MINUTE, 30);
		
		merge = new Commit("Me Tester", "Me.tester@domain.com", "merge branchA", "master", aDate.getTime());
		merge.setMergeCommit(fromSomeone);
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected=NullPointerException.class)
	public void testNoNullMessage() throws IOException {
		new Commit("Me Tester", "Me.tester@domain.com", null, "master", new java.util.Date());
	}
	
	@Test
	public void testCommitReferencedBlob() throws IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		cmt.writeTo(byout);
		
		assertEquals(
				"commit refs/heads/master\nmark " + cmt.getMarkID() + "\n" +
				"committer Me Tester <Me.tester@domain.com> 1355795 -0500\n" + 
				"data 20\nSmall commit message\n" +
				"M " + GitFileType.Normal.getOctalRepresentation() + " " + simpleBlob.getMarkID() + " a random file.txt\n\n",
				new String(byout.toByteArray(), 0, byout.size()));
		assertEquals("master", cmt.getReference());
	}
	
	@Test
	public void testCommitFromWithCopy() throws IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		nextCmt.writeTo(byout);
		
		assertEquals(
				"commit refs/heads/master\nmark " + nextCmt.getMarkID() + "\n" +
				"committer Me Tester <Me.tester@domain.com> 1357595 -0500\n" +
				"data 13\nOther message\n" +
				"from " + cmt.getMarkID() + "\n" +
				"M " + GitFileType.Executable.getOctalRepresentation() + " " + simpleBlob.getMarkID() + " a random file - Copy.txt\n\n",
				new String(byout.toByteArray(), 0, byout.size()));
		assertEquals("master", nextCmt.getReference());
	}
	
	@Test
	public void testCommitInlineFileResume() throws IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		inline.writeTo(byout);
		
		assertEquals(
				"commit refs/heads/master\nmark " + inline.getMarkID() + "\n" +
				"committer Me Tester <Me.tester@domain.com> 1359395 -0500\n" +
				"data 11\nInline Data\n" +
				"from refs/heads/master^0\n" +
				"M " + GitFileType.Normal.getOctalRepresentation() + " inline a random file - Copy - Copy.txt\n" +
				"data 39\nThis is a small file\nwith multiple line\n\n",
				new String(byout.toByteArray(), 0, byout.size()));
		assertEquals("master", inline.getReference());
	}

	@Test
	public void testCommitFromSomeone() throws IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		fromSomeone.writeTo(byout);
		
		assertEquals(
				"commit refs/heads/branchA\nmark " + fromSomeone.getMarkID() + "\n" +
				"author Other Tester <Other.Tester@domain.com> 1361195 -0500\n" +
				"committer Me Tester <Me.tester@domain.com> 1361195 -0500\n" +
				"data 15\nAn other branch\n" +
				"D a random file - Copy - Copy.txt\n" +
				"D a random file - Copy.txt\n\n",
				new String(byout.toByteArray(), 0, byout.size()));
		assertEquals("branchA", fromSomeone.getReference());
		
		byout.reset();
		fromSomeone.writeTo(byout);
		assertEquals("", new String(byout.toByteArray(), 0, byout.size()));
	}
	
	@Test
	public void testMergeCommit() throws IOException {
		ByteArrayOutputStream byout = new ByteArrayOutputStream();
		merge.writeTo(byout);
		
		assertEquals(
				"commit refs/heads/master\nmark " + merge.getMarkID() + "\n" +
				"committer Me Tester <Me.tester@domain.com> 1362995 -0500\n" +
				"data 13\nmerge branchA\n" +
				"merge " + fromSomeone.getMarkID() + "\n\n",
				new String(byout.toByteArray(), 0, byout.size()));
		assertEquals("master", merge.getReference());
	}
}
