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

import org.ossnoize.fakestarteam.InternalPropertiesProvider;

import com.starbase.util.OLEDate;

public class View extends SimpleTypedResource implements ISecurableContainer, ISecurableObject {
	
	private ViewConfiguration configuration;
	private View otherView;
	
	protected View() {
		this.configuration = ViewConfiguration.createTip();
		this.otherView = null;
	}
	
	public View(View parent, java.lang.String name, java.lang.String description, java.lang.String defaultWorkingFolder) {
		this.configuration = ViewConfiguration.createTip();
		this.otherView = null;
	}
	
	public View(View view, ViewConfiguration configuration) {
		this.configuration = configuration;
		this.otherView = view;
	}

	@Override
	public int getID() {
		if(null != otherView)
			return otherView.getID();
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public java.lang.String getName() {
		if(null != otherView)
			return otherView.getName();
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public void update() {
		if(null != otherView)
			otherView.update();
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public Project getProject() {
		if(null != otherView)
			return otherView.getProject();
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public View getParentView() {
		if(null != otherView)
			return otherView.getParentView();
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public String getDescription() {
		if(null != otherView)
			return otherView.getDescription();
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public void setDescription(String description) {
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public String getDefaultPath() {
		if(null != otherView)
			return otherView.getDefaultPath();
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public void setDefaultPath(String path) {
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public void refresh() {
		if(null != otherView)
			otherView.refresh();
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public void populate() {
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public OLEDate getCreatedTime() {
		if(null != otherView)
			return otherView.getCreatedTime();
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public int getCreatedBy() {
		if(null != otherView)
			return otherView.getCreatedBy();
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public Folder getRootFolder() {
		if(null != otherView) {
			return otherView.createRootFolder(this);
		}
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	protected Folder createRootFolder(View v) {
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public RecycleBin getRecycleBin() {
		if(null != otherView)
			return otherView.getRecycleBin();
		throw new UnsupportedOperationException("Not implemented at this level");
	}
	
	public ViewConfiguration getConfiguration() {
		return configuration;
	}
	
	public void discard() {
	}
	
	public Server getServer() {
		return InternalPropertiesProvider.getInstance().getCurrentServer();
	}
}
