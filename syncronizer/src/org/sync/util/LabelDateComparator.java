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

import java.util.Comparator;

import com.starbase.starteam.Label;

public class LabelDateComparator implements Comparator<Label> {
  
  private AlphanumComparator Fallback = new AlphanumComparator();
	@Override
	public int compare(Label arg0, Label arg1) {
		if(arg0.getRevisionTime().getLongValue() > arg1.getRevisionTime().getLongValue())
		{
			return 1;
		}
		else if(arg0.getRevisionTime().getLongValue() < arg1.getRevisionTime().getLongValue())
		{
			return -1;
		}
		return Fallback.compare(arg0.getName(), arg1.getName());
	}

}
