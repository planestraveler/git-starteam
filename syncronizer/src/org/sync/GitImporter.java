package org.sync;

import java.util.Map;
import java.util.TreeMap;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Project;
import com.starbase.starteam.View;
import com.starbase.starteam.File;

public class GitImporter {
	private Project project;
	private View view;
	private Map<Long, File> timeAndFile;
	
	public GitImporter(Project p, View v) {
		project = p;
		view = v;
		timeAndFile = new TreeMap<Long, File>();
	}

	public void generateFastImportStream() {
		Folder root = view.getRootFolder();
	}
}
