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

public class CheckoutOptions {

	private View view;
	private boolean EOLConversionEnabled;
	private OLEDate time;
	private boolean timeBased;
	private int labelId;
	private boolean labelBased;
	private boolean isTip;
	private boolean updateStatus;
	private int lockType;

	public CheckoutOptions(View view) {
		this.view = view;
		this.time = view.getConfiguration().getTime();
		this.timeBased = view.getConfiguration().isTimeBased();
		this.labelId = view.getConfiguration().getLabelID();
		this.labelBased = view.getConfiguration().isLabelBased();
		this.isTip = view.getConfiguration().isTip();
		this.updateStatus = false;
		this.lockType = 0;
	}

	public void setEOLConversionEnabled(boolean b) {
		EOLConversionEnabled = b;
	}

	public boolean getEOLConversionEnabled() {
		return EOLConversionEnabled;
	}
	
	public boolean isByDate() {
		return timeBased;
	}
	
	public void setCheckoutDate(OLEDate d) {
		time = d;
		timeBased = true;
	}
	
	public OLEDate getCheckoutDate() {
		return time;
	}
	
	public boolean isByLabel() {
		return labelBased;
	}
	
	public void setCheckoutLabelID(int id) {
		labelId = id;
		labelBased = true;
	}
	
	public int getCheckoutLabelID() {
		return labelId;
	}
	
	public boolean isByTip() {
		return isTip;
	}
	
	public void setTips() {
		isTip = true;
	}

	public boolean getUpdateStatus() {
		return updateStatus;
	}
	
	public void setUpdateStatus(boolean updateStatus) {
		this.updateStatus = updateStatus;
	}
	
	public int getLockType() {
		return lockType;
	}
}
