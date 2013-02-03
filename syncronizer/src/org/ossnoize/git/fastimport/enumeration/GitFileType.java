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
package org.ossnoize.git.fastimport.enumeration;

public enum GitFileType {

	Normal("100644"),
	Executable("100755"),
	SymbolicLink("120000"),
	NullFile("000000");
	
	private String OctalRepresentation;

	public String getOctalRepresentation() {
		return OctalRepresentation;
	}

	private GitFileType(String octalRepresentation) {
		OctalRepresentation = octalRepresentation;
	}

	public static GitFileType fromOctal(String octalString) {
		for(GitFileType type : values()) {
			if(type.getOctalRepresentation().equals(octalString)) {
				return type;
			}
		}
		throw new UnsupportedOperationException("Cannot find corresponding type from octal representation: <" + octalString + ">");
	}
}
