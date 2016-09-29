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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserMapping {

	private Map<String, String> mapping;
	private String              defaultDomain;
	
	public UserMapping(String filename) {
		this.mapping = new HashMap<String,String>();
		if (filename != null) {
			parseDirectory(filename);
		}
	}
	protected UserMapping(InputStream stream) {
		this.mapping = new HashMap<String,String>();
		if (stream != null) {
		  parseDirectory(stream);
		}
	}
	
	public void setDefaultDomain(String domain) {
		this.defaultDomain = domain;
	}
	
	public String getEmail(String name) {
		String email = mapping.get(name);
		if (null == email) {
			
			if (defaultDomain != null) {
				email = name.replaceAll("\\.", "").replaceAll(" ", ".") + "@" + defaultDomain;
			}
			else {
				email = "unknown@noreply.com";
			}
		}
		return email;
	}
	
	
	private void parseDirectory(InputStream stream) {
		String line;
		try (
			InputStreamReader streamReader = new InputStreamReader(stream, Charset.forName("UTF-8"));
		    BufferedReader reader = new BufferedReader(streamReader);
		) {
			Pattern commentPattern = Pattern.compile("^([^#]*)#.*");
			Pattern emailPattern = Pattern.compile("^([^=]*)=\\s*([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})\\s*");
			Pattern emptyPattern = Pattern.compile("^\\s*$");
			
			int lineno = 0;
		    while ((line = reader.readLine()) != null) {
		    	++lineno;
		    	Matcher commentMatcher = commentPattern.matcher(line);
		    	if (commentMatcher.matches()) {
		    		line = commentMatcher.group(1);
		    	}
		    	Matcher emailMatcher = emailPattern.matcher(line);
		    	if (emailMatcher.matches()) {
		    		String key   = emailMatcher.group(1).trim();
		    		String email = emailMatcher.group(2).trim();
		    		this.mapping.put(key, email);
		    	}
		    	else {
		    		Matcher emptyMatcher = emptyPattern.matcher(line);
		    		if (!emptyMatcher.matches()) {
		    			Log.log("Invalid email mapping at line " + lineno + ": " + line);
		    		}
		    	}
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.log("Error parsing mapping: " + e.getMessage());
		}
	}
	
	private void dump() {
		System.out.println("UserMapping dump:");
		for (Entry<String, String> entry : mapping.entrySet()) {
			System.out.println("  " + entry.getKey() + " => " + entry.getValue());
		}
	}
	
	private void parseDirectory(String filename) {
		try (
		    InputStream stream = new FileInputStream(filename);
		) {
			parseDirectory(stream);
		} catch (FileNotFoundException e) {
			Log.log("Email mapping file \"" + filename + "\" not found.");
		} catch (IOException e) {
			Log.log("Error opening mapping file \"" + filename + "\": " + e.getMessage());
		}
	}
}
