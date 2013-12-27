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

import java.io.IOException;
import java.io.OutputStream;
import org.ossnoize.git.fastimport.DataRef;

public class SmallRef {

	private String ref;

	/**
	 * Create a small ref object representing either a markable object in the
	 * fast-import stream or a shortened SHA-1 string. Any other git object
	 * small identifier is supported.
	 * Ex: HEAD, master, master~, abc1230ef, tag-0.12
	 * @param ref
	 */
	public SmallRef(String ref) {
		this.ref = ref;
	}
	
	/**
	 * Git the internal reference to the object
	 * @return The string representing the small reference.
	 */
	public String getRef() {
		return ref;
	}
	
	public SmallRef back(int nbCommit) {
		if(nbCommit < 0)
			return this;
		return new SmallRef(ref + "~" + nbCommit);
	}
	/**
	 * Try to convert the small ref to a valid DataRef that could be used
	 * by the fast-import stream.
	 * @return a mock DataRef object.
	 */
	public DataRef toDataRef() {
		if(ref.startsWith(":")) {
			return new DataRef() {
				@Override
				public void writeTo(OutputStream out) throws IOException {
					out.write(ref.getBytes());
				}
				
				@Override
				public String getId() {
					return ref;
				}
			};
		}
		return null;
	}
}
