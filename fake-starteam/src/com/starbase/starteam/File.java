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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.ossnoize.fakestarteam.FakeFolder;
import org.ossnoize.fakestarteam.FileUtility;
import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.SimpleTypedResourceIDProvider;
import org.ossnoize.fakestarteam.exception.InvalidOperationException;

import com.starbase.util.MD5;
import com.starbase.util.OLEDate;

public class File extends Item {

	private static final String FILE_PROPERTIES = "file.properties";
	private static final String FILE_STORED = "stored.gz";

	public File(Folder parent) {
		super();
		this.parent = parent;
		this.view = parent.getView();
		isNew = true;
	}

	protected File(int id, View view) {
		super();
		this.view = view;
		try {
			holdingPlace = createHoldingPlace(id, findLastRevision(id));
		} catch (IOException e) {
			throw new InvalidOperationException("Cannot initialize the " + id + " in " + parent);
		}
		loadFileProperties();
	}

	protected File(int id, int revision, View view) {
		super();
		this.view = view;
		try {
			holdingPlace = createHoldingPlace(id, revision);
		} catch (IOException e) {
			throw new InvalidOperationException("Cannot initialize the " + id + " in " + parent + ": revision " + revision);
		}
		loadFileProperties();
	}

	public void add(java.io.File file, String name, String desc, String reason, int lockStatus, boolean updateStatus) throws java.io.IOException {
		if(isNew()) {
			registerNewID();
			holdingPlace = createHoldingPlace(0);
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
			shareTo(parent);
		} else {
			throw new InvalidOperationException("Cannot add a file that is already existing");
		}
	}

	private java.io.File createHoldingPlace(int revision) throws IOException {
		return createHoldingPlace(getObjectID(), revision);
	}

	private java.io.File createHoldingPlace(int id, int revision) throws IOException {
		java.io.File storage = InternalPropertiesProvider.getInstance().getStorageLocation();
		String folder = storage.getCanonicalPath() + java.io.File.separator + id + java.io.File.separator + revision;
		return new java.io.File(folder);
	}

	public void checkinFrom(java.io.File file, String reason, int lockStatus, boolean forceCheckin, boolean updateStatus) throws java.io.IOException {
		if(!isNew()) {
			int newRevision = getRevisionNumber() + 1;
			loadFileProperties();
			holdingPlace = createHoldingPlace(newRevision);
			if(holdingPlace.exists()) {
				if(forceCheckin) {
					newRevision = findLastRevision(getObjectID()) + 1;
					holdingPlace = createHoldingPlace(newRevision);
				} else {
					throw new InvalidOperationException("Cannot check-in a past version of a file, do a force check-in to force the update");
				}
			}
			setRevisionNumber(newRevision);
			setComment(reason);
			setModifiedBy();
			setModifiedTime();
			copyToGz(file);
			update();
		} else {
			throw new InvalidOperationException("Cannot check-in a file that was not added");
		}
	}
	
