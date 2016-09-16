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
package org.sync.commitstrategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sync.Log;
import org.sync.changerequests.ChangeRequestInformation;
import org.sync.changerequests.ChangeRequestsHelper;
import org.sync.changerequests.ChangeRequestsHelper.NoSuchChangeRequestException;
import org.sync.changerequests.ChangeRequestsHelperFactory;

import com.starbase.starteam.File;
import com.starbase.starteam.Label;
import com.starbase.starteam.View;

public class ChangeRequestPopulationStrategy extends BasePopulationStrategy {
	private ChangeRequestInformation crInfo;
	private ChangeRequestsHelper changeRequestHelper;

	public ChangeRequestPopulationStrategy(View view, String filePattern) {
		super(view);
		changeRequestHelper = ChangeRequestsHelperFactory.getFactory().createHelper(view);
		changeRequestHelper.setFilePattern(filePattern);
	}

	public void setChangeRequestInformation(ChangeRequestInformation info) {
		crInfo = info;
	}
	
	@Override
	protected String correctedComment(File historyFile) {
		String comment = super.correctedComment(historyFile);
		if (crInfo != null) {
			long start = System.nanoTime();
			// If CR specified, we overwrite the comment of the specified files with
			// the content of the CR.
			Pattern filePattern = crInfo.getFilePattern();
			Matcher matcher = filePattern.matcher(historyFile.getFullName());

			if (matcher.matches()) {
				comment = crInfo.toString();
			}
		}
		return comment;
	}

	@Override
	protected void createCommitInformation(String path, File fileToCommit, int counter) {
		if (!changeRequestHelper.commentMatchFilter(fileToCommit.getComment())) {
			super.createCommitInformation(path, fileToCommit, counter);
		}
	}

	@Override
	public void setCurrentLabel(Label current) {
		if (changeRequestHelper.labelHasCRInfoAttached(current)) {
			try {
				crInfo = changeRequestHelper.getChangeRequestsInformation(current);
			} catch (NoSuchChangeRequestException ex) {
				Log.logf("Could not find change request: %s", ex.getMessage());
				crInfo = null;
			}
		} else {
			crInfo = null;
		}
	}

	@Override
	public boolean isTagRequired() {
		// We want tag in the situation
		return crInfo == null;
	}
}
