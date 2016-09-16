package org.sync.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.ProjectProvider;
import org.ossnoize.fakestarteam.SimpleTypedResourceIDProvider;
import org.ossnoize.fakestarteam.UserProvider;
import org.ossnoize.fakestarteam.builder.StarteamProjectBuilder;
import org.ossnoize.git.fastimport.GitAttributes;
import org.ossnoize.git.fastimport.enumeration.GitFileType;
import org.sync.MainEntry;
import org.sync.RepositoryHelper;
import org.sync.RepositoryHelperFactory;
import org.sync.util.FileUtility;
import org.sync.util.LogEntry;
import org.sync.util.SmallRef;

public class MainEntryTest {

	private static File importLocation;
	@Before
	public void setUp() throws Exception {
		String baseFolder = Files.createTempDirectory("repository").toString();
		importLocation = new File(baseFolder);
		InternalPropertiesProvider.getInstance().setBaseStorageLocation(baseFolder);
	}

	@After
	public void tearDown() throws Exception {
		FileUtility.rmDir(importLocation);
		SimpleTypedResourceIDProvider.deleteProvider();
		UserProvider.deleteInstance();
		InternalPropertiesProvider.deleteInstace();
		ProjectProvider.deleteInstance();
		RepositoryHelperFactory.deleteFactory();
	}

	@Test
	public void testLabelImport() throws IOException {
		StarteamProjectBuilder.main(new String[] {"UnitTest", "1", "10"});
		MainEntry.main(new String[] {
				"-h", "localhost", "-P", "23456", "-U", "Test", "--password=passw0rd", "-p", "UnitTest", "-v", "MAIN",
				"-d", "test.com", "-c", "-L", "-W", importLocation.getAbsolutePath(), "--verbose"
				});

		RepositoryHelperFactory.getFactory().setCreateRepo(false);
		RepositoryHelper helper = RepositoryHelperFactory.getFactory().createHelper();
		
		List<LogEntry> entries = helper.getCommitLog(new SmallRef("MAIN"));
		Collections.reverse(entries);
		Iterator<LogEntry> i = entries.iterator();
		
		assertCommit01(i.next());
		assertCommit02(i.next());
		assertCommit03(i.next());
		assertCommit04(i.next());
		assertCommit05(i.next());
		assertCommit06(i.next());
		assertCommit07(i.next());
		assertCommit08(i.next());
		assertCommit09(i.next());
		assertCommit10(i.next());
		assertCommit11(i.next());
		assertCommit12(i.next());
		assertCommit13(i.next());
		assertCommit14(i.next());
		assertCommit15(i.next());
		assertCommit16(i.next());
		assertCommit17(i.next());
		assertCommit18(i.next());
		assertCommit19(i.next());
		assertCommit20(i.next());
		assertFalse(i.hasNext());
    String repoInformation = importLocation.getAbsolutePath() + File.separator + "starteam" + File.separator + "StarteamFileInfo.gz";
    assertTrue(new File(repoInformation).exists());
	}
	
	@Test
	public void testLabelWithResume() throws IOException {
		StarteamProjectBuilder.main(new String[] {"UnitTest", "1", "5"});
		MainEntry.main(new String[] {
				"-h", "localhost", "-P", "23456", "-U", "Test", "--password=passw0rd", "-p", "UnitTest", "-v", "MAIN",
				"-d", "test.com", "-c", "-L", "-W", importLocation.getAbsolutePath(), "--verbose"
				});
		
		RepositoryHelperFactory.getFactory().setCreateRepo(false);
		RepositoryHelper helper = RepositoryHelperFactory.getFactory().createHelper();
		
		List<LogEntry> entries = helper.getCommitLog(new SmallRef("MAIN"));
		Collections.reverse(entries);
		Iterator<LogEntry> i = entries.iterator();

		assertCommit01(i.next());
		assertCommit02(i.next());
		assertCommit03(i.next());
		assertCommit04(i.next());
		assertCommit05(i.next());
		assertCommit06(i.next());
		assertCommit07(i.next());
		assertCommit08(i.next());
		assertCommit09(i.next());
		assertCommit10(i.next());
		assertCommit11(i.next());
		assertFalse(i.hasNext());
		
		StarteamProjectBuilder.main(new String[] {"UnitTest", "6", "10"});

		MainEntry.main(new String[] {
				"-h", "localhost", "-P", "23456", "-U", "Test", "--password=passw0rd", "-p", "UnitTest", "-v", "MAIN",
				"-d", "test.com", "-c", "-L", "-W", importLocation.getAbsolutePath(), "--verbose", "-R"
				});
		
		entries = helper.getCommitLog(new SmallRef("MAIN"));
		Collections.reverse(entries);
		i = entries.iterator();
		
		assertCommit01(i.next());
		assertCommit02(i.next());
		assertCommit03(i.next());
		assertCommit04(i.next());
		assertCommit05(i.next());
		assertCommit06(i.next());
		assertCommit07(i.next());
		assertCommit08(i.next());
		assertCommit09(i.next());
		assertCommit10(i.next());
		assertCommit11(i.next());
		assertCommit12(i.next());
		assertCommit13(i.next());
		assertCommit14(i.next());
		assertCommit15(i.next());
		assertCommit16(i.next());
		assertCommit17(i.next());
		assertCommit18(i.next());
		assertCommit19(i.next());
		assertCommit20(i.next());
		assertFalse(i.hasNext());
	}

