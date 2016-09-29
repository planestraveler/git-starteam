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
			Pattern commentPattern = Pattern.compile("^\\s*#.*");
			// From http://emailregex.com/ (with required escaping to be a valid string literal) 
			String  emailPart    = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
			Pattern emailPattern = Pattern.compile("^([^=]*)=\\s*(" + emailPart + ")\\s*");
			Pattern emptyPattern = Pattern.compile("^\\s*$");
			
			int lineno = 0;
		    while ((line = reader.readLine()) != null) {
		    	++lineno;
		    	Matcher commentMatcher = commentPattern.matcher(line);
		    	if (commentMatcher.matches()) {
		    		continue;
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
