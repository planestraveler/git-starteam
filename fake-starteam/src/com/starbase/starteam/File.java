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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.ossnoize.fakestarteam.FileUtility;
import org.ossnoize.fakestarteam.SimpleTypedResourceIDProvider;
import org.ossnoize.fakestarteam.exception.InvalidOperationException;

public class File extends Item {

	private static final String FILE_PROPERTIES = "file.properties";
	private static final String FILE_STORED = "stored.gz";

	public File(Folder parent) {
		super();
		this.parent = parent;
		this.view = parent.getView();
		isNew = true;
	}

	protected File(String name, Folder parent) {
		super();
		this.parent = parent;
		this.view = parent.getView();
		try {
			holdingPlace = new java.io.File(parent.holdingPlace.getCanonicalPath() + java.io.File.separator + name + java.io.File.separator + findLastRevision(name));
		} catch (IOException e) {
			throw new InvalidOperationException("Cannot initialize the " + name + " in " + parent);
		}
		loadFileProperties();
	}

	protected File(String name, int revision, Folder parent) {
		super();
		this.parent = parent;
		this.view = parent.getView();
		try {
			holdingPlace = new java.io.File(parent.holdingPlace.getCanonicalPath() + java.io.File.separator + name + java.io.File.separator + revision);
		} catch (IOException e) {
			throw new InvalidOperationException("Cannot initialize the " + name + " in " + parent + ": revision " + revision);
		}
		loadFileProperties();
	}

	public void add(java.io.File file, String name, String desc, String reason, int lockStatus, boolean updateStatus) throws java.io.IOException {
		if(isNew()) {
			holdingPlace = new java.io.File(parent.holdingPlace.getCanonicalPath() + java.io.File.separator + name + java.io.File.separator + "0");
			loadFileProperties();
			setRevisionNumber(0);
			setComment(reason);
			setDescription(desc);
			setName(name);
			setModifiedBy();
			setCreatedTime();
			setModifiedTime();
			copyToGz(file);
			isNew = false;
			update();
		} else {
			throw new InvalidOperationException("Cannot add a file that is already existing");
		}
	}

	public void checkinFrom(java.io.File file, String reason, int lockStatus, boolean forceCheckin, boolean updateStatus) throws java.io.IOException {
		if(!isNew()) {
			int newRevision = getRevisionNumber() + 1;
			loadFileProperties();
			holdingPlace = new java.io.File(parent.holdingPlace.getCanonicalPath() + java.io.File.separator + getName() + java.io.File.separator + newRevision);
			if(holdingPlace.exists()) {
				if(forceCheckin) {
					newRevision = findLastRevision(getName()) + 1;
					holdingPlace = new java.io.File(parent.holdingPlace.getCanonicalPath() + java.io.File.separator + getName() + java.io.File.separator + newRevision);
				} else {
					throw new InvalidOperationException("Cannot check-in a past version of a file, do a force check-in to force the update");
				}
			}
			setRevisionNumber(newRevision);
			setComment(reason);
			setModifiedBy();
			setModifiedTime();
			registerNewID();
			copyToGz(file);
			update();
		} else {
			throw new InvalidOperationException("Cannot check-in a file that was not added");
		}
	}
	
	public boolean checkoutByVersion(java.io.File checkoutTo, int viewVersion, int lockStatus, boolean timeStampNow, boolean eol, boolean updateStatus) throws java.io.IOException {
		holdingPlace = new java.io.File(parent.holdingPlace.getCanonicalPath() + java.io.File.separator + getName() + java.io.File.separator + viewVersion);
		if(holdingPlace.exists()) {
			loadFileProperties();
			if(null == checkoutTo) {
				//TODO: build the default checkout directory location
				throw new InvalidOperationException("Does not yet support null checkoutTo parameter");
			}
			copyFromGz(holdingPlace, checkoutTo);
			if(!timeStampNow) {
				checkoutTo.setLastModified(getModifiedTime().getLongValue());
			}
			return true;
		} else {
			return false;
		}
	}

