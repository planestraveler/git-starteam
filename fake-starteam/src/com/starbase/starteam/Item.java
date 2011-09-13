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

import java.util.Properties;

public class Item extends SimpleTypedResource implements ISecurableObject {

	private int id;
	private boolean isNew;
	private Properties itemProperties;
	
	protected Item() {
	}
	
	public String getComment() {
		//return itemProperties.getProperty(PropertyNames.COMMENT);
		return "";
	}
	
	public void setComment(String comment) {
		//itemProperties.setProperty(PropertyNames.COMMENT, comment);
	}
	
	@Override
	public int getID() {
		return id;
	}
	
	public Item[] getHistory() {
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public boolean isNew() {
		return isNew;
	}
	
	public void update() {
		// Save in the File Database.
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public void refresh() {
		throw new UnsupportedOperationException("Not implemented at this level");
	}
}
