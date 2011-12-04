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

import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Item;
import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.TypeNames;
import com.starbase.starteam.View;
import com.starbase.starteam.File;

public class GitImporter {
	private static final TypeNames typeNames = new TypeNames();
	private Server server;
	private Project project;
	private View view;
	private Map<String, File> sortedFileList = new TreeMap<String, File>();
	
	public GitImporter(Server s, Project p, View v) {
		server = s;
		project = p;
		view = v;
	}

	public void generateFastImportStream() {
		Folder root = view.getRootFolder();
		recursiveFilePopulation(root);
		
		String lastComment = "";
		int lastUID = -1;
		for(Map.Entry<String, File> e : sortedFileList.entrySet()) {
			System.err.println(e.getKey());
		}
	}

	private void recursiveFilePopulation(Folder f) {
		for(Item i : f.getItems(typeNames.FILE)) {
			for(Item hi : i.getHistory()) {
				if(hi instanceof File) {
					File historyFile = (File) hi;
					long modifiedTime = hi.getModifiedTime().getLongValue();
					int userid = hi.getModifiedBy();
					String path = hi.getParentFolderHierarchy() + java.io.File.separator + historyFile.getName();
					String comment = hi.getComment();
					String key = MessageFormat.format("{0,number,000000000000000}|{1,number,000000}|{2}|{3}", modifiedTime, userid, comment, path);
					sortedFileList.put(key, historyFile);
					System.err.println("Found file marked as: " + key);
				}
			}
		}
		for(Folder subfolder : f.getSubFolders()) {
			recursiveFilePopulation(subfolder);
		}
	}
}
