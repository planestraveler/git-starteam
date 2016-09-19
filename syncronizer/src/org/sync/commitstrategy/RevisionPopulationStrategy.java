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

import java.util.ArrayList;
import java.util.List;

import org.sync.Log;

import com.starbase.starteam.File;
import com.starbase.starteam.Folder;
import com.starbase.starteam.Label;
import com.starbase.starteam.View;

public class RevisionPopulationStrategy extends BasePopulationStrategy {

	private Label currentLabel;
	private boolean tagRequired;

	public RevisionPopulationStrategy(View view) {
		super(view);
	}

	@Override
	public void setCurrentLabel(Label current) {
		currentLabel = current;
		tagRequired = false;
	}

	@Override
	public boolean isTagRequired() {
		return tagRequired;
	}

	@Override
	protected void processFileForCommit(String head, File historyFile, String path) {
		if (currentLabel == null) {
			throw new RuntimeException("The CurrentLabel need to be set prior to processing files");
		}
		File correctTipFromLabel = (File) historyFile.getFromHistoryByLabelID(currentLabel.getID());
		if (correctTipFromLabel != null) {
			super.processFileForCommit(head, correctTipFromLabel, path);
			tagRequired = true;
			/*
			 * Log.logf("File %s%s is attached to label <%s>",
			 * correctTipFromLabel.getParentFolder().getFolderHierarchy(),
			 * correctTipFromLabel.getName(), CurrentLabel.getName());
			 */
		}
	}

	@Override
	public void filePopulation(String head, Folder f) {
		List<String> backupPath = new ArrayList<String>();
		backupPath.addAll(lastFiles);
		super.filePopulation(head, f);
		if (!tagRequired) {
			lastFiles.addAll(backupPath);
			deletedFiles.clear();
			currentCommitList.clear();
			Log.logf("No modifications were attached to folder <%s> in label <%s>", f.getFolderHierarchy(),
			    currentLabel.getName());
		}
	}
}
