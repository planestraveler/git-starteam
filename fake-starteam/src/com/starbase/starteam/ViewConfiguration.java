/*****************************************************************************
 * All public interface based on Starteam API are a property of Borland, 
 * those interface are reproduced here only for testing purpose. You should
 * never use those interface to create a competitive product to the Starteam
 * Server. 
 * 
 * The implementation is given AS-IS and should not be considered a reference 
 * to the API. The behavior on a lots of method and class will not be the
 * same as the real API. The reproduction only seek to mimic some basic 
 * operation. You will not found anything here that can be deduced by using
 * the real API.
 * 
 * Fake-Starteam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *****************************************************************************/
package com.starbase.starteam;

import com.starbase.util.OLEDate;

public class ViewConfiguration {

	private boolean tip;
	private OLEDate time;
	private boolean timeBased;
	private int labelID;
	private boolean labelBased;

	private ViewConfiguration() {
		tip = false;
		timeBased = false;
		labelBased = false;
	}

	public static ViewConfiguration createTip() {
		ViewConfiguration ret = new ViewConfiguration();
		ret.setTip(true);
		return ret;
	}
	
	public static ViewConfiguration createFromTime(OLEDate oleDate) {
		ViewConfiguration ret = new ViewConfiguration();
		ret.setTime(oleDate);
		return ret;
	}
	
	public static ViewConfiguration createFromLabel(int labelId) {
		ViewConfiguration ret = new ViewConfiguration();
		ret.setLabelId(labelId);
		return ret;
	}

	private void setLabelId(int id) {
		labelID = id;
		labelBased = true;
	}

	private void setTime(OLEDate oleDate) {
		time = oleDate;
		timeBased = true;
	}

	private void setTip(boolean b) {
		tip = b;
	}
	
	public boolean isTip() {
		return tip;
	}
	
	public boolean isTimeBased() {
		return timeBased;
	}
	
	public boolean isLabelBased() {
		return labelBased;
	}
	
	public OLEDate getTime() {
		return time;
	}
	
	public int getLabelID() {
		return labelID;
	}

}
