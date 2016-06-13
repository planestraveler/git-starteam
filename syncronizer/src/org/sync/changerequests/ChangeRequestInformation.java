package org.sync.changerequests;

import java.util.regex.Pattern;

import com.starbase.starteam.ChangeRequest;
import com.starbase.starteam.Label;
import com.starbase.starteam.Server;
import com.starbase.starteam.User;


public class ChangeRequestInformation {

	private final String Release_Notes_Property = "Usr_ReleaseNote";
	private final String SIM_Issue_ID_Property = "Usr_SimIssueID";
	private final String JIRA_Issue_Property = "Usr_SimSRS";
	private final String Workflow_Status_Property = "Usr_StarFlowStatus";
	
	private ChangeRequest changeRequest;
	
	private String associatedFilePattern;

	public ChangeRequestInformation(ChangeRequest changeRequest, String associatedFilePattern){
		this.changeRequest = changeRequest;
		this.associatedFilePattern = associatedFilePattern;
	}
	
	
	public int getNumber(){
		return changeRequest.getNumber();
	}
	
	public User getResponsability(){
		Server server = changeRequest.getServer();
		return server.getUser(changeRequest.getResponsibility());		
	}
	
	public User getEnteredBy(){
		Server server = changeRequest.getServer();
		return server.getUser(changeRequest.getCreatedBy());
	}
	
	public String getDescription() {
		return changeRequest.getDescription();
	}

	public String getType(){
		return changeRequest.getRequestTypeDisplayName(changeRequest.getRequestType());
	}
	
	public String getWorkflowStatus(){
		Object workflowStatusObject = changeRequest.get(Workflow_Status_Property);
		return changeRequest.getFlagDisplayName((int) workflowStatusObject);
	}
	
	public String getFixDetails() {
		return changeRequest.getFix();
	}

	public String getAdressedIn(){
		int labelId = changeRequest.getAddressedIn();
		
		Label[] labels = changeRequest.getAllLabels();
		
		for(Label label : labels){
			if(label.getID() == labelId){
				return label.getName();
			}
		}
		
		return "";
	}
	
	public String getComponent(){
		return changeRequest.getComponent();
	}
	
	public String getCodeReviewer(){
		return changeRequest.getExternalReference();
	}
	
	public String getFixSummary(){
		return changeRequest.getSynopsis();
	}
	
	public String getReleaseNotes() {
		Object releaseNotestObject = changeRequest.get(Release_Notes_Property);
		return releaseNotestObject == null ? null: (String) releaseNotestObject;
	}

	public String getSimIssue(){
		Object simIssueObject = changeRequest.get(SIM_Issue_ID_Property);
		return simIssueObject == null ? null: (String) simIssueObject;
	}
	
	public String getJiraIssue(){
		Object jiraIssueObject = changeRequest.get(JIRA_Issue_Property);
		return jiraIssueObject == null ? null: (String) jiraIssueObject;
	}

	public Pattern getFilePattern() {		
		return Pattern.compile(associatedFilePattern);
	}

	@Override
	public String toString(){
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append("CR: ");
		stringBuilder.append(getNumber());
		stringBuilder.append("\n");
		
		/*
		stringBuilder.append("Workflow Status: ");
		stringBuilder.append(getWorkflowStatus());
		stringBuilder.append("\n");
		*/
		
		stringBuilder.append("Addressed In: ");
		stringBuilder.append(getAdressedIn());
		stringBuilder.append("\n");
		
		stringBuilder.append("Type: ");
		stringBuilder.append(getType());
		stringBuilder.append("\n");
		
		stringBuilder.append("Responsability: ");
		stringBuilder.append(getResponsability().toString());
		stringBuilder.append("\n");
		
		stringBuilder.append("Entered By: ");
		stringBuilder.append(getEnteredBy().toString());
		stringBuilder.append("\n");
		
		
		stringBuilder.append("------\n");
		
		stringBuilder.append("Code Reviewer: ");
		stringBuilder.append(getCodeReviewer());
		stringBuilder.append("        ");
		

		stringBuilder.append("Component: \n");
		stringBuilder.append(getComponent());
		stringBuilder.append("\n\n");
		
		stringBuilder.append("Fix Summary: \n");
		stringBuilder.append(getFixSummary());
		stringBuilder.append("\n\n");

		stringBuilder.append("ReleaseNotes: \n");
		stringBuilder.append(getReleaseNotes());
		stringBuilder.append("\n\n");
		
		stringBuilder.append("Description: \n");
		stringBuilder.append(getDescription());
		stringBuilder.append("\n\n");
		
		stringBuilder.append("Fix: \n");
		stringBuilder.append(getFixDetails());
		stringBuilder.append("\n\n");
		
		
		stringBuilder.append("------\n");
		
		stringBuilder.append("SimIssue: ");
		stringBuilder.append(getSimIssue());
		stringBuilder.append("        ");
		
		
		stringBuilder.append("JiraIssue: ");
		stringBuilder.append(getJiraIssue());
		
		return stringBuilder.toString();
	}
}
