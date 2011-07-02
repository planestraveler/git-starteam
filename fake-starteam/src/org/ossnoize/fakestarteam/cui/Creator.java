package org.ossnoize.fakestarteam.cui;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.SerializableProject;

import com.starbase.starteam.Project;
import com.starbase.starteam.Server;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

public class Creator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option createProject = parser.addStringOption('P', "project");
		
		try {
			parser.parse(args);
		} catch (IllegalOptionValueException e) {
			System.err.println(e.getMessage());
			printHelp();
			System.exit(1);
		} catch (UnknownOptionException e) {
			System.err.println(e.getMessage());
			printHelp();
			System.exit(2);
		}
		String projectName = (String) parser.getOptionValue(createProject);
		String[] remainder = parser.getRemainingArgs();
		if(remainder.length > 0) {
			try {
				InternalPropertiesProvider.getInstance().setFileName(remainder[0]);
			} catch (ZipException e) {
				System.err.println(e.getMessage());
				System.exit(3);
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.exit(4);
			}
		}
		Server server = new Server("localhost", 23456);
		if(null != projectName) {
			Project prj = new SerializableProject(server, projectName, File.separator);
			prj.update();
		}
	}

	private static void printHelp() {
		System.out.println("-P <project name>");
	}

}