  @Test
  public void testLabelFromDateToTip() throws IOException {
    StarteamProjectBuilder.main(new String[] {"UnitTest", "1", "10"});
    
    MainEntry.main(new String[] {
      "-h", "localhost", "-P", "23456", "-U", "Test", "--password=passw0rd", "-p", "UnitTest", "-v", "MAIN",
      "-d", "test.com", "-c", "-L", "-W", importLocation.getAbsolutePath(), "--verbose", "-t", "2010-07-14 00:00:00"
    });
    
    RepositoryHelperFactory.getFactory().setCreateRepo(false);
    RepositoryHelper helper = RepositoryHelperFactory.getFactory().createHelper();
    
    List<LogEntry> entries = helper.getCommitLog(new SmallRef("MAIN"));
    Collections.reverse(entries);
    
    Iterator<LogEntry> i = entries.iterator();
    assertCommit02(i.next());
    assertCommit05(i.next());
    assertCommit06(i.next());
    assertCommit07Alt(i.next());
    assertCommit08(i.next());
    assertCommit09Alt(i.next());
    assertCommit10(i.next());
    assertCommit11(i.next());
    assertCommit12(i.next());
    assertCommit13(i.next());
    assertCommit14(i.next());
    assertCommit15(i.next());
    assertCommit16(i.next());
    assertCommit17(i.next());
    assertCommit18(i.next());
    assertCommit19(i.next());
    assertCommit20(i.next());
    assertFalse(i.hasNext());
  }
  
  @Test
  public void testLabelWithLFS() throws IOException {
    StarteamProjectBuilder.main(new String[] {"UnitTest", "11", "13"});

    MainEntry.main(new String[] {
      "-h", "localhost", "-P", "23456", "-U", "Test", "--password=passw0rd", "-p", "UnitTest", "-v", "MAIN",
      "-d", "test.com", "-c", "-L", "-W", importLocation.getAbsolutePath(), "--verbose",
      "--lfs-size", "32M", "--lfs-pattern", ".*(tar.xz|tar.gz|zip|7z|rar)$"
    });
    
    File lfsBoost155 = new File(importLocation.getAbsolutePath() + File.separator 
      + "lfs" + File.separator + "objects" + File.separator + "ff" + File.separator
      + "f0" + File.separator + "fff00023dd79486d444c8e29922f4072e1d451fc5a4d2b6075852ead7f2b7b52");
    assertTrue(lfsBoost155.exists());
    
    File lfsBoost156 = new File(importLocation.getAbsoluteFile() + File.separator
      + "lfs" + File.separator + "objects" + File.separator + "13" + File.separator
      + "47" + File.separator + "134732acaf3a6e7eba85988118d943f0fa6b7f0850f65131fff89823ad30ff1d");
    assertTrue(lfsBoost156.exists());
    
    File lfsFileRoller = new File(importLocation.getAbsoluteFile() + File.separator
      + "lfs" + File.separator + "objects" + File.separator + "09" + File.separator
      + "a6" + File.separator + "09a68db98aa33febdad76e6977d7771b3b444da7f57ab6f4fe7d6a77e39d5a91");
    assertTrue(lfsFileRoller.exists());

    RepositoryHelperFactory.getFactory().setCreateRepo(false);
    RepositoryHelper helper = RepositoryHelperFactory.getFactory().createHelper();


    List<LogEntry> entries = helper.getCommitLog(new SmallRef("MAIN"));
    Collections.reverse(entries);
    Iterator<LogEntry> i = entries.iterator();

    assertCommitLFS1(i.next());
    assertCommitLFS2(i.next());
    assertCommitLFS3(i.next());
    assertCommitLFS4(i.next());
    assertCommitLFS5(i.next());

    assertFalse(i.hasNext());
    ByteArrayOutputStream attributesFile = new ByteArrayOutputStream(4096);
    helper.getFileContent("MAIN", ".gitattributes", attributesFile);

    GitAttributes attributes = new GitAttributes();
    attributes.parse(new ByteArrayInputStream(attributesFile.toByteArray()));

    assertEquals(false, attributes.pathHasAttributes("archive/boost_1_55_0.tar.bz2"));
    assertEquals(true,  attributes.pathHasAttributes("archive/boost_1_56_0.tar.bz2"));
    assertEquals(true,  attributes.pathHasAttributes("archive/file-roller-3.16.5.tar.xz"));
    assertEquals(true,  attributes.pathHasAttributes("archive/ImageMagick-6.9.4-7.tar.xz"));
    assertEquals(true,  attributes.pathHasAttributes("archive/appliance-1.28.1.tar.xz"));
  }
  
