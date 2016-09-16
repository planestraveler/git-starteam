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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Comparator;

import com.starbase.starteam.Label;
import com.starbase.util.OLEDate;

public class RevisionDateComparator implements Comparator<Label> {
private static String buildDateToken = "build.date=";
private static String buildDateFormat = "MM/dd/yy hh:mm a";

  private AlphanumComparator Fallback = new AlphanumComparator();
	@Override
	public int compare(Label arg0, Label arg1) {
		long date0 = 0;
		long date1 = 0;
		
		try {
			date0 = getLabelDate(arg0).getLongValue();
			date1 = getLabelDate(arg1).getLongValue();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(date0 > date1)
		{
			return 1;
		}
		else if(date0 < date1)
		{
			return -1;
		}
		return Fallback.compare(arg0.getName(), arg1.getName());
	}
	
	public static OLEDate getLabelDate(Label revisionLabel) throws ParseException{
		String labelDescription = revisionLabel.getDescription();
		int buildDateDescriptionIndex = labelDescription.indexOf(buildDateToken);
		if (buildDateDescriptionIndex >= 0) {
			String buildDateDescription = labelDescription.substring(buildDateDescriptionIndex + buildDateToken.length());

			String date = buildDateDescription.startsWith("0")
			    ? buildDateDescription.substring(1, buildDateDescription.indexOf('('))
			    : buildDateDescription.substring(0, buildDateDescription.indexOf('('));

			DateFormat dateFormat = new java.text.SimpleDateFormat(buildDateFormat);

			return new OLEDate(dateFormat.parse(date.trim()));
		}
		return revisionLabel.getRevisionTime();
	}
}
