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
import java.util.Date;
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
import org.sync.RepositoryHelper;
import org.sync.RepositoryHelperFactory;
import org.sync.commitstrategy.BasePopulationStrategy;
import org.sync.commitstrategy.RevisionPopulationStrategy;
import org.sync.util.CommitInformation;
import org.sync.util.FileUtility;
import org.sync.util.RevisionDateComparator;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Label;
import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.View;
import com.starbase.starteam.ViewConfiguration;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RevisionPopulationTest {

	private static File ImportLocation;
	private static Server ConnectedServer;
	private static View SelectedView;
	private static RepositoryHelper Helper;
	private static List<String> LastFiles;
	private static Date EarliestTime;

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
		EarliestTime = new java.util.Date(0);
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

	private BasePopulationStrategy CommitBuilder;
	private Label[] SelectedViewLabels;

	@Before
	public void setUp() throws Exception {
		CommitBuilder = new RevisionPopulationStrategy(SelectedView);
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
			if (l.isRevisionLabel()) {
				tempList.add(l);
			}
		}
		SelectedViewLabels = tempList.toArray(new Label[tempList.size()]);
		Arrays.sort(SelectedViewLabels, new RevisionDateComparator());
	}

	@After
	public void tearDown() throws Exception {
		LastFiles = CommitBuilder.getLastFiles();
		EarliestTime = new java.util.Date(CommitBuilder.getListOfCommit().lastKey().getTime());
		CommitBuilder = null;
		SelectedViewLabels = null;
	}

	@Test
	public void revisionLabel0() {
		CommitBuilder.setCurrentLabel(SelectedViewLabels[0]);
		View selected = new View(SelectedView, ViewConfiguration.createFromTime(SelectedViewLabels[0].getRevisionTime()));
		Folder root = selected.getRootFolder();
		
		CommitBuilder.filePopulation("MAIN", root);
		NavigableMap<CommitInformation, com.starbase.starteam.File> commitList = CommitBuilder.getListOfCommit();
		
		Iterator<Entry<CommitInformation, com.starbase.starteam.File>> it = commitList.entrySet().iterator();
		assertTrue(it.hasNext());
		Entry<CommitInformation, com.starbase.starteam.File> entry = it.next();

		assertEquals("Basic construction file", entry.getKey().getComment());
		assertEquals("src/cpp/wine/msvcp100/Makefile.in", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/Makefile.in"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertTrue(it.hasNext());
		entry = it.next();

		assertEquals("Stub of msvcp100 dlls", entry.getKey().getComment());
		assertEquals("src/cpp/wine/msvcp100/msvcp100.c", entry.getKey().getPath());
		assertEquals(new Integer(-1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/msvcp100.c"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));

		assertFalse(it.hasNext());
	}

	@Test
	public void revisionLabel1() {
		CommitBuilder.setCurrentLabel(SelectedViewLabels[1]);
		View selected = new View(SelectedView, ViewConfiguration.createFromTime(SelectedViewLabels[0].getRevisionTime()));
		Folder root = selected.getRootFolder();

		CommitBuilder.filePopulation("MAIN", root);
		NavigableMap<CommitInformation, com.starbase.starteam.File> commitList = CommitBuilder.getListOfCommit();

		String nextComment = "Copy files from msvcp90";

		Iterator<Entry<CommitInformation, com.starbase.starteam.File>> it = commitList.entrySet().iterator();
		assertTrue(it.hasNext());
		Entry<CommitInformation, com.starbase.starteam.File> entry = it.next();

		assertEquals(nextComment, entry.getKey().getComment());
		assertEquals("src/cpp/wine/msvcp100/Makefile.in", entry.getKey().getPath());
		assertEquals(new Integer(1), Helper.getRegisteredFileVersion("MAIN", "src/cpp/wine/msvcp100/Makefile.in"));
		assertTrue(Helper.updateFileVersion("MAIN", entry.getKey().getPath(), entry.getValue().getViewVersion(),
		    entry.getValue().getContentVersion()));
	}
}