  @Test
  public void testFileDeletion() throws IOException {
    
    StarteamProjectBuilder.main(new String[] {"UnitTest", "20", "21"});

    MainEntry.main(new String[] {
      "-h", "localhost", "-P", "23456", "-U", "Test", "--password=passw0rd", "-p", "UnitTest", "-v", "MAIN",
      "-d", "test.com", "-c", "-L", "-W", importLocation.getAbsolutePath(), "--verbose",
      "--lfs-size", "32M", "--lfs-pattern", ".*(tar.xz|tar.gz|zip|7z|rar)$"
    });
    
    RepositoryHelperFactory.getFactory().setCreateRepo(false);
    RepositoryHelper helper = RepositoryHelperFactory.getFactory().createHelper();
    
    List<LogEntry> entries = helper.getCommitLog(new SmallRef("MAIN"));
    Collections.reverse(entries);
    Iterator<LogEntry> i = entries.iterator();
    
    assertCommitFD1(i.next());
    assertCommitFD2(i.next());
    assertCommitFD3(i.next());
    assertFalse(i.hasNext());
  }
  
  @Test
  public void testSubfolderImport() throws IOException
  {
    StarteamProjectBuilder.main(new String[] {"UnitTest", "30", "34"});
    
    MainEntry.main(new String[] {
      "-h", "localhost", "-P", "23456", "-U", "Test", "--password=passw0rd", "-p", "UnitTest", "-v", "MAIN",
      "-d", "test.com", "-c", "-L", "-W", importLocation.getAbsolutePath(), "--verbose", "--folder", "src/java"
    });
    
    RepositoryHelperFactory.getFactory().setCreateRepo(false);
    RepositoryHelper helper = RepositoryHelperFactory.getFactory().createHelper();
    
    List<LogEntry> entries = helper.getCommitLog(new SmallRef("MAIN"));
    Collections.reverse(entries);
    Iterator<LogEntry> i = entries.iterator();
    
    assertSubCommit30(i.next());
    assertSubCommit31(i.next());
    assertSubCommit32(i.next());
    assertSubCommit33(i.next());
    assertSubCommit34(i.next());
    assertFalse(i.hasNext());
  }
  
  private void assertCommitFD1(LogEntry entry) {
    int index = 0;
    assertEquals("A blob representation for git", entry.getComment());
    assertEquals("Test <Test@test.com>",          entry.getAuthor());
    assertEquals(1,                               entry.getFilesEntry().size());
    assertEquals("src/blob.java",                 entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.NullFile,            entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,              entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(index).getTypeOfModification());
  }
  