	private void copyToGz(java.io.File file) throws IOException {
		GZIPOutputStream gzout = null;
		FileOutputStream fout = null;
		FileInputStream fin = null;
		if(!holdingPlace.exists())
			holdingPlace.mkdirs();
		fout = new FileOutputStream(holdingPlace.getCanonicalPath() + java.io.File.separator + FILE_STORED);
		gzout = new GZIPOutputStream(fout);
		fin = new FileInputStream(file);
		
		byte[] buffer = new byte[1024*64];
		int read = fin.read(buffer);
		while(read >= 0) {
			gzout.write(buffer, 0, read);
			read = fin.read(buffer);
		}
		FileUtility.close(fin, gzout, fout);
	}

	private void copyFromGz(java.io.File source, java.io.File target) throws IOException {
		GZIPInputStream gzin = null;
		FileInputStream fin = null;
		FileOutputStream fout = null;
		
		if(!source.exists()) {
			throw new InvalidOperationException("Could not find the storing folder");
		}
		fin = new FileInputStream(source.getCanonicalPath() + java.io.File.separator + FILE_STORED);
		gzin = new GZIPInputStream(fin);
		fout = new FileOutputStream(target);
		
		byte[] buffer = new byte[1024 * 64];
		int read = gzin.read(buffer);
		while(read >= 0) {
			fout.write(buffer, 0, read);
			read = gzin.read(buffer);
		}
		FileUtility.close(fout, gzin, fin);
	}
	
	public void setDescription(String description) {
		if(null != itemProperties) {
			itemProperties.setProperty(propertyKeys.FILE_DESCRIPTION, description);
		} else {
			throw new InvalidOperationException("Item properties are not initialized");
		}
	}

	public String getDescription() {
		if(null != itemProperties) {
			return itemProperties.getProperty(propertyKeys.FILE_DESCRIPTION);
		} else {
			throw new InvalidOperationException("Item properties are not initialized");
		}
	}

	public void setName(String name) {
		if(null != itemProperties) {
			itemProperties.setProperty(propertyKeys.FILE_NAME, name);
		} else {
			throw new InvalidOperationException("Item properties are not initialized");
		}
	}

	public String getName() {
		if(null != itemProperties) {
			return itemProperties.getProperty(propertyKeys.FILE_NAME);
		} else {
			throw new InvalidOperationException("Item properties are not initialized");
		}
	}

	@Override
	public void update() {
		if(itemProperties == null) {
			throw new InvalidOperationException("Properties are not initialized yet!!!");
		}
		FileOutputStream fout = null;
		try {
			if(!holdingPlace.exists())
				holdingPlace.mkdirs();
			fout = new FileOutputStream(holdingPlace.getCanonicalPath() + java.io.File.separator + FILE_PROPERTIES);
			itemProperties.store(fout, "File properties");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtility.close(fout);
		}
	}

	private void loadFileProperties() {
		itemProperties = new Properties();
		FileInputStream fin = null;
		try {
			java.io.File fileProperty = new java.io.File(holdingPlace.getCanonicalPath() + java.io.File.separator + FILE_PROPERTIES);
			if(fileProperty.exists()) {
				fin = new FileInputStream(fileProperty);
				itemProperties.load(fin);
				int id = Integer.parseInt(itemProperties.getProperty(propertyKeys.OBJECT_ID));
				SimpleTypedResourceIDProvider.getProvider().registerExisting(id, this);
			} else {
				registerNewID();
				isNew = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtility.close(fin);
		}
	}

	private void registerNewID() {
		if(null != itemProperties) {
			itemProperties.setProperty(propertyKeys.OBJECT_ID, 
					Integer.toString(SimpleTypedResourceIDProvider.getProvider().registerNew(this)));
		} else {
			throw new InvalidOperationException("itemProperties is not yet initialized");
		}
	}

	private int findLastRevision(String name) {
		int max = 0;
		try {
			java.io.File nameDir = new java.io.File(parent.holdingPlace.getCanonicalPath() + java.io.File.separator + name);
			if(nameDir.exists()) {
				for(String aRevision : nameDir.list()) {
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
	
	@Override
	protected List<Item> loadHistory() {
		int lastRevision = findLastRevision(getName());
		List<Item> ret = new ArrayList<Item>(lastRevision);
		for(int i=0; i<lastRevision; i++) {
			ret.add(new File(getName(), i, parent));
		}
		return ret;
	}

	@Override
	public String toString() {
		return getName();
	}
}
