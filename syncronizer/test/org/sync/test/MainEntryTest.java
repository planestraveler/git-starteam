package org.sync.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.builder.StarteamProjectBuilder;
import org.ossnoize.git.fastimport.enumeration.GitFileType;
import org.sync.MainEntry;
import org.sync.RepositoryHelper;
import org.sync.RepositoryHelperFactory;
import org.sync.util.FileUtility;
import org.sync.util.LogEntry;
import org.sync.util.SmallRef;
import org.sync.util.LogEntry.FileEntry;

public class MainEntryTest {

	private static File importLocation;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String baseFolder = System.getProperty("java.io.tmpdir") + File.separator + "repository";
		importLocation = new File(baseFolder);
		if(importLocation.exists())
		{
			FileUtility.rmDir(importLocation);
		}
		InternalPropertiesProvider.getInstance().setBaseStorageLocation(baseFolder);
		StarteamProjectBuilder.main(new String[] {"UnitTest"});
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//FileUtility.rmDir(importLocation);
	}

	@Test
	public void testLabelImport() throws IOException {
		MainEntry.main(new String[] {
				"-h", "localhost", "-P", "23456", "-U", "Test", "--password=passw0rd", "-p", "UnitTest", "-v", "MAIN",
				"-d", "test.com", "-c", "-L", "-W", importLocation.getAbsolutePath(), "--verbose"
				});
		//TODO: Validate the logs to see if everything is all right.
		RepositoryHelperFactory.getFactory().setCreateRepo(false);
		RepositoryHelper helper = RepositoryHelperFactory.getFactory().createHelper();
		
		List<LogEntry> entries = helper.getCommitLog(new SmallRef("MAIN"));
		Collections.reverse(entries);
		Iterator<LogEntry> i = entries.iterator();
		
		LogEntry entry = i.next();
		assertEquals("First version of glsl mesa lexer", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/cpp/mesa/glsl/glsl_lexer.ll", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
		
		entry = i.next();
		assertEquals("The initial version of the sconstruct file of mesa", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/scons/SConstruct", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());

		entry = i.next();
		assertEquals("This class represent the File class exist in starteam", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/java/starteam/File.java", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());

		entry = i.next();
		assertEquals("This class represent the Item class exist in starteam", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/java/starteam/Item.java", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());

		entry = i.next();
		assertEquals("This class represent the Project class exist in starteam", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/java/starteam/Project.java", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());

		// Fallback to the file description
		entry = i.next();
		assertEquals("Readme file for the project", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("doc/README", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());
		
		// merge the file without comment with the current committed files.
		
		entry = i.next();
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

		entry = i.next();
		assertEquals("Parser should always be with lexer", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/cpp/mesa/glsl/glsl_parser.yy", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());

		entry = i.next();
		assertEquals("Updated lexer", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/cpp/mesa/glsl/glsl_lexer.ll", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());

		entry = i.next();
		assertEquals("Basic construction file", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/cpp/wine/msvcp100/Makefile.in", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());

		entry = i.next();
		assertEquals("Stub of msvcp100 dlls", entry.getComment());
		assertEquals(1, entry.getFilesEntry().size());
		assertEquals("src/cpp/wine/msvcp100/msvcp100.c", entry.getFilesEntry().get(0).getPath());
		assertEquals(GitFileType.NullFile, entry.getFilesEntry().get(0).getFromType());
		assertEquals(GitFileType.Normal, entry.getFilesEntry().get(0).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition, entry.getFilesEntry().get(0).getTypeOfModification());
		assertEquals("Test <Test@test.com>", entry.getAuthor());

		entry = i.next();
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

		// This is an actual issue with the rename, the file have moved without modification so we don't
		// have any more information than the original comment of the last modification.
		entry = i.next();
		//assertEquals("",                                  entry.getComment());
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

		entry = i.next();
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
		
		entry = i.next();
		assertEquals("",                                       entry.getComment());
		assertEquals(1,                                        entry.getFilesEntry().size());
		index = 0;
		assertEquals("src/scons/SConstruct",                   entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.NullFile,                     entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Delete,       entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());
		
		entry = i.next();
		assertEquals("Added getParentFolder property",         entry.getComment());
		assertEquals(1,                                        entry.getFilesEntry().size());
		index = 0;
		assertEquals("src/java/starteam/Item.java",            entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());

		entry = i.next();
		assertEquals("Added subfolder listing capacity",       entry.getComment());
		assertEquals(1,                                        entry.getFilesEntry().size());
		index = 0;
		assertEquals("src/java/starteam/Folder.java",          entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());

		entry = i.next();
		assertEquals("Class reprensenting the view of starteam",
				                                               entry.getComment());
		assertEquals(1,                                        entry.getFilesEntry().size());
		index = 0;
		assertEquals("src/java/starteam/View.java",            entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.NullFile,                     entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Addition,     entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());

		entry = i.next();
		assertEquals("Improve Logic of project creation",      entry.getComment());
		assertEquals(1,                                        entry.getFilesEntry().size());
		index = 0;
		assertEquals("src/java/starteam/Project.java",         entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());

		entry = i.next();
		assertEquals("Load from history and Return it",        entry.getComment());
		assertEquals(1,                                        entry.getFilesEntry().size());
		index = 0;
		assertEquals("src/java/starteam/File.java",            entry.getFilesEntry().get(index).getPath());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getFromType());
		assertEquals(GitFileType.Normal,                       entry.getFilesEntry().get(index).getToType());
		assertEquals(LogEntry.TypeOfModification.Modification, entry.getFilesEntry().get(index).getTypeOfModification());
		assertEquals("Test <Test@test.com>",                   entry.getAuthor());
	}
	
	public void testTimeImport() {
		// TODO: create repo with the right parameters
		MainEntry.main(new String[] {});
		//TODO: Validate the logs to see if everything is all right.
	}

}