	public boolean checkoutByVersion(java.io.File checkoutTo, int viewVersion, int lockStatus, boolean timeStampNow, boolean eol, boolean updateStatus) throws java.io.IOException {
		holdingPlace = createHoldingPlace(viewVersion);
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
	
	public void checkoutTo(java.io.File checkoutTo, int lockStatus, boolean timeStampNow, boolean eol, boolean updateStatus) throws java.io.IOException {
		if(holdingPlace.exists()) {
			copyFromGz(holdingPlace, checkoutTo);
			if(!timeStampNow) {
				checkoutTo.setLastModified(getModifiedTime().getLongValue());
			}
		} else {
			throw new InvalidOperationException("The file does not exist in the repository");
		}
	}

	private void copyToGz(java.io.File file) throws IOException {
		MessageDigest digest;
		int encoding = propertyEnums.FILE_ENCODING_ASCII;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Could not find digest type MD5: " + e.getMessage());
		}
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
		long size = 0;
		while(read >= 0) {
			size += read;
			gzout.write(buffer, 0, read);
			digest.update(buffer, 0, read);
			// Analyze the encoding of the file.
			if(encoding == propertyEnums.FILE_ENCODING_ASCII ||
			   encoding == propertyEnums.FILE_ENCODING_UNICODE) 
			{
				for(int it = 0; it < read; ++it) {
					if(0 == buffer[it]) {
						encoding = propertyEnums.FILE_ENCODING_BINARY;
						break;
					} else if (0 > buffer[it]) {
						encoding = propertyEnums.FILE_ENCODING_UNICODE;
					}
				}
			}
			read = fin.read(buffer);
		}
		byte[] md5Array = digest.digest();
		MD5 fileChecksum = new MD5(md5Array);
		
		itemProperties.setProperty(propertyKeys.FILE_MD5_CHECKSUM, fileChecksum.toHexString());
		itemProperties.setProperty(propertyKeys.FILE_SIZE, Long.toString(size));
		itemProperties.setProperty(propertyKeys.FILE_ENCODING, Integer.toString(encoding));
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
	
	public long getSizeEx() {
		if(null != itemProperties) {
			try {
				return Long.parseLong(itemProperties.getProperty(propertyKeys.FILE_SIZE));
			} catch (NumberFormatException e) {
				return -1L;
			}
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
		if(null == itemProperties) {
			itemProperties = new Properties();
		}
		FileInputStream fin = null;
		try {
			java.io.File fileProperty = new java.io.File(holdingPlace.getCanonicalPath() + java.io.File.separator + FILE_PROPERTIES);
			if(fileProperty.exists()) {
				fin = new FileInputStream(fileProperty);
				itemProperties.load(fin);
				int id = Integer.parseInt(itemProperties.getProperty(propertyKeys.OBJECT_ID));
				SimpleTypedResourceIDProvider.getProvider().registerExisting(id, this);
				
				SimpleTypedResource ressource = SimpleTypedResourceIDProvider.getProvider().findExisting(getParentObjectID());
				if(ressource instanceof Folder) {
					parent = (Folder)ressource;
				} else {
					parent = new FakeFolder(this.view, id, null);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtility.close(fin);
		}
	}

	private void registerNewID() {
		itemProperties = new Properties();
		itemProperties.setProperty(propertyKeys.OBJECT_ID,
				Integer.toString(SimpleTypedResourceIDProvider.getProvider().registerNew(this)));
		itemProperties.setProperty(propertyKeys.PARENT_OBJECT_ID,
				Integer.toString(parent.getObjectID()));
	}

	private int findLastRevision(int id) {
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
	
	@Override
	protected List<Item> loadHistory() {
		int lastRevision = findLastRevision(getObjectID());
		List<Item> ret = new ArrayList<Item>(lastRevision);
		for(int i=0; i<=lastRevision; i++) {
			ret.add(new File(getObjectID(), i, this.view));
		}
		return ret;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	public int getStatus() throws IOException {
		return getStatus(getWorkingFile());
	}
	
	public int getStatus(java.io.File file) throws IOException {
		OLEDate time = getModifiedTime();
		long size = getSizeEx();
		if(!file.isFile()) {
			return Status.MISSING;
		}
		if(file.lastModified() > time.getLongValue()) {
			return Status.MODIFIED;
		}
		if(file.lastModified() == time.getLongValue()) {
			if(file.length() == size) {
				return Status.CURRENT;
			}
		}
		if(file.lastModified() < time.getLongValue()) {
			if(file.length() != size) {
				return Status.OUTOFDATE;
			}
		}
		return Status.UNKNOWN;
	}
	
	public int getStatusNow() throws IOException {
		return getStatus();
	}

	private java.io.File getWorkingFile() {
		return new java.io.File(System.getProperty("user.dir") + java.io.File.separator + getParentFolderHierarchy() + java.io.File.separator + getName());
	}

	public int getStatusByMD5(MD5 md5) {
		String md5sum = itemProperties.getProperty(propertyKeys.FILE_MD5_CHECKSUM);
		if(null != md5sum) {
			if(md5sum.equalsIgnoreCase(md5.toHexString())) {
				return Status.CURRENT;
			}
		}
		return Status.UNKNOWN;
	}

	public byte[] getMD5() {
		if(null != itemProperties) {
			String md5sum = itemProperties.getProperty(propertyKeys.FILE_MD5_CHECKSUM);
			MD5 fileChecksum = new MD5(md5sum);
			return fileChecksum.getData();
		}
		throw new InvalidOperationException("Item Properties was never initialized");
	}
	
	public int getCharset() {
		if(null != itemProperties) {
			if(itemProperties.containsKey(propertyKeys.FILE_ENCODING)) {
				try {
					return Integer.parseInt(itemProperties.getProperty(propertyKeys.FILE_ENCODING));
				} catch (NumberFormatException ex) {
					throw new InvalidOperationException("The file encoding value is invalid: " + ex.getMessage());
				}
			} else {
				// Assume file that have unidentified encoding as binary.
				return propertyEnums.FILE_ENCODING_BINARY;
			}
		} else {
			throw new InvalidOperationException("Item Properties was never initialized");
		}
	}

	@Override
	public Item shareTo(Folder folder) {
		StringBuffer childIdList = null;
		if(folder.itemProperties.containsKey(propertyKeys._FILES)) {
			childIdList = new StringBuffer(folder.itemProperties.getProperty(propertyKeys._FILES)).append(";");
		} else {
			childIdList = new StringBuffer(25);
		}
		childIdList.append(getObjectID());
		folder.itemProperties.setProperty(propertyKeys._FILES, childIdList.toString());
		folder.update();
		return this;
	}
	
	@Override
	public void moveTo(Folder folder) {
		Folder origin = getParentFolder();
		String thisStringId = Integer.toString(getObjectID());
		if(origin.itemProperties.containsKey(propertyKeys._FILES)) {
			StringBuffer idList = new StringBuffer(origin.itemProperties.getProperty(propertyKeys._FILES));
			int start = idList.indexOf(thisStringId);
			idList.delete(start, start+thisStringId.length());
			origin.itemProperties.setProperty(propertyKeys._FILES, idList.toString());
			origin.update();
		}
		itemProperties.setProperty(propertyKeys.PARENT_OBJECT_ID, Integer.toString(folder.getObjectID()));
		shareTo(folder);
	}
}
