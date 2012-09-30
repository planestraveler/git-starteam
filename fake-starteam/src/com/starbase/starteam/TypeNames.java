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

import java.util.HashSet;
import java.util.Set;

public class TypeNames {
	protected TypeNames() {}
	protected static Set<String> typeNamesList;
	static {
		TypeNames enumeration = new TypeNames();
		typeNamesList = new HashSet<String>();
		typeNamesList.add(enumeration.AUDIT);
		typeNamesList.add(enumeration.CHANGEREQUEST);
		typeNamesList.add(enumeration.FILE);
		typeNamesList.add(enumeration.FOLDER);
		typeNamesList.add(enumeration.ITEM);
		typeNamesList.add(enumeration.ITEM_REFERENCE);
		typeNamesList.add(enumeration.ITEM_REVISION);
		typeNamesList.add(enumeration.LABEL);
		typeNamesList.add(enumeration.LINK);
		typeNamesList.add(enumeration.PROJECT);
		typeNamesList.add(enumeration.PROMOTIONMODEL);
		typeNamesList.add(enumeration.PROMOTIONSTATE);
		typeNamesList.add(enumeration.REQUIREMENT);
		typeNamesList.add(enumeration.TASK);
		typeNamesList.add(enumeration.TASKDEPENDENCY);
		typeNamesList.add(enumeration.TOPIC);
		typeNamesList.add(enumeration.TYPE);
		typeNamesList.add(enumeration.USER);
		typeNamesList.add(enumeration.USERACCOUNT);
		typeNamesList.add(enumeration.VIEW);
		typeNamesList.add(enumeration.WORKRECORD);
	}
	public final String	AUDIT = "Audit";
	public final String CHANGEREQUEST = "ChangeRequest";
	public final String FILE = "File";
	public final String FOLDER = "Folder";
	public final String ITEM = "Item";
	public final String ITEM_REFERENCE = "ItemReference";
	public final String ITEM_REVISION = "ItemRevision";
	public final String LABEL = "Label";
	public final String LINK = "Link";
	public final String PROJECT = "Project";
	public final String PROMOTIONMODEL = "PromotionModel";
	public final String PROMOTIONSTATE = "PromotionState";
	public final String REQUIREMENT = "Requirement";
	public final String TASK = "Task";
	public final String TASKDEPENDENCY = "TaskDependency";
	public final String TOPIC = "Topic";
	public final String TYPE = "Type";
	public final String USER = "User";
	public final String USERACCOUNT = "User";
	public final String VIEW = "View";
	public final String WORKRECORD = "WorkRecord";
}
