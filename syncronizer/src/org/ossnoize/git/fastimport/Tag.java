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
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tag implements FastImportObject {

	// unsafe for multi-threaded use
	private final static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("Z");

	private final String tagName;
	private final DataRef committish;
	private final String taggerName;
	private final String taggerEmail;
	private final Date tagDate;
	private final Data comment = new Data();

	public Tag(String tagName, DataRef committish, String taggerName, String taggerEmail,
				 Date tagDate, String comment) throws IOException {
		this.tagName = tagName;
		this.committish = committish;
		this.taggerName = taggerName;
		this.taggerEmail = taggerEmail;
		this.tagDate = tagDate;
		this.comment.writeData(comment.getBytes("UTF-8"));
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		StringBuilder tagMsg = new StringBuilder();
		tagMsg.append("tag ");
		if (tagName.length() > 255)
		{
			tagMsg.append(tagName.substring(0,255));
		}
		else
		{
			tagMsg.append(tagName);
		}
		tagMsg.append('\n');
		tagMsg.append("from ").append(committish.getId()).append('\n');
		tagMsg.append("tagger ").append(taggerName)
			.append(" <").append(taggerEmail).append("> ")
			.append(tagDate.getTime() / 1000).append(' ').append(DATEFORMAT.format(tagDate))
			.append('\n');
		out.write(tagMsg.toString().getBytes("UTF-8"));
		comment.writeTo(out);
	}

}