  private void assertCommitFD2(LogEntry entry) {
    int index = 0; 
    assertEquals("A data representation for git", entry.getComment());
    assertEquals("Test <Test@test.com>",          entry.getAuthor());
    assertEquals(1,                               entry.getFilesEntry().size());
    assertEquals("src/data.java",                 entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.NullFile,            entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,              entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(index).getTypeOfModification());
  }
  
  private void assertCommitFD3(LogEntry entry) {
    int index = 0;
    assertEquals("Correction for better ease of use", entry.getComment());
    assertEquals("Test <Test@test.com>",              entry.getAuthor());
    assertEquals(2,                                   entry.getFilesEntry().size());
    assertEquals("src/blob.java",                     entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(index).getTypeOfModification());
    index++;
    assertEquals("src/data.java",                     entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.NullFile,                entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Delete,  entry.getFilesEntry().get(index).getTypeOfModification());
  }
  
  private void assertCommitLFS1(LogEntry entry) {
    int index = 0;
    assertEquals("Boost version 1.55.0 sources",        entry.getComment());
    assertEquals("Test <Test@test.com>",                entry.getAuthor());
    assertEquals(2,                                     entry.getFilesEntry().size());
    assertEquals(".gitattributes",                      entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.NullFile,                  entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,                    entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Addition,  entry.getFilesEntry().get(index).getTypeOfModification());
    assertEquals("f62641a",                             entry.getFilesEntry().get(index).getToSHA().getRef());
    index++;
    assertEquals("archive/boost_1_55_0.tar.bz2",        entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.NullFile,                  entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,                    entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Addition,  entry.getFilesEntry().get(index).getTypeOfModification());
  }
  
  private void assertCommitLFS2(LogEntry entry) {
    int index = 0;
    assertEquals("Source archive of boost 1.56.0",          entry.getComment());
    assertEquals("Test <Test@test.com>",                    entry.getAuthor());
    assertEquals(3,                                         entry.getFilesEntry().size());
    assertEquals(".gitattributes",                          entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Modification,  entry.getFilesEntry().get(index).getTypeOfModification());
    assertEquals("b9c13de",                                 entry.getFilesEntry().get(index).getToSHA().getRef());
    index++;
    assertEquals("archive/boost_1_55_0.tar.bz2",            entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.NullFile,                      entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Delete,        entry.getFilesEntry().get(index).getTypeOfModification());
    index++;
    assertEquals("archive/boost_1_56_0.tar.bz2",            entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.NullFile,                      entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Addition,      entry.getFilesEntry().get(index).getTypeOfModification());
  }
  
  private void assertCommitLFS3(LogEntry entry) {
    int index = 0;
    assertEquals("Source archive of ImageMagick 6.9.4",     entry.getComment());
    assertEquals("Test <Test@test.com>",                    entry.getAuthor());
    assertEquals(2,                                         entry.getFilesEntry().size());
    assertEquals(".gitattributes",                          entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Modification,  entry.getFilesEntry().get(index).getTypeOfModification());
    index++;
    assertEquals("archive/ImageMagick-6.9.4-7.tar.xz",     entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.NullFile,                      entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Addition,      entry.getFilesEntry().get(index).getTypeOfModification());
  }
  
  private void assertCommitLFS4(LogEntry entry) {
    int index = 0;
    assertEquals("Source archive of appliance",             entry.getComment());
    assertEquals("Test <Test@test.com>",                    entry.getAuthor());
    assertEquals(2,                                         entry.getFilesEntry().size());
    assertEquals(".gitattributes",                          entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Modification,  entry.getFilesEntry().get(index).getTypeOfModification());
    index++;
    assertEquals("archive/appliance-1.28.1.tar.xz",         entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.NullFile,                      entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Addition,      entry.getFilesEntry().get(index).getTypeOfModification());
  }
  
  private void assertCommitLFS5(LogEntry entry) {
    int index = 0;
    assertEquals("Source archive of file-roller 3.16.5",    entry.getComment());
    assertEquals("Test <Test@test.com>",                    entry.getAuthor());
    assertEquals(2,                                         entry.getFilesEntry().size());
    assertEquals(".gitattributes",                          entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Modification,  entry.getFilesEntry().get(index).getTypeOfModification());
    index++;
    assertEquals("archive/file-roller-3.16.5.tar.xz",       entry.getFilesEntry().get(index).getPath());
    assertEquals(GitFileType.NullFile,                      entry.getFilesEntry().get(index).getFromType());
    assertEquals(GitFileType.Normal,                        entry.getFilesEntry().get(index).getToType());
    assertEquals(LogEntry.TypeOfModification.Addition,      entry.getFilesEntry().get(index).getTypeOfModification());
  }
  
