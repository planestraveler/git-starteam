/*****************************************************************************
    This file is part of Git-Starteam.

    Git-Starteam is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Git-Starteam is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Git-Starteam.  If not, see <http://www.gnu.org/licenses/>.
******************************************************************************/
package org.sync.changerequests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sync.Log;

import com.starbase.starteam.ChangeRequest;
import com.starbase.starteam.FolderListManager;
import com.starbase.starteam.ItemListManager;
import com.starbase.starteam.Items;
import com.starbase.starteam.Label;
import com.starbase.starteam.QueryInfo;
import com.starbase.starteam.QueryNode;
import com.starbase.starteam.QueryPart;
import com.starbase.starteam.Server;
import com.starbase.starteam.Type;
import com.starbase.starteam.View;

public class ChangeRequestsHelper implements IChangeRequestsHelper{

	private String filePattern = null; 
	private Pattern crDescriptionPattern = Pattern.compile(".*\\[CR(\\d+)\\].*");
	private Pattern commentFilterPattern = Pattern.compile("Automatic Update from CRForm \\(version \\d+\\.\\d+\\.\\d+\\.\\d+\\)");
	private View currentView = null;
	private Type crType = null;
	
	private FolderListManager flm = null;
	private ItemListManager ilm = null;

	public ChangeRequestsHelper(View view) {
		currentView = view;
		long start = System.nanoTime();
		flm = new FolderListManager(currentView);
		flm.includeFolders(currentView.getRootFolder(), -1);

		Server server = currentView.getServer();

		crType = server.typeForName(server.getTypeNames().CHANGEREQUEST);

		ilm = new ItemListManager(crType, flm);
		ilm.getItems(); // Fetch all CR from the current view so we can poke them
		                // later.
		long duration = System.nanoTime() - start;
		Log.logf("Creating cache of CR took %dns for view %s", duration, currentView.getName());
	}
	
	@Override
	public boolean isChangeRequestsFeatureEnable() {
		return filePattern != null;
	}

	@Override
	public boolean labelHasCRInfoAttached(Label label) {
		String description = label.getDescription();
		if(description == null || description.isEmpty()){
			return false;
		}
		
		Matcher matcher = crDescriptionPattern.matcher(description);
		return matcher.matches();
	}

	@Override
	public ChangeRequestInformation getChangeRequestsInformation(Label label) {
		long start = System.nanoTime();
		ChangeRequest changeRequest = getChangeRequest(label);
		long duration = System.nanoTime() - start;
		if (duration > 1000000) {
			Log.logf("Getting change request took %dns for label %s", duration, label.getName());
		}
		ChangeRequestInformation changeRequestInfo = new ChangeRequestInformation(changeRequest, filePattern);
		
		return changeRequestInfo;
	}
	
	@Override
	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}

	@Override
	public boolean commentMatchFilter(String comment) {
		if(comment == null || comment.isEmpty()){
			return false;
		}
		
		Matcher matcher = commentFilterPattern.matcher(comment);
		return matcher.matches();
	}
	
	private ChangeRequest getChangeRequest(Label label) {
		int crNumber = getCRNumber(label);
		Server server = currentView.getServer();

		QueryInfo query = buildQuery(server, crType, crNumber);
		Items items = ilm.selectByQuery(query);
		
		if(items.size() == 0){
			throw new NoSuchChangeRequestException("Change Request #" + crNumber + " not found.");
		}
		
		return (ChangeRequest)items.getAt(0);
	}
	
	public int getCRNumber(Label label){
		String description = label.getDescription();
		if(description != null && !description.isEmpty()){
			Matcher matcher = crDescriptionPattern.matcher(description);
			
			if(matcher.matches()){
				return Integer.parseInt(matcher.group(1));
			}
		}
		return 0;
	}
	
	private QueryInfo buildQuery(Server server, Type crType, int crNumber){
		int propertyID = crType.propertyForName(server.getPropertyNames().CR_CHANGE_NUMBER).getID();
		
		QueryPart part = new QueryPart(propertyID,QueryPart.REL_EQUAL, crNumber);
		QueryNode node = new QueryNode(QueryNode.OP_AND);
		node.appendQueryPart(part);
		
		QueryInfo query = new QueryInfo(crType,false,"My Temporary Query",node);
		return query;
	}
	
	public class NoSuchChangeRequestException extends RuntimeException{
		
		public NoSuchChangeRequestException(){
			super();
		}
		
		public NoSuchChangeRequestException(String message){
			super(message);
		}
	}
}
