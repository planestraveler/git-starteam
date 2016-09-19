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

import java.util.HashMap;
import java.util.Map;

import com.starbase.starteam.File;
import com.starbase.starteam.Folder;
import com.starbase.starteam.Item;
import com.starbase.starteam.View;
import com.starbase.util.FileUtils;

/**
 * RenameFinder searches for rename events and caches results.
 * History items are not stored in the StarTeam cache and result in terrible performance,
 * so this class provides the necessary caching.
 *
 * This class is not threadsafe.
 */
public class RenameFinder {

	private Map<Folder, Folder> folderCache = new HashMap<Folder, Folder>();

	/**
	 * Returns the item after startTime when oldPath was renamed to file in view.
	 * Searches file and it's folders for a rename event between those two times.
	 * Returns null if a rename event is not found.
	 */
	public Item findEventItem(View view, String oldPath, String newPath, File file, long startTime) {
		String oldFolderName = FileUtils.getParent(oldPath, "/");
		String folderName = FileUtils.getParent(newPath, "/");
		String oldFileName = FileUtils.getName(oldPath, "/");

		// We could try to be clever here and use an ItemList to populate all of the history
		// item properties in a single trip, but each .getHistory() call will trigger a
		// round trip. There's a good chance we won't even need the history, so don't bother.

		// file was probably renamed during the time period
		if (!oldFileName.equals(file.getName())) {
			Item[] hist = file.getHistory();
			for (int i = 0; i < hist.length; i++) {
				File item = (File) hist[i];
				long time = item.getModifiedTime().getLongValue();
				if (time < startTime) {
					break;
				}
				if (i+1 < hist.length &&
					item.getName().equals(file.getName()) &&
					((File)hist[i+1]).getName().equals(oldFileName)) {
					return item;
				}
			}
		}

		// some folder was probably renamed during the time period
		if (null != oldFolderName && !oldFolderName.equals(folderName)) {
			for (Folder folder = file.getParentFolder(); folder != null; folder = folder.getParentFolder()) {
				Folder item = folderCache.get(folder);
				if (item != null) {
					return item;
				}
				Item[] hist = folder.getHistory();
				for (int i = 0; i < hist.length; i++) {
					item = (Folder) hist[i];
					long time = item.getModifiedTime().getLongValue();
					if (time < startTime) {
						break;
					}
					if (i + 1 < hist.length && !item.getName().equals(((Folder) hist[i + 1]).getName())) {
						cacheFolders(file.getParentFolder(), folder, item);
						return item;
					}
				}
			}
		}

		return null;
	}

	private void cacheFolders(Folder start, Folder end, Folder item) {
		for (Folder f = start; !f.equals(end); f = f.getParentFolder()) {
			folderCache.put(f, item);
		}
		folderCache.put(end, item);
	}

}
