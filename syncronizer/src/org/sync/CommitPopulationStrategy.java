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
package org.sync;

import java.util.List;
import java.util.NavigableMap;
import java.util.Set;

import org.sync.util.CommitInformation;

import com.starbase.starteam.File;
import com.starbase.starteam.Folder;
import com.starbase.starteam.Label;

/**
 * Interface defining the used strategy to populate the files modification in
 * starteam.
 */
public interface CommitPopulationStrategy {

	/**
	 * Main method for generating the list of commit to build. It will use the
	 * provided root to figure all the files to import and provide a list of
	 * commit to recreate in git. This method will also recover information about
	 * deleted files.
	 * 
	 * @param head
	 *          The name of the branch on which the branch will Starteam check-in
	 *          will be imported
	 * @param root
	 *          The root folder we will recursively populate
	 **/
	void filePopulation(String head, Folder root);

	/**
	 * In a case where we need to resume importation on a branch which
	 * git-starteam has never seen. It is used to let it know of the actual list
	 * of files in the git head we are importing over. Useful in the case we are
	 * creating a new branch based on a new folder or based on a new view.
	 * 
	 * @param initialPaths
	 *          A list of path
	 **/
	void setInitialPathList(Set<String> initialPaths);

	/**
	 * Provide the list of commit to rebuild the history since the last execution
	 * of the strategy.
	 * 
	 * @return the list of commit to reproduce
	 **/
	NavigableMap<CommitInformation, File> getListOfCommit();

	/**
	 * Provide the list of files that was never seen in the provided folder. Those
	 * files are qualified for deletion through some garbage collection process.
	 * 
	 * @return the list of path that wasn't seen during the commit population
	 **/
	Set<String> pathToDelete();

	/**
	 * Provide the repository helper to the Strategy
	 * 
	 * @param helper
	 *          The Repository helper used by the fast-export process
	 */
	void setRepositoryHelper(RepositoryHelper helper);

	/**
	 * Provide the last commit time, to prevent jump in time while importing
	 * commit from Starteam. Using label configuration may cause such issue.
	 * 
	 * @param earliestTime
	 *          The least acceptable time for a commit.
	 */
	void setLastCommitTime(java.util.Date earliestTime);

	/**
	 * Add verbosity to the construction of the commit for debugging purpose
	 * 
	 * @param verbose
	 *          true for verbosity, false for none
	 */
	void setVerboseLogging(boolean verbose);

	/**
	 * Provide a list of all the files that was lastly seen by the previous pass
	 * of the strategy
	 * 
	 * @return a list of git path
	 */
	List<String> getLastFiles();

	/**
	 * Provide the label on which the population of the commit will be based on
	 * which could be used by some strategy.
	 * 
	 * @param current
	 *          The label to use during the commit population.
	 */
	void setCurrentLabel(Label current);

	/**
	 * Based on the provided label, should we create a tag based on it.
	 * 
	 * @return true if a tag should be created false otherwise
	 */
	boolean isTagRequired();

	/**
	 * Tell the strategy to look into the RecycleBin to discover more inforamtion
	 * about removal of a file to properly give ownership.
	 *
	 * This feature is enabled by default.
	 */
	void setFileRemoveExtendedInformation(boolean enable);

}
