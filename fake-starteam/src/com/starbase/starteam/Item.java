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
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.ossnoize.fakestarteam.FileUtility;
import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.SimpleTypedResourceIDProvider;
import org.ossnoize.fakestarteam.exception.InvalidOperationException;

import com.starbase.util.OLEDate;

public class Item extends SimpleTypedResource implements ISecurableObject {
	public static interface LockType {
		public static final int UNLOCKED = 0;
		public static final int EXCLUSIVE = 1;
		public static final int NONEXCLUSIVE = 2;
		public static final int BREAK_FLAG = 16;
		public static final int UNCHANGED = 3;
	}

	protected static final PropertyNames propertyKeys = new PropertyNames();
	protected static final PropertyEnums propertyEnums = new PropertyEnums();
	protected static final String FOLDER_PROPERTIES = "folder.properties";
	protected static final String FILE_PROPERTIES = "file.properties";
	
	protected boolean isNew;
	protected boolean isFromHistory;
	protected Properties itemProperties;
	protected File holdingPlace;
	protected View view;
	protected Folder parent;

	protected Item() {
	}
	
	public String getComment() {
		return itemProperties.getProperty(propertyKeys.COMMENT);
	}
	
	public void setComment(String comment) {
		itemProperties.setProperty(propertyKeys.COMMENT, comment);
	}
	
	public int getRevisionNumber() {
		if(itemProperties.containsKey(propertyKeys.REVISION_NUMBER)) {
			try {
				return Integer.parseInt(itemProperties.getProperty(propertyKeys.REVISION_NUMBER));
			} catch (NumberFormatException ne) {
				throw new InvalidOperationException("The REVISION_NUMBER Property is not a number: " + 
						itemProperties.getProperty(propertyKeys.REVISION_NUMBER));
			}
		}
		return 0;
	}
	
	protected void setRevisionNumber(int rev) {
		if(itemProperties.containsKey(propertyKeys._VIEW_ID)) {
			int viewId = Integer.parseInt(itemProperties.getProperty(propertyKeys._VIEW_ID));
			if(viewId != view.getID()) {
				throw new InvalidOperationException("View branching is not supported yet");
			}
		}
		itemProperties.setProperty(propertyKeys.REVISION_NUMBER, Integer.toString(rev));
		itemProperties.setProperty(propertyKeys.FILE_CONTENT_REVISION, Integer.toString(rev));
	}
	
	protected int findRightRevision(int id) {
		if(getView().getConfiguration().isTip()) {
			isFromHistory = false;
			return findLastRevision(id);
		} else if (getView().getConfiguration().isTimeBased()) {
			isFromHistory = true;
			return findTimeRevision(id);
		} else if (getView().getConfiguration().isLabelBased()) {
			isFromHistory = true;
			Label aLabel = new Label(getView().getID(), getView().getConfiguration().getLabelID());
			return aLabel.getRevisionOfItem(id);
		}
		throw new InvalidOperationException("Cannot find a revision with this view configuration");
	}
	
