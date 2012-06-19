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
package org.ossnoize.fakestarteam;

import java.io.Serializable;
import java.util.Date;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Project;
import com.starbase.starteam.View;
import com.starbase.util.OLEDate;

public class SerializableView extends View implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4548267790199736069L;
	
	private View parent;
	private String name;
	private String description;
	private String defaultWorkingFolder;
	private int id;
	private int rootFolderId;
	private Date createdDate;
	private int createdBy;
	private transient Project project;
	private transient Folder rootFolder;
	
	protected SerializableView() {
	}

	public SerializableView(View parent, String name, String description, String defaultWorkingFolder) {
		super(parent, name, description, defaultWorkingFolder);
		this.parent = parent;
		this.name = name;
		this.description = description;
		this.defaultWorkingFolder = defaultWorkingFolder;
		this.id = SimpleTypedResourceIDProvider.getProvider().registerNew(this);
		this.createdDate = new Date();
		this.createdBy = InternalPropertiesProvider.getInstance().getCurrentServer().getMyUserAccount().getID();
		if(null != parent) {
			project = parent.getProject();
			if(project instanceof SerializableProject) {
				((SerializableProject)project).addNewView(this);
			}
		}
		rootFolder = new FakeFolder(this, 0, null);
		rootFolderId = rootFolder.getObjectID();
	}

	@Override
	public int getID() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void update() {
		if(null == project) {
			throw new UnsupportedOperationException("Need an active project to be set");
		}
		project.update();
	}

	void setProject(Project serializableProject) {
		this.project = serializableProject;
		SimpleTypedResourceIDProvider.getProvider().registerExisting(id, this);
	}
	
	@Override
	public Project getProject() {
		return project;
	}
	
	@Override
	public View getParentView() {
		return parent;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String getDefaultPath() {
		return defaultWorkingFolder;
	}
	
	@Override
	public void setDefaultPath(String path) {
		defaultWorkingFolder = path;
	}
	
	@Override
	public OLEDate getCreatedTime() {
		if(null == createdDate) {
			// TODO: Remove this patch for old serialized views.
			createdDate = new Date(System.currentTimeMillis());
			update();
		}
		return new OLEDate(createdDate);
	}
	
	@Override
	public Folder getRootFolder() {
		if(null == rootFolder) {
			rootFolder = new FakeFolder(this, rootFolderId, null);
		}
		return rootFolder;
	}
}
