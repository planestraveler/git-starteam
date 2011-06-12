package org.sync;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;

public class MainEntry {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		File testFolder = new File("/tmp/gitTest");
		if(testFolder.exists() && testFolder.isDirectory()) {
			try {
				Git aGitDB = Git.open(testFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			InitCommand init = new InitCommand();
			init.setBare(true);
			init.setDirectory(testFolder);
			init.call();
		}
	}

}
