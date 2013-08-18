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
package com.starbase.starteam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.ossnoize.fakestarteam.FakeFolder;
import org.ossnoize.fakestarteam.FileUtility;
import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.SimpleTypedResourceIDProvider;
import org.ossnoize.fakestarteam.exception.InvalidOperationException;

public class Folder extends Item {

	protected Folder() {
	}
	
	public Folder(Server server) {
		throw new UnsupportedOperationException("Unknown goal for this constructor");
	}

	public Folder(Folder parent, String name, String workingFolder) {
		itemProperties = new Properties();
		// initialize the basic properties of the folder.
		itemProperties.setProperty(propertyKeys.OBJECT_ID, 
				Integer.toString(SimpleTypedResourceIDProvider.getProvider().registerNew(parent.getView(), this)));
		this.parent = parent;
		view = parent.getView();
		setName(name);
		try {
			File storage = InternalPropertiesProvider.getInstance().getStorageLocation();
			String folder = storage.getCanonicalPath() + File.separator + getObjectID();
			holdingPlace = new File(folder);
			validateHoldingPlace();
			loadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
		shareTo(parent);
	}

	protected void validateHoldingPlace() {
		if(null == holdingPlace) {
			throw new InvalidOperationException("Cannot create a folder without an holding place.");
		}
		if(holdingPlace.exists()) {
			if(holdingPlace.isFile()) {
				holdingPlace.delete();
				holdingPlace.mkdirs();
			}
		} else {
			holdingPlace.mkdirs();
		}
	}
	
	public void setName(java.lang.String name) {
		if(itemProperties == null) {
			throw new InvalidOperationException("The properties are not initialized");
		}
		itemProperties.setProperty(propertyKeys.FOLDER_NAME, name);
	}
	
	public String getName() {
		if(itemProperties == null) {
			throw new InvalidOperationException("The properties are not initialized");
		}
		return itemProperties.getProperty(propertyKeys.FOLDER_NAME);
	}
	
	public Folder[] getSubFolders() {
		if(itemProperties == null)
			throw new InvalidOperationException("The properties are not initialized");

		String listOfFolder = itemProperties.getProperty(propertyKeys._CHILD_FOLDER);
		List<Folder> generatedList = new ArrayList<Folder>();
		if(listOfFolder != null && listOfFolder.length() > 0) {
			for(String folderId : listOfFolder.split(";")) {
				if(null != folderId && 0 < folderId.length()) {
					try {
						int id = Integer.parseInt(folderId);
						SimpleTypedResource ressource =
								SimpleTypedResourceIDProvider.getProvider().findExisting(view, id);
						if(null != ressource && ressource instanceof Folder) {
							generatedList.add((Folder)ressource);
						} else {
							Folder child = new FakeFolder(this.view, id, this);
							generatedList.add(child);
						}
					} catch (NumberFormatException ne) {
						throw new InvalidOperationException("Folder child id corrupted.");
					}
				}
			}
		}
		Folder[] buffer = new Folder[generatedList.size()];
		return generatedList.toArray(buffer);
	}

	private com.starbase.starteam.File[] getFiles() {
		if(itemProperties == null)
			throw new InvalidOperationException("The properties are not initialized yet");

		String listOfFile = itemProperties.getProperty(propertyKeys._FILES);
		List<com.starbase.starteam.File> generatedList = new ArrayList<com.starbase.starteam.File>();
		if(listOfFile != null && 0 < listOfFile.length()) {
			for(String fileID : listOfFile.split(";")) {
				if(null != fileID && 0 < fileID.length()) {
					try {
						int id = Integer.parseInt(fileID);
						SimpleTypedResource ressource =
								SimpleTypedResourceIDProvider.getProvider().findExisting(view, id);

						com.starbase.starteam.File aFile = null;
						if(null != ressource && ressource instanceof com.starbase.starteam.File) {
							aFile = ((com.starbase.starteam.File)ressource);
						} else {
							aFile = new com.starbase.starteam.File(id, this.view);
						}
						if((this.view instanceof RecycleBin && aFile.isDeleted()) || isFromHistory() ||
						   !((this.view instanceof RecycleBin) || aFile.isDeleted())) {
							generatedList.add(aFile);
						}
					} catch (NumberFormatException ne) {
						ne.printStackTrace();
						throw new InvalidOperationException("Folder child id corrupted.");
					}
				}
			}
		}
		com.starbase.starteam.File[] buffer = new com.starbase.starteam.File[generatedList.size()];
		return generatedList.toArray(buffer);
	}
	
	public Item[] getItems(java.lang.String typeName) {
		if(typeName.equalsIgnoreCase(getTypeNames().FOLDER))
			return getSubFolders();
		else if (typeName.equalsIgnoreCase(getTypeNames().FILE)) {
			return getFiles();
		}
		return new Item[0];
	}
	
	@Override
	public Folder getParentFolder() {
		return parent;
	}
	
	@Override
	public void update() {
		if(itemProperties == null) {
			throw new InvalidOperationException("Properties are not initialized yet!!!");
		}
		int version = getRevisionNumber() + 1;
		setRevisionNumber(version);
		setModifiedBy();
		setModifiedTime();
		FileOutputStream fout = null;
		try {
			File storageFolder = new File(holdingPlace.getCanonicalPath() + File.separator + version);
			if(storageFolder.exists())
			{
				throw new Error("Corrupted folder id:" + getObjectID());
			}
			storageFolder.mkdirs();
			
			fout = new FileOutputStream(storageFolder.getCanonicalPath() + File.separator + FOLDER_PROPERTIES);
			itemProperties.store(fout, "Folders properties");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtility.close(fout);
		}
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	protected void loadProperties() {
		FileInputStream fin = null;
		try {
			int lastRevision = findRightRevision(Integer.parseInt(holdingPlace.getName()));
			File folderProperty = new File(holdingPlace.getCanonicalPath() + File.separator + lastRevision + File.separator + FOLDER_PROPERTIES);
			if(folderProperty.exists()) {
				fin = new FileInputStream(folderProperty);
				itemProperties.load(fin);
				int id = Integer.parseInt(itemProperties.getProperty(propertyKeys.OBJECT_ID));
				int viewid = Integer.parseInt(itemProperties.getProperty(propertyKeys._VIEW_ID));
				if(viewid != view.getID()) {
					throw new InvalidOperationException("The object is not registered on this view");
				}
				SimpleTypedResourceIDProvider.getProvider().registerExisting(view, id, this);

				if(null == parent) {
					SimpleTypedResource parent = SimpleTypedResourceIDProvider.getProvider().findExisting(view, getParentObjectID());
					if(parent instanceof Folder) {
						this.parent = (Folder)parent;
					} else if(getParentObjectID() != 0) {
						this.parent = new FakeFolder(this.view, getParentObjectID(), null);
					}
				}
			} else {
				// initialize the basic properties of the folder.
				if (null != parent)	{
					itemProperties.setProperty(propertyKeys.PARENT_OBJECT_ID, Integer.toString(parent.getObjectID()));
					buildParentPath();
				} else {
					itemProperties.setProperty(propertyKeys.FOLDER_PATH, getName());
				}
				itemProperties.setProperty(propertyKeys._VIEW_ID, Integer.toString(view.getID()));
				update();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtility.close(fin);
		}
	}

	@Override
	public Item shareTo(Folder folder) {
		StringBuffer childIdList = null;
		if(folder.itemProperties.containsKey(propertyKeys._CHILD_FOLDER)) {
			childIdList = new StringBuffer(folder.itemProperties.getProperty(propertyKeys._CHILD_FOLDER)).append(";");
		} else {
			childIdList = new StringBuffer(25);
		}
		childIdList.append(getObjectID());
		folder.itemProperties.setProperty(propertyKeys._CHILD_FOLDER, childIdList.toString());
		folder.update();
		return super.shareTo(folder);
	}
	
	@Override
	public void moveTo(Folder folder) {
		Folder origin = getParentFolder();
		super.moveTo(folder);
		
		// Fixup the old parent and the child folder path.
		String thisStringId = Integer.toString(getObjectID());
		if(origin.itemProperties.containsKey(propertyKeys._CHILD_FOLDER)) {
			StringBuffer idList = new StringBuffer(origin.itemProperties.getProperty(propertyKeys._CHILD_FOLDER));
			int start = idList.indexOf(thisStringId);
			idList.delete(start, start+thisStringId.length());
			origin.itemProperties.setProperty(propertyKeys._CHILD_FOLDER, idList.toString());
			origin.update();
		}
		buildParentPath();
		update();
	}
	
	@Override
	public void remove() {
		String childs = parent.itemProperties.getProperty(propertyKeys._CHILD_FOLDER);
		String without = childs.replace(Integer.toString(getObjectID()), "");
		parent.itemProperties.setProperty(propertyKeys._CHILD_FOLDER, without);
		parent.update();
		for(com.starbase.starteam.File f : getFiles()) {
			f.remove();
		}
		for(Folder f : getSubFolders()) {
			f.remove();
		}
		super.remove();
	}
	
	private void buildParentPath() {
		itemProperties.setProperty(propertyKeys.FOLDER_PATH, 
				parent.getParentFolderQualifiedName() + File.separatorChar + getName());
		for(Folder f : getSubFolders()) {
			f.buildParentPath();
			f.update();
		}
	}

	public java.lang.String getFolderHierarchy() {
		if(null != parent) {
			return parent.getFolderHierarchy() + getName() + File.separator;
		}
		return getName() + File.separator;
	}
	
	@Override
	public Type getType() {
		return new Type(getTypeNames().FOLDER, view.getServer());
	}

	public void populateNow(String file, String[] populateProps, int i) {
		// Nothing to do specially as in fake-starteam, most of the stuff are not on a remote server far away.
	}

	public void discardItems(String file, int i) {
		// Nothing special to do unless we have some memory retension issue.
	}
}
