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

	@Override
	public int compare(Label arg0, Label arg1) {
		long date0 = 0;
		long date1 = 0;
		
		try {
			date0 = getLabelDate(arg0).getLongValue();
			date1 = getLabelDate(arg1).getLongValue();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
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
		return 0;
	}
	
	public static OLEDate getLabelDate(Label revisionLabel) throws ParseException{
		String labelDescription = revisionLabel.getDescription();
		int buildDateDescriptionIndex = labelDescription.indexOf(buildDateToken);
		String buildDateDescription = labelDescription.substring(buildDateDescriptionIndex + buildDateToken.length());
	    String date = buildDateDescription.substring(1, buildDateDescription.indexOf(')'));
	
	    DateFormat dateFormat = new java.text.SimpleDateFormat(buildDateFormat);
	
	    return new OLEDate(dateFormat.parse(date));
	}
}
