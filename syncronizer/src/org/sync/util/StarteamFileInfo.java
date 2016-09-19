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
package org.sync.util;

import java.io.Serializable;

public class StarteamFileInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2131516984987259692L;
	private int id;
	private int version;
	private int contentVersion;
	private String filename;

	public StarteamFileInfo(String filename, int id, int version, int content) {
		this.filename = filename;
		this.id = id;
		this.version = version;
		this.contentVersion = content;
	}
	
	public int getId() {
		return id;
	}
	
	public int getVersion() {
		return version;
	}
	
	public int getContentVersion() {
		return contentVersion;
	}

	public String getFilename() {
		return filename;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}

	public void setContentVersion(int version) {
		this.contentVersion = version;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

}
