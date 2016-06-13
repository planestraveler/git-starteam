package org.sync.changerequests;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Label;
import com.starbase.starteam.View;

public interface IChangeRequestsHelper {

	public boolean isChangeRequestsFeatureEnable();
	
	public boolean labelHasCRInfoAttached(Label label);

	public boolean commentMatchFilter(String comment);
	
	public ChangeRequestInformation getChangeRequestsInformation(View view, Folder folder, Label label);
	
	public void setFilePattern(String filePattern);
	
	public int getCRNumber(Label label);

}