	private void assertCommit20(LogEntry entry) {
		int index;
		assertEquals("Load from history and Return it",        entry.getComment());
		assertEquals(1,                                        entry.getFilesEntry().size());
		index = 0;
		assertEquals("src/java/starteam/File.java",            entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());
	}

	private void assertCommit19(LogEntry entry) {
		int index;
		assertEquals("Improve Logic of project creation",      entry.getComment());
		assertEquals(1,                                        entry.getFilesEntry().size());
		index = 0;
		assertEquals("src/java/starteam/Project.java",         entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());
	}

	private void assertCommit18(LogEntry entry) {
		int index;
		assertEquals("Class reprensenting the view of starteam",
				                                               entry.getComment());
		assertEquals(1,                                        entry.getFilesEntry().size());
		index = 0;
		assertEquals("src/java/starteam/View.java",            entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.NullFile,                     entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition,     entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());
	}

	private void assertCommit17(LogEntry entry) {
		int index;
		assertEquals("Added subfolder listing capacity",       entry.getComment());
		assertEquals(1,                                        entry.getFilesEntry().size());
		index = 0;
		assertEquals("src/java/starteam/Folder.java",          entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());
	}

	private void assertCommit16(LogEntry entry) {
		int index;
		assertEquals("Added getParentFolder property",         entry.getComment());
		assertEquals(1,                                        entry.getFilesEntry().size());
		index = 0;
		assertEquals("src/java/starteam/Item.java",            entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());
	}

	private void assertCommit15(LogEntry entry) {
		int index = 0;
		assertEquals("",                                       entry.getComment());
		assertEquals(1,                                        entry.getFilesEntry().size());
		assertEquals("src/scons/SConstruct",                   entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.NullFile,                     entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Delete,       entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());
	}

	private void assertCommit14(LogEntry entry) {
		int index;
		assertEquals("Fixed stream-off size definition",       entry.getComment());
		assertEquals(2,                                        entry.getFilesEntry().size());
		index = 0;
		assertEquals("src/c/wine/msvcp100/ios.c",              entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(index).getTypeOfModification());
		index++;
		assertEquals("src/c/wine/msvcp100/msvcp.h",            entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());
	}

