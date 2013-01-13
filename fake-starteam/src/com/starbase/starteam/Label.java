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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.ossnoize.fakestarteam.FileUtility;
import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.exception.InvalidOperationException;

import com.starbase.util.OLEDate;

public class Label extends CacheRef {
	public static final int NEXT_BUILD_ID 			= -2;
	public static final int SCOPE_ITEM_AND_CONTENTS = 1;
	public static final int SCOPE_ITEM_ONLY 		= 0;
	public static final int SCOPE_ITEM_TREE 		= 2;
	
	private final String ID = "id";
	private final String ITEM_LIST = "items";
	private final String NAME = "name";
	private final String DESCRIPTION = "description";
	private final String TIME = "time";
	private final String REVISION = "revision time";
	private final String VIEW_ID = "view id";
	private final String PROJECT_ID = "project id";
	private final String BUILD_LABEL = "build label";
	private final String FROZEN = "frozen";
	
	private int id;
	private Map<Integer, Integer> itemList = new HashMap<Integer,Integer>();
	private String name;
	private String description;
	private OLEDate time;
	private OLEDate revisionTime;
	private int viewID;
	private int projectID;
	private boolean isNew;
	private boolean buildLabel;
	private boolean frozen;

	protected Label(View view, String name, String description, OLEDate time, boolean buildLabel, boolean frozen) {
		isNew = true;
		if(null == view)
			throw new NullPointerException("View cannot be null");
		if(null == view.getProject())
			throw new NullPointerException("Project of view cannot be null");
		if(null == name)
			throw new NullPointerException("Name cannot be null");
		if(null == description)
			throw new NullPointerException("Description cannot be null");
		if(null == time)
			throw new NullPointerException("Time cannot be null");
		this.name = name;
		this.description = description;
		this.time = time;
		this.revisionTime = time;
		this.viewID = view.getID();
		this.projectID = view.getProject().getID();
		this.buildLabel = buildLabel;
		this.frozen = frozen;
	}
	
	protected Label(int viewId, int labelId) {
		isNew = false;
		loadInformation(viewId, labelId);
	}
	
	protected static Label[] getLabelList(int viewId) {
		File dir = buildStoragePath(viewId);
		ArrayList<Label> list = new ArrayList<Label>();
		for(String labelid : dir.list()) {
			list.add(new Label(viewId, Integer.parseInt(labelid)));
		}
		Label[] LabelList = new Label[list.size()];
		return list.toArray(LabelList);
	}
	
	protected int getRevisionOfItem(int itemId) {
		if(!itemList.containsKey(itemId))
			throw new InvalidOperationException("This item was not labeled in the view");
		return itemList.get(itemId);
	}
	
	public boolean isNew() {
		return isNew;
	}
	
	public OLEDate getTime() {
		return time;
	}
	
	public OLEDate getRevisionTime() {
		return revisionTime;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}

	public int getID() {
		return id;
	}
	
	public void update() {
		java.io.File storageLocation = buildStoragePath(viewID);
		if(isNew) {
			int max = 0;
			if(storageLocation.exists()) {
				for(String l : storageLocation.list()) {
					try {
						int value = Integer.parseInt(l);
						if(value > max) {
							max = value;
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			} else {
				storageLocation.mkdirs();
			}
			id = max + 1;
		}
		Properties toSave = new Properties();
		toSave.setProperty(ID, Integer.toString(id));
		toSave.setProperty(ITEM_LIST, toListString());
		toSave.setProperty(NAME, name);
		toSave.setProperty(DESCRIPTION, description);
		toSave.setProperty(TIME, Long.toString(time.getLongValue()));
		toSave.setProperty(REVISION, Long.toString(revisionTime.getLongValue()));
		toSave.setProperty(VIEW_ID, Integer.toString(viewID));
		toSave.setProperty(PROJECT_ID, Integer.toString(projectID));
		toSave.setProperty(BUILD_LABEL, Boolean.toString(buildLabel));
		toSave.setProperty(FROZEN, Boolean.toString(frozen));
		FileWriter fout = null;
		try {
			java.io.File labelInformation = new java.io.File(storageLocation.getAbsolutePath() + File.separator + id);
			fout = new FileWriter(labelInformation);
			toSave.store(fout, "Label <" + name + "> in viewId " + viewID + " and projectId " + projectID);
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			FileUtility.close(fout);
		}
	}
	
	public void attachToFolder(Folder folder, int scope) {
		attachToItem(folder);
		if(scope == SCOPE_ITEM_ONLY)
			return;
		for(Item i : folder.getItems(folder.getTypeNames().FILE)) {
			attachToItem(i);
		}
		for(Folder f : folder.getSubFolders()) {
			attachToFolder(f, scope);
		}
	}
	
	public void attachToItem(Item i) {
		itemList.put(i.getItemID(), i.getRevisionNumber());
	}

	private String toListString() {
		StringBuilder builder = new StringBuilder();
		for(Map.Entry<Integer, Integer> pair : itemList.entrySet()) {
			builder.append(pair.getKey()).append('/').append(pair.getValue()).append(';');
		}
		return builder.toString();
	}
	
	private void fromListString(String list) {
		itemList.clear();
		for(String s : list.split(";")) {
			if(s.length() > 0) {
				String[] pair = s.split("/");
				if(pair.length != 2)
					throw new InvalidOperationException("the item revision pair is not properly seperated");
				
				int id = Integer.parseInt(pair[0]);
				int rev = Integer.parseInt(pair[1]);
				itemList.put(id, rev);
			}
		}
	}

	private static java.io.File buildStoragePath(int viewId) {
		return new java.io.File(InternalPropertiesProvider.getInstance().getStorageLocation() + 
				java.io.File.separator + "LabelStorage" + java.io.File.separator + viewId);
	}
	
	private void loadInformation(int viewId, int labelId) {
		java.io.File labelFile = new File(buildStoragePath(viewId).getAbsolutePath() + File.separator + labelId);
		if(!labelFile.exists()) {
			throw new InvalidOperationException("Label id " + labelId + " does not exist in view " + viewId);
		}
		Properties labelProps = new Properties();
		FileReader fread = null;
		try {
			fread = new FileReader(labelFile);
			labelProps.load(fread);
			id = Integer.parseInt(labelProps.getProperty(ID));
			fromListString(labelProps.getProperty(ITEM_LIST));
			name = labelProps.getProperty(NAME);
			description = labelProps.getProperty(DESCRIPTION);
			time = new OLEDate(Long.parseLong(labelProps.getProperty(TIME)));
			revisionTime = new OLEDate(Long.parseLong(labelProps.getProperty(REVISION)));
			viewID = Integer.parseInt(labelProps.getProperty(VIEW_ID));
			projectID = Integer.parseInt(labelProps.getProperty(PROJECT_ID));
			buildLabel = labelProps.getProperty(BUILD_LABEL).equalsIgnoreCase("true");
			frozen = labelProps.getProperty(FROZEN).equalsIgnoreCase("true");
			if(viewID != viewId) {
				throw new InvalidOperationException("The view id (" + viewID +") does not match with the requested view id(" + viewId + ")");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtility.close(fread);
		}
	}
}
