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

import org.ossnoize.git.fastimport.enumeration.FeatureType;

public class Feature implements FastImportObject {
	private static final String FEATURE_SP = "feature ";
	private FeatureType type;
	private String arguments;

	public Feature(FeatureType type) {
		this(type, null);
	}
	
	public Feature(FeatureType type, String arguments) {
		this.type = type;
		this.arguments = arguments;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		StringBuilder feature = new StringBuilder(32);
		feature.append(FEATURE_SP).append(type.getName());
		if(type.hasArguments() && null != arguments) {
			feature.append('=').append(arguments);
		}
		feature.append('\n');
		out.write(feature.toString().getBytes("UTF-8"));
		out.flush();
	}

}