	private void assertCommit13(LogEntry entry) {
		assertEquals("Unexpected Move", entry.getComment());
		assertEquals(11,                                  entry.getFilesEntry().size());
		int index = 0;
		assertEquals("src/c/wine/msvcp100/Makefile.in",   entry.getFilesEntry().get(index).renamedTo());
		assertEquals("src/cpp/wine/msvcp100/Makefile.in", entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Rename,  entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals(100,                                 entry.getFilesEntry().get(index).getDiffRatio());
		index++;
		assertEquals("src/c/wine/msvcp100/cxx.h",         entry.getFilesEntry().get(index).renamedTo());
		assertEquals("src/cpp/wine/msvcp100/cxx.h",       entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Rename,  entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals(100,                                 entry.getFilesEntry().get(index).getDiffRatio());
		index++;
		assertEquals("src/c/wine/msvcp100/exception.c",   entry.getFilesEntry().get(index).renamedTo());
		assertEquals("src/cpp/wine/msvcp100/exception.c", entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Rename,  entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals(100,                                 entry.getFilesEntry().get(index).getDiffRatio());
		index++;
		assertEquals("src/c/wine/msvcp100/ios.c",         entry.getFilesEntry().get(index).renamedTo());
		assertEquals("src/cpp/wine/msvcp100/ios.c",       entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Rename,  entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals(100,                                 entry.getFilesEntry().get(index).getDiffRatio());
		index++;
		assertEquals("src/c/wine/msvcp100/locale.c",      entry.getFilesEntry().get(index).renamedTo());
		assertEquals("src/cpp/wine/msvcp100/locale.c",    entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Rename,  entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals(100,                                 entry.getFilesEntry().get(index).getDiffRatio());
		index++;
		assertEquals("src/c/wine/msvcp100/math.c",        entry.getFilesEntry().get(index).renamedTo());
		assertEquals("src/cpp/wine/msvcp100/math.c",      entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Rename,  entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals(100,                                 entry.getFilesEntry().get(index).getDiffRatio());
		index++;
		assertEquals("src/c/wine/msvcp100/memory.c",      entry.getFilesEntry().get(index).renamedTo());
		assertEquals("src/cpp/wine/msvcp100/memory.c",    entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Rename,  entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals(100,                                 entry.getFilesEntry().get(index).getDiffRatio());
		index++;
		assertEquals("src/c/wine/msvcp100/misc.c",        entry.getFilesEntry().get(index).renamedTo());
		assertEquals("src/cpp/wine/msvcp100/misc.c",      entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Rename,  entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals(100,                                 entry.getFilesEntry().get(index).getDiffRatio());
		index++;
		assertEquals("src/c/wine/msvcp100/msvcp.h",       entry.getFilesEntry().get(index).renamedTo());
		assertEquals("src/cpp/wine/msvcp100/msvcp.h",     entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Rename,  entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals(100,                                 entry.getFilesEntry().get(index).getDiffRatio());
		index++;
		assertEquals("src/c/wine/msvcp100/msvcp100.c",    entry.getFilesEntry().get(index).renamedTo());
		assertEquals("src/cpp/wine/msvcp100/msvcp100.c",  entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Rename,  entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals(100,                                 entry.getFilesEntry().get(index).getDiffRatio());
		index++;
		assertEquals("src/c/wine/msvcp100/string.c",      entry.getFilesEntry().get(index).renamedTo());
		assertEquals("src/cpp/wine/msvcp100/string.c",    entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                  entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Rename,  entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals(100,                                 entry.getFilesEntry().get(index).getDiffRatio());
		index++;
		assertEquals("Test <Test@test.com>",              entry.getAuthor());
	}

	private void assertCommit12(LogEntry entry) {
		assertEquals("Copy files from msvcp90", entry.getComment());
		assertEquals(10, entry.getFilesEntry().size());
		assertEquals("src/cpp/wine/msvcp100/Makefile.in", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("src/cpp/wine/msvcp100/cxx.h", entry.getFilesEntry().get(1).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(1).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(1).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(1).getTypeOfModification());
		assertEquals("src/cpp/wine/msvcp100/exception.c", entry.getFilesEntry().get(2).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(2).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(2).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(2).getTypeOfModification());
		assertEquals("src/cpp/wine/msvcp100/ios.c", entry.getFilesEntry().get(3).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(3).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(3).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(3).getTypeOfModification());
		assertEquals("src/cpp/wine/msvcp100/locale.c", entry.getFilesEntry().get(4).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(4).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(4).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(4).getTypeOfModification());
		assertEquals("src/cpp/wine/msvcp100/math.c", entry.getFilesEntry().get(5).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(5).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(5).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(5).getTypeOfModification());
		assertEquals("src/cpp/wine/msvcp100/memory.c", entry.getFilesEntry().get(6).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(6).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(6).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(6).getTypeOfModification());
		assertEquals("src/cpp/wine/msvcp100/misc.c", entry.getFilesEntry().get(7).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(7).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(7).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(7).getTypeOfModification());
		assertEquals("src/cpp/wine/msvcp100/msvcp.h", entry.getFilesEntry().get(8).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(8).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(8).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(8).getTypeOfModification());
		assertEquals("src/cpp/wine/msvcp100/string.c", entry.getFilesEntry().get(9).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(9).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(9).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(9).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}

	private void assertCommit11(LogEntry entry) {
		assertEquals("Stub of msvcp100 dlls", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/cpp/wine/msvcp100/msvcp100.c", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}

	private void assertCommit10(LogEntry entry) {
		assertEquals("Basic construction file", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/cpp/wine/msvcp100/Makefile.in", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}

	private void assertCommit09(LogEntry entry) {
		assertEquals("Updated lexer", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/cpp/mesa/glsl/glsl_lexer.ll", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}
  
	private void assertCommit09Alt(LogEntry entry) {
		assertEquals("Updated lexer", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/cpp/mesa/glsl/glsl_lexer.ll", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}

	private void assertCommit08(LogEntry entry) {
		assertEquals("Parser should always be with lexer", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/cpp/mesa/glsl/glsl_parser.yy", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}

	private void assertCommit07(LogEntry entry) {
		assertEquals("Upgrade the version", entry.getComment());
		assertEquals(3, entry.getFilesEntry().size());
		assertEquals("src/java/starteam/File.java", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("src/java/starteam/Folder.java", entry.getFilesEntry().get(1).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(1).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(1).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(1).getTypeOfModification());
		assertEquals("src/java/starteam/Item.java", entry.getFilesEntry().get(2).getPath());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(2).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(2).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(2).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}
  
	private void assertCommit07Alt(LogEntry entry) {
		assertEquals("Upgrade the version", entry.getComment());
		assertEquals(3, entry.getFilesEntry().size());
		assertEquals("src/java/starteam/File.java", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("src/java/starteam/Folder.java", entry.getFilesEntry().get(1).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(1).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(1).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(1).getTypeOfModification());
		assertEquals("src/java/starteam/Item.java", entry.getFilesEntry().get(2).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(2).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(2).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(2).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}

	private void assertCommit06(LogEntry entry) {
		assertEquals("Readme file for the project", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("doc/README", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}

	private void assertCommit05(LogEntry entry) {
		assertEquals("This class represent the Project class exist in starteam", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/java/starteam/Project.java", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}

	private void assertCommit04(LogEntry entry) {
		assertEquals("This class represent the Item class exist in starteam", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/java/starteam/Item.java", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}

	private void assertCommit03(LogEntry entry) {
		assertEquals("This class represent the File class exist in starteam", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/java/starteam/File.java", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}

	private void assertCommit02(LogEntry entry) {
		assertEquals("The initial version of the sconstruct file of mesa", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/scons/SConstruct", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}

	private void assertCommit01(LogEntry entry) {
		assertEquals("First version of glsl mesa lexer", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/cpp/mesa/glsl/glsl_lexer.ll", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
	}
	
  private void assertSubCommit30(LogEntry entry) {
    assertEquals("A blob representation for git", entry.getComment());
    assertEquals(1, entry.getFilesEntry().size());
    assertEquals("blob.java", entry.getFilesEntry().get(0).getPath());
    assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
    assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
    assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
    assertEquals("Test <Test@test.com>", entry.getAuthor());
  }
  
  private void assertSubCommit31(LogEntry entry) {
    assertEquals("Fix license", entry.getComment());
    assertEquals(1, entry.getFilesEntry().size());
    assertEquals("blob.java", entry.getFilesEntry().get(0).getPath());
    assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getFromType());
    assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
    assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(0).getTypeOfModification());
    assertEquals("Test <Test@test.com>", entry.getAuthor());
  }
  
  private void assertSubCommit32(LogEntry entry) {
    assertEquals("Add assignation data", entry.getComment());
    assertEquals(1, entry.getFilesEntry().size());
    assertEquals("blob.java", entry.getFilesEntry().get(0).getPath());
    assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getFromType());
    assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
    assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(0).getTypeOfModification());
    assertEquals("Test <Test@test.com>", entry.getAuthor());
  }
  
  private void assertSubCommit33(LogEntry entry) {
    assertEquals("Only write a blob once", entry.getComment());
    assertEquals(1, entry.getFilesEntry().size());
    assertEquals("blob.java", entry.getFilesEntry().get(0).getPath());
    assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getFromType());
    assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
    assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(0).getTypeOfModification());
    assertEquals("Test <Test@test.com>", entry.getAuthor());
  }
  
  private void assertSubCommit34(LogEntry entry) {
    assertEquals("Overriden method should have @Override keyword", entry.getComment());
    assertEquals(1, entry.getFilesEntry().size());
    assertEquals("blob.java", entry.getFilesEntry().get(0).getPath());
    assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getFromType());
    assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
    assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(0).getTypeOfModification());
    assertEquals("Test <Test@test.com>", entry.getAuthor());
  }
	
	public void testTimeImport() {
		// TODO: create repo with the right parameters
		MainEntry.main(new String[] {});
		//TODO: Validate the logs to see if everything is all right.
	}

}
