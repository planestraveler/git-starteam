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

import java.util.regex.Pattern;

import com.starbase.starteam.ChangeRequest;
import com.starbase.starteam.Label;
import com.starbase.starteam.Server;
import com.starbase.starteam.User;


public class ChangeRequestInformation {

	private final String Release_Notes_Property = "Usr_ReleaseNote";
	private final String SIM_Issue_ID_Property = "Usr_SimIssueID";
	private final String JIRA_Issue_Property = "Usr_SimSRS";
	
	private ChangeRequest changeRequest;
	
	private String associatedFilePattern;
	private String CachedString;
	private boolean CachingIsDone;

	public ChangeRequestInformation(ChangeRequest changeRequest, String associatedFilePattern){
		this.changeRequest = changeRequest;
		this.associatedFilePattern = associatedFilePattern;
		CachedString = null;
		CachingIsDone = false;
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
		return changeRequest.getStatusDisplayName(changeRequest.getStatus());
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
		if (CachingIsDone) {
			return CachedString;
		}
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append("[CR");
		stringBuilder.append(getNumber());
		stringBuilder.append("] ");

		stringBuilder.append(getFixSummary());
		stringBuilder.append("\n\n");
		
		stringBuilder.append("Workflow Status: ");
		stringBuilder.append(getWorkflowStatus());
		stringBuilder.append("\n");
		
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
		
		CachedString = stringBuilder.toString();
		CachingIsDone = true;
		return CachedString;
	}
}
