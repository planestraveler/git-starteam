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
package org.ossnoize.git.fastimport;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.ossnoize.git.fastimport.exception.InvalidSha1;


public class Sha1Ref implements DataRef {
	private final static Pattern validate = Pattern.compile("[0-9a-fA-F]{40}");

	private String sha1;

	public Sha1Ref(String sha1) {
		if(!validate.matcher(sha1).matches()) {
			throw new InvalidSha1("This is not a valid sha1: " + sha1);
		}
		this.sha1 = sha1;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(sha1.getBytes());
	}

	@Override
	public String getId() {
		return sha1;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DataRef) {
			DataRef other = (DataRef) obj;
			return getId().equals(other.getId());
		}
		return false;
	}
	
	@Override
	public String toString() {
		return sha1;
	}

}