	protected int findLastRevision(int id) {
		int max = 0;
		java.io.File storage = InternalPropertiesProvider.getInstance().getStorageLocation();
		try {
			java.io.File tempLocation = new java.io.File(storage.getCanonicalPath() + java.io.File.separator + id);
			if(tempLocation.exists()) {
				for(String aRevision : tempLocation.list()) {
					try {
						int tocheck = Integer.parseInt(aRevision.trim());
						if(tocheck > max) {
							max = tocheck;
						}
					} catch (NumberFormatException ne) {
						ne.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return max;
	}
	
	protected int findTimeRevision(int id) {
		int revision = 0;
		java.io.File storage = InternalPropertiesProvider.getInstance().getStorageLocation();
		OLEDate time = getView().getConfiguration().getTime();
		if(null == time) {
			throw new Error("Cannot find a time revision without a time parameter");
		}
		long javaTime = time.getLongValue();
		long bestTime = Long.MIN_VALUE;
		try {
			java.io.File location = new java.io.File(storage.getCanonicalPath() + java.io.File.separator + id);
			if(location.exists()) {
				for(String name : location.list()) {
					// check if it is a file
					java.io.File aFile = new java.io.File(location.getCanonicalPath() + java.io.File.separator + name + java.io.File.separator + FILE_PROPERTIES);
					if(!aFile.exists()) {
						// if not then check if it is a folder
						aFile = new java.io.File(location.getCanonicalPath() + java.io.File.separator + name + java.io.File.separator + FOLDER_PROPERTIES);
					}
					if(aFile.exists()) {
						Properties prop = new Properties();
						FileInputStream in = new FileInputStream(aFile);
						try {
							prop.load(in);
							long modificationTime = Long.parseLong(prop.getProperty(propertyKeys.MODIFIED_TIME, "0"));
							if(modificationTime <= javaTime && modificationTime > bestTime) {
								bestTime = modificationTime;
								revision = Integer.parseInt(name);
							}
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							FileUtility.close(in);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return revision;
	}
	
	protected void setView(View view) {
		this.view = view;
	}
	
	public int getModifiedBy() {
		try {
			return Integer.parseInt(itemProperties.getProperty(propertyKeys.MODIFIED_USER_ID));
		} catch (NumberFormatException ne) {
			throw new InvalidOperationException("The MODIFIED_USER_ID Property is not a number: " + 
					itemProperties.getProperty(propertyKeys.MODIFIED_USER_ID));
		}
	}
	
	protected void setModifiedBy() {
		int myUserID = InternalPropertiesProvider.getInstance().getCurrentServer().getMyUserAccount().getID();
		itemProperties.setProperty(propertyKeys.MODIFIED_USER_ID, Integer.toString(myUserID));
	}
	
	protected void setCreatedTime() {
		itemProperties.setProperty(propertyKeys.CREATED_TIME, Long.toString(InternalPropertiesProvider.getInstance().getCurrentServer().getCurrentTime().getLongValue()));
	}
	
	public OLEDate getCreatedTime() {
		try {
			return new OLEDate(Long.parseLong(itemProperties.getProperty(propertyKeys.CREATED_TIME)));
		} catch (NumberFormatException nfe) {
			throw new InvalidOperationException("The item as a invalid java time: " + itemProperties.getProperty(propertyKeys.CREATED_TIME));
		}
	}
	
	protected void setModifiedTime() {
		itemProperties.setProperty(propertyKeys.MODIFIED_TIME, Long.toString(InternalPropertiesProvider.getInstance().getCurrentServer().getCurrentTime().getLongValue()));
	}
	
	public OLEDate getModifiedTime() {
		try {
			return new OLEDate(Long.parseLong(itemProperties.getProperty(propertyKeys.MODIFIED_TIME)));
		} catch (NumberFormatException nfe) {
			throw new InvalidOperationException("The item as a invalid java time: " + itemProperties.getProperty(propertyKeys.MODIFIED_TIME));
		}
	}
	
	@Override
	public int getID() {
		return hashCode();
	}
	
	public int getItemID() {
		return getObjectID();
	}
	
	public int getObjectID() {
		return Integer.parseInt(itemProperties.getProperty(propertyKeys.OBJECT_ID));
	}

	public int getParentObjectID() {
		if(itemProperties.containsKey(propertyKeys.PARENT_OBJECT_ID)) {
			return Integer.parseInt(itemProperties.getProperty(propertyKeys.PARENT_OBJECT_ID));
		}
		return 0;
	}
	
	public boolean isNew() {
		return isNew;
	}
	
	public boolean isFromHistory() {
		return isFromHistory;
	}
	
	public Folder getParentFolder() {
		return parent;
	}
	
	public void update() {
		// Save in the File Database.
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public void refresh() {
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public View getView() {
		return view;
	}
	
	protected List<Item> loadHistory() {
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public Item[] getHistory() {
		List<Item> history = loadHistory();
		Item[] ret = new Item[history.size()];
		history.toArray(ret);
		return ret;
	}
	
	public String getParentFolderHierarchy() {
		if(null == parent.getParentFolder()) {
			if(null == view.getParentView()) {
				return view.getProject().getName() + File.separator;
			}
			return view.getName() + File.separator;
		} else {
			return parent.getParentFolderHierarchy() + parent + File.separator;
		}
	}
	
	public String getParentFolderQualifiedName() {
		if(0 == getParentObjectID()) {
			return view.getName();
		}
		return itemProperties.getProperty(propertyKeys.FOLDER_PATH);
	}
	
	@Override
	public TypeNames getTypeNames() {
		return super.getTypeNames();
	}
	
	@Override
	public Type getType() {
		return new Type(getTypeNames().ITEM, view.getServer());
	}
	
	public Item shareTo(Folder folder) {
		if(!(folder instanceof org.ossnoize.fakestarteam.TrashFolder))
			incrementRefCount();
		return this;
	}

	protected void incrementRefCount() {
		int refCount;
		if(itemProperties.containsKey(propertyKeys._REF_COUNT))
			refCount = Integer.parseInt(itemProperties.getProperty(propertyKeys._REF_COUNT));
		else
			refCount = 0;
		refCount += 1;
		itemProperties.setProperty(propertyKeys._REF_COUNT, Integer.toString(refCount));
	}

	protected void decrementRefCount() {
		int refCount;
		if(itemProperties.containsKey(propertyKeys._REF_COUNT))
			refCount = Integer.parseInt(itemProperties.getProperty(propertyKeys._REF_COUNT));
		else
			refCount = 1;
		refCount -= 1;
		itemProperties.setProperty(propertyKeys._REF_COUNT, Integer.toString(refCount));
	}
	
	public boolean isDeleted() {
		return itemProperties.containsKey(propertyKeys._REF_COUNT) && 
				itemProperties.getProperty(propertyKeys._REF_COUNT).equals("0") && (view instanceof RecycleBin);
	}

	public void moveTo(Folder folder) {
		decrementRefCount();
		shareTo(folder);
	}
	
	public void remove() {
		decrementRefCount();
		if(itemProperties.getProperty(propertyKeys._REF_COUNT).equals("0")) {
			itemProperties.setProperty(propertyKeys.DELETED_TIME, Long.toString(System.currentTimeMillis()));
			itemProperties.setProperty(propertyKeys.DELETED_USER_ID, 
					Integer.toString(InternalPropertiesProvider.getInstance().getCurrentServer().getMyUserAccount().getID()));
			itemProperties.setProperty(PropertyNames.ITEM_DELETED_TIME, itemProperties.getProperty(propertyKeys.DELETED_TIME));
			itemProperties.setProperty(PropertyNames.ITEM_DELETED_USER_ID, itemProperties.getProperty(propertyKeys.DELETED_USER_ID));
		}
		update();
	}
	
	public OLEDate getDeletedTime() {
		if(itemProperties.containsKey(propertyKeys.DELETED_TIME)) {
			long date = Long.parseLong(itemProperties.getProperty(propertyKeys.DELETED_TIME));
			return new OLEDate(date);
		}
		return new OLEDate(0);
	}
	
	public int getDeletedUserID() {
		if(itemProperties.containsKey(propertyKeys.DELETED_USER_ID)) {
			int userId = Integer.parseInt(itemProperties.getProperty(propertyKeys.DELETED_USER_ID));
			return userId;
		}
		return -1;
	}
	
	public void discard() {
		loadProperties();
		SimpleTypedResourceIDProvider.getProvider().clearExisting(getView(), getObjectID());
	}
	
	protected void loadProperties() {
	}
}
