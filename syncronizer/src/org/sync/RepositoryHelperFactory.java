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

public class RepositoryHelperFactory {

	private static RepositoryHelperFactory instance = null;
	/** 
	 * We cache the help to not create a ton of process instance. Each helper should initialize it process
	 * and cache it locally. Ideally, the helper should block the current thread until all property are
	 * properly initialized. The process could also only block when all the process have finished properly.
	 */
	private RepositoryHelper helper;
	private String preferredPath;
	private boolean createRepo;
	
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
		if (null == helper) {
			try {
				// TODO: Add more validation to support more repository type. (Bazaar, Mercurial, ...)
				helper = new org.sync.githelper.GitHelper(preferredPath, createRepo);
			} catch (Exception e) {
				e.printStackTrace();
				clearCachedHelper();
			}
		}
		
		return helper;
	}
	
	/**
	 * Clear the cached helper to force it recreation.
	 */
	public void clearCachedHelper() {
		helper = null;
	}

	public void setPreferedPath(String pathToProgram) {
		preferredPath = pathToProgram;
	}

	public void setCreateRepo(boolean create) {
		createRepo = create;
	}
}
