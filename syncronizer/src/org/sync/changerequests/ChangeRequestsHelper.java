package org.sync.changerequests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.starbase.starteam.ChangeRequest;
import com.starbase.starteam.Folder;
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
	
	
	public ChangeRequestsHelper(){
		
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
	public ChangeRequestInformation getChangeRequestsInformation(View view, Folder folder, Label label) {
		ChangeRequest changeRequest = getChangeRequest(view, folder, label);
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
	
	private ChangeRequest getChangeRequest(View view, Folder folder, Label label){
        int crNumber = getCRNumber(label);
		
		FolderListManager flm = new FolderListManager(view);
		flm.includeFolders(folder, -1);
		
		Server server = view.getServer();
		
		Type crType = server.typeForName(server.getTypeNames().CHANGEREQUEST);
		
		ItemListManager ilm = new ItemListManager(crType,flm);
		ilm.getItems();
		QueryInfo query = buildQuery(server, crType, crNumber);
		Items items = ilm.selectByQuery(query);
		
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
}
