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

import java.io.File;
import java.io.FilenameFilter;

public class RepositoryHelperFactory {

	private static RepositoryHelperFactory instance = null;
	// TODO: Add more filter to support more repository type. (Bazaar, Mercurial, ...)
	private final FilenameFilter gitFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.equals(".git");
		}
	};
	/** 
	 * We cache the help to not create a ton of process instance. Each helper should initialize it process
	 * and cache it locally. Ideally, the helper should block the current thread until all property are
	 * properly initialized. The process could also only block when all the process have finished properly.
	 */
	private RepositoryHelper helper;
	
	private RepositoryHelperFactory() {
	}
	
	public static RepositoryHelperFactory getFactory() {
		if(null == instance) {
			instance = new RepositoryHelperFactory();
		}
		return instance;
	}
	
	/**
	 * Create a new helper or return a cached instance of an already existing helper. The helper will be
	 * created according to the detected repository. If the repository is unknown, then the returned object
	 * will be null.
	 * @return An appropriate helper with respect to the detected repository. Null otherwise.
	 */
	public RepositoryHelper createHelper() {
		if(null != helper) {
			return helper;
		}
		File dir = new File(System.getProperty("user.dir"));
		String[] gitDir = dir.list(gitFilter);
		if(null != gitDir) {
			if(gitDir.length == 1) {
				helper = new org.sync.githelper.GitHelper();
				return helper;
			}
		}
		// TODO: Add more validation to support more repository type. (Bazaar, Mercurial, ...)
		return null;
	}
	
	/**
	 * Clear the cached helper to force it recreation.
	 */
	public void clearCachedHelper() {
		helper = null;
	}
}
