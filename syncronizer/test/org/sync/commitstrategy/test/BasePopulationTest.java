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
package org.sync.commitstrategy.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.ProjectProvider;
import org.ossnoize.fakestarteam.SimpleTypedResourceIDProvider;
import org.ossnoize.fakestarteam.UserProvider;
import org.ossnoize.fakestarteam.builder.StarteamProjectBuilder;
import org.sync.CommitPopulationStrategy;
import org.sync.RepositoryHelper;
import org.sync.RepositoryHelperFactory;
import org.sync.commitstrategy.BasePopulationStrategy;
import org.sync.util.CommitInformation;
import org.sync.util.FileUtility;
import org.sync.util.LabelDateComparator;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Label;
import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.View;
import com.starbase.starteam.ViewConfiguration;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasePopulationTest {

	private CommitPopulationStrategy CommitBuilder;
	private static Server ConnectedServer;
	private static View SelectedView;
	private static RepositoryHelper Helper;
	private static List<String> LastFiles;
	private static java.util.Date EarliestTime = new java.util.Date(0);
	
	private Label[] SelectedViewLabels;
	private static File ImportLocation;

	@BeforeClass
	public static void setUpClass() throws Exception {
		String baseFolder = Files.createTempDirectory("repository").toString();
		ImportLocation = new File(baseFolder);
		InternalPropertiesProvider.getInstance().setBaseStorageLocation(baseFolder);
		StarteamProjectBuilder.main(new String[] { "UnitTest", "1", "9" });
		
		ConnectedServer = new Server("localhost", 23456);
		ConnectedServer.connect();
		ConnectedServer.logOn("Test", "passw0rd");
		for (Project p : ConnectedServer.getProjects()) {
			if (p.getName().equalsIgnoreCase("UnitTest")) {
				for (View v : p.getViews()) {
					if (v.getName().equalsIgnoreCase("MAIN")) {
						SelectedView = v;
					}
				}
			}
		}
		RepositoryHelperFactory.getFactory().setCreateRepo(true);
		Helper = RepositoryHelperFactory.getFactory().createHelper();
	}

	@Before
	public void setUp() throws Exception {
		CommitBuilder = new BasePopulationStrategy(SelectedView);
		CommitBuilder.setRepositoryHelper(Helper);
		CommitBuilder.setVerboseLogging(true);
		// On resume the time shall be set as the last commit. Otherwise it shall be
		// set internally for the last file modification done
		CommitBuilder.setLastCommitTime(EarliestTime);
		// since we start each step with a new instance. We need to provide the
		// initial set of path present in the repository
		if (LastFiles != null) {
			TreeSet<String> initialSetOfFiles = new TreeSet<String>();
			initialSetOfFiles.addAll(LastFiles);
			CommitBuilder.setInitialPathList(initialSetOfFiles);
		}

		ArrayList<Label> tempList = new ArrayList<Label>();
		for (Label l : SelectedView.getActiveLabels()) {
			if (l.isViewLabel()) {
				tempList.add(l);
			}
		}
		SelectedViewLabels = tempList.toArray(new Label[tempList.size()]);
		Arrays.sort(SelectedViewLabels, new LabelDateComparator());
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		Helper = null;
		SelectedView = null;
		ConnectedServer = null;
		FileUtility.rmDir(ImportLocation);
		SimpleTypedResourceIDProvider.deleteProvider();
		UserProvider.deleteInstance();
		InternalPropertiesProvider.deleteInstace();
		ProjectProvider.deleteInstance();
	}

	@After
	public void tearDown() throws Exception {
		LastFiles = CommitBuilder.getLastFiles();
		EarliestTime = new java.util.Date(CommitBuilder.getListOfCommit().lastKey().getTime());
		CommitBuilder = null;
		SelectedViewLabels = null;
	}

	@Test
	public void testCommitInformationOfLabel0() {
		int labelId = SelectedViewLabels[0].getID();
		View selectedConfiguration = new View(SelectedView, ViewConfiguration.createFromLabel(labelId));
		Folder root = selectedConfiguration.getRootFolder();

		CommitBuilder.filePopulation("MAIN", root);
		NavigableMap<CommitInformation, com.starbase.starteam.File> testCommitSet = CommitBuilder.getListOfCommit();

		Iterator<Entry<CommitInformation, com.starbase.starteam.File>> it = testCommitSet.entrySet().iterator();
		assertTrue(it.hasNext());

		Entry<CommitInformation, com.starbase.starteam.File> entry = it.next();
		assertEquals(entry.getKey().getComment(), "First version of glsl mesa lexer");
		assertEquals(entry.getKey().getPath(), "src/cpp/mesa/glsl/glsl_lexer.ll");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/mesa/glsl/glsl_lexer.ll"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), "The initial version of the sconstruct file of mesa");
		assertEquals(entry.getKey().getPath(), "src/scons/SConstruct");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/scons/SConstruct"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), "This class represent the File class exist in starteam");
		assertEquals(entry.getKey().getPath(), "src/java/starteam/File.java");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/java/starteam/File.java"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), "This class represent the Item class exist in starteam");
		assertEquals(entry.getKey().getPath(), "src/java/starteam/Item.java");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/java/starteam/Item.java"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), "This class represent the Project class exist in starteam");
		assertEquals(entry.getKey().getPath(), "src/java/starteam/Project.java");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/java/starteam/Project.java"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertFalse(it.hasNext());
	}

	@Test
	public void testCommitInformationOfLabel2() {
		int labelId = SelectedViewLabels[2].getID();
		View selectedConfiguration = new View(SelectedView, ViewConfiguration.createFromLabel(labelId));
		Folder root = selectedConfiguration.getRootFolder();

		CommitBuilder.filePopulation("MAIN", root);
		NavigableMap<CommitInformation, com.starbase.starteam.File> testCommitSet = CommitBuilder.getListOfCommit();

		Iterator<Entry<CommitInformation, com.starbase.starteam.File>> it = testCommitSet.entrySet().iterator();

		assertTrue(it.hasNext());

		Entry<CommitInformation, com.starbase.starteam.File> entry = it.next();
		assertEquals(entry.getKey().getComment(), "Readme file for the project");
		assertEquals(entry.getKey().getPath(), "doc/README");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "doc/README"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		String nextComment = "Upgrade the version";
		entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/java/starteam/File.java");
		assertEquals(new Integer(1), Helper.getRegisteredFileVersion("MAIN", "src/java/starteam/File.java"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/java/starteam/Folder.java");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/java/starteam/Folder.java"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/java/starteam/Item.java");
		assertEquals(new Integer(1), Helper.getRegisteredFileVersion("MAIN", "src/java/starteam/Item.java"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertFalse(it.hasNext());
	}

	@Test
	public void testCommitInformationOfLabel3() {
		int labelId = SelectedViewLabels[3].getID();
		View selectedConfiguration = new View(SelectedView, ViewConfiguration.createFromLabel(labelId));
		Folder root = selectedConfiguration.getRootFolder();

		CommitBuilder.filePopulation("MAIN", root);
		NavigableMap<CommitInformation, com.starbase.starteam.File> testCommitSet = CommitBuilder.getListOfCommit();

		Iterator<Entry<CommitInformation, com.starbase.starteam.File>> it = testCommitSet.entrySet().iterator();

		assertTrue(it.hasNext());

		Entry<CommitInformation, com.starbase.starteam.File> entry = it.next();
		assertEquals(entry.getKey().getComment(), "Parser should always be with lexer");
		assertEquals(entry.getKey().getPath(), "src/cpp/mesa/glsl/glsl_parser.yy");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/mesa/glsl/glsl_parser.yy"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), "Updated lexer");
		assertEquals(entry.getKey().getPath(), "src/cpp/mesa/glsl/glsl_lexer.ll");
		assertEquals(new Integer(1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/mesa/glsl/glsl_lexer.ll"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertFalse(it.hasNext());
	}

	@Test
	public void testCommitInformationOfLabel4() {
		int labelId = SelectedViewLabels[4].getID();
		View selectedConfiguration = new View(SelectedView, ViewConfiguration.createFromLabel(labelId));
		Folder root = selectedConfiguration.getRootFolder();

		CommitBuilder.filePopulation("MAIN", root);
		NavigableMap<CommitInformation, com.starbase.starteam.File> testCommitSet = CommitBuilder.getListOfCommit();

		Iterator<Entry<CommitInformation, com.starbase.starteam.File>> it = testCommitSet.entrySet().iterator();

		assertTrue(it.hasNext());

		Entry<CommitInformation, com.starbase.starteam.File> entry = it.next();
		assertEquals(entry.getKey().getComment(), "Basic construction file");
		assertEquals(entry.getKey().getPath(), "src/cpp/wine/msvcp100/Makefile.in");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/Makefile.in"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), "Stub of msvcp100 dlls");
		assertEquals(entry.getKey().getPath(), "src/cpp/wine/msvcp100/msvcp100.c");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/msvcp100.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertFalse(it.hasNext());
	}

	@Test
	public void testCommitInformationOfLabel5() {
		int labelId = SelectedViewLabels[5].getID();
		View selectedConfiguration = new View(SelectedView, ViewConfiguration.createFromLabel(labelId));
		Folder root = selectedConfiguration.getRootFolder();

		CommitBuilder.filePopulation("MAIN", root);
		NavigableMap<CommitInformation, com.starbase.starteam.File> testCommitSet = CommitBuilder.getListOfCommit();

		Iterator<Entry<CommitInformation, com.starbase.starteam.File>> it = testCommitSet.entrySet().iterator();

		assertTrue(it.hasNext());

		String nextComment = "Copy files from msvcp90";
		Entry<CommitInformation, com.starbase.starteam.File> entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/cpp/wine/msvcp100/Makefile.in");
		assertEquals(new Integer(1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/Makefile.in"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/cpp/wine/msvcp100/cxx.h");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/cxx.h"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/cpp/wine/msvcp100/exception.c");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/exception.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/cpp/wine/msvcp100/ios.c");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/ios.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/cpp/wine/msvcp100/locale.c");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/locale.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/cpp/wine/msvcp100/math.c");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/math.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/cpp/wine/msvcp100/memory.c");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/memory.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/cpp/wine/msvcp100/misc.c");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/misc.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/cpp/wine/msvcp100/msvcp.h");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/msvcp.h"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(entry.getKey().getComment(), nextComment);
		assertEquals(entry.getKey().getPath(), "src/cpp/wine/msvcp100/string.c");
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/string.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertFalse(it.hasNext());
	}

	@Test
	public void testCommitInformationOfLabel7() {
		int labelId = SelectedViewLabels[7].getID();
		View selectedConfiguration = new View(SelectedView, ViewConfiguration.createFromLabel(labelId));
		Folder root = selectedConfiguration.getRootFolder();

		CommitBuilder.filePopulation("MAIN", root);
		NavigableMap<CommitInformation, com.starbase.starteam.File> testCommitSet = CommitBuilder.getListOfCommit();
		assertEquals(0, CommitBuilder.pathToDelete().size());

		Iterator<Entry<CommitInformation, com.starbase.starteam.File>> it = testCommitSet.entrySet().iterator();

		assertTrue(it.hasNext());

		String nextComment = "Unexpected Move";
		Entry<CommitInformation, com.starbase.starteam.File> entry = it.next();
		assertEquals(nextComment, entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/Makefile.in", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/Makefile.in"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(nextComment, entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/cxx.h", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/cxx.h"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(nextComment, entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/exception.c", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/exception.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(nextComment, entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/ios.c", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/ios.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(nextComment, entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/locale.c", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/locale.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(nextComment, entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/math.c", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/math.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(nextComment, entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/memory.c", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/memory.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(nextComment, entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/misc.c", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/misc.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(nextComment, entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/msvcp.h", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/msvcp.h"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(nextComment, entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/msvcp100.c", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/msvcp100.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals(nextComment, entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/string.c", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/string.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals("", entry.getKey().getComment());
		assertEquals("src/cpp/wine/msvcp100/Makefile.in", entry.getKey().getPath());
		assertEquals(new Integer(2), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/Makefile.in"));
		assertTrue(entry.getKey().isFileDelete());
		Helper.unregisterFileId("MAIN", entry.getKey().getPath());

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals("", entry.getKey().getComment());
		assertEquals("src/cpp/wine/msvcp100/cxx.h", entry.getKey().getPath());
		assertEquals(new Integer(1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/cxx.h"));
		assertTrue(entry.getKey().isFileDelete());
		Helper.unregisterFileId("MAIN", entry.getKey().getPath());

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals("", entry.getKey().getComment());
		assertEquals("src/cpp/wine/msvcp100/exception.c", entry.getKey().getPath());
		assertEquals(new Integer(1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/exception.c"));
		assertTrue(entry.getKey().isFileDelete());
		Helper.unregisterFileId("MAIN", entry.getKey().getPath());

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals("", entry.getKey().getComment());
		assertEquals("src/cpp/wine/msvcp100/ios.c", entry.getKey().getPath());
		assertEquals(new Integer(1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/ios.c"));
		assertTrue(entry.getKey().isFileDelete());
		Helper.unregisterFileId("MAIN", entry.getKey().getPath());
	}

	@Test
	public void testCommitInformationOfLabel9() {
		int labelId = SelectedViewLabels[9].getID();
		View selectedConfiguration = new View(SelectedView, ViewConfiguration.createFromLabel(labelId));
		Folder root = selectedConfiguration.getRootFolder();

		CommitBuilder.filePopulation("MAIN", root);
		NavigableMap<CommitInformation, com.starbase.starteam.File> testCommitSet = CommitBuilder.getListOfCommit();
		assertEquals(0, CommitBuilder.pathToDelete().size());

		Iterator<Entry<CommitInformation, com.starbase.starteam.File>> it = testCommitSet.entrySet().iterator();

		assertTrue(it.hasNext());

		Entry<CommitInformation, com.starbase.starteam.File> entry = it.next();
		assertEquals("Fixed stream-off size definition", entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/ios.c", entry.getKey().getPath());
		assertEquals(new Integer(1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/ios.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals("Fixed stream-off size definition", entry.getKey().getComment());
		assertEquals("src/c/wine/msvcp100/msvcp.h", entry.getKey().getPath());
		assertEquals(new Integer(1), Helper.getRegisteredFileVersion("MAIN", "src/c/wine/msvcp100/msvcp.h"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());

		entry = it.next();
		assertEquals("", entry.getKey().getComment());
		assertEquals("src/scons/SConstruct", entry.getKey().getPath());
		assertTrue(entry.getKey().isFileDelete());
		Helper.unregisterFileId("MAIN", "src/scons/SConstruct");

		assertFalse(it.hasNext());
	}
	
}
