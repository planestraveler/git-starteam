package org.sync;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class MainEntry {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		File testFolder = new File("/tmp/gitTest");
		if(testFolder.exists() && testFolder.isDirectory()) {
			FileRepositoryBuilder repBuilder = new FileRepositoryBuilder();
			repBuilder.setGitDir(testFolder);
			repBuilder.readEnvironment();
			repBuilder.findGitDir();
			try {
				repBuilder.build();
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
