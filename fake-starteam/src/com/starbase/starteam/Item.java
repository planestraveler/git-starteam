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
import java.util.List;
import java.util.Properties;

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
		try {
			return Integer.parseInt(itemProperties.getProperty(propertyKeys.REVISION_NUMBER));
		} catch (NumberFormatException ne) {
			throw new InvalidOperationException("The REVISION_NUMBER Property is not a number: " + 
					itemProperties.getProperty(propertyKeys.REVISION_NUMBER));
		}
	}
	
	protected void setRevisionNumber(int rev) {
		itemProperties.setProperty(propertyKeys.REVISION_NUMBER, Integer.toString(rev));
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
		if(null != view) {
			int myUserID = view.getProject().getServer().getMyUserAccount().getID();
			itemProperties.setProperty(propertyKeys.MODIFIED_USER_ID, Integer.toString(myUserID));
		} else {
			throw new InvalidOperationException("The item is not part of a view");
		}
	}
	
	protected void setCreatedTime() {
		itemProperties.setProperty(propertyKeys.CREATED_TIME, Long.toString(System.currentTimeMillis()));
	}
	
	public OLEDate getCreatedTime() {
		try {
			return new OLEDate(Long.parseLong(itemProperties.getProperty(propertyKeys.CREATED_TIME)));
		} catch (NumberFormatException nfe) {
			throw new InvalidOperationException("The item as a invalid java time: " + itemProperties.getProperty(propertyKeys.CREATED_TIME));
		}
	}
	
	protected void setModifiedTime() {
		itemProperties.setProperty(propertyKeys.MODIFIED_TIME, Long.toString(System.currentTimeMillis()));
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
	
	public int getObjectID() {
		return Integer.parseInt(itemProperties.getProperty(propertyKeys.OBJECT_ID));
	}
	
	public boolean isNew() {
		return isNew;
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
			return File.separator + view.getName() + File.separator;
		} else {
			return parent.getParentFolderHierarchy() + parent + File.separator;
		}
	}
	
	@Override
	public TypeNames getTypeNames() {
		return super.getTypeNames();
	}
}
