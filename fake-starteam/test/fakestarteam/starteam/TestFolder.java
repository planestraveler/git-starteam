package fakestarteam.starteam;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ossnoize.fakestarteam.UserProvider;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Item;
import com.starbase.starteam.Project;
import com.starbase.starteam.RecycleBin;
import com.starbase.starteam.Server;
import com.starbase.starteam.View;

public class TestFolder {
	
	static Server server;
	static int UserID;
	static final String TestUserName = "TestFolder1";
	static Project project;
	static View view;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		server = new Server("localhost", 23456);
		UserProvider.getInstance().createNewUser(TestUserName);
		server.connect();
		UserID = server.logOn(TestUserName, null);
		for(Project p : server.getProjects()) {
			project = p;
			for(View v : p.getViews()) {
				view = v;
				break;
			}
			break;
		}
		if(null == project) {
			fail("The test need to find a project to work");
		}
		if(null == view) {
			fail("The test need to find a view to work");
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		view = null;
		project = null;
		server.disconnect();
		server = null;
		UserProvider.getInstance().deleteUser(UserID);
	}

	@Test
	public void testGetType() {
		assertEquals(server.typeForName("Folder"), view.getRootFolder().getType());
	}
	
	public void testRecursiveRecycleBinContent(Folder f) {
		for(Item i : f.getItems(f.getTypeNames().FILE)) {
			assertTrue(i.isDeleted());
		}
		for(Folder sub : f.getSubFolders()) {
			testRecursiveRecycleBinContent(sub);
		}
	}
	
	@Test
	public void testRecycleBinContent() {
		RecycleBin trash = view.getRecycleBin();
		testRecursiveRecycleBinContent(trash.getRootFolder());
	}

}
