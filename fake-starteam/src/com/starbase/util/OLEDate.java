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
package com.starbase.util;

import java.util.Date;

public class OLEDate {
	
	private Long javaTime;
	public static OLEDate CURRENT_SERVER_TIME = new OLEDate(true); 
	
	public OLEDate() {
		this.javaTime = System.currentTimeMillis();
	}
	
	private OLEDate(boolean serverTime) {
		if(!serverTime) {
			this.javaTime = System.currentTimeMillis();
		} else {
			this.javaTime = null;
		}
	}
	
	public OLEDate(long javaTime) {
		this.javaTime = javaTime;
	}
	
	public OLEDate(Date createdDate) {
		this.javaTime = createdDate.getTime();
	}

	public long getLongValue() {
		if(null != javaTime) {
			return javaTime;
		} else {
			return System.currentTimeMillis();
		}
	}
	
	public java.util.Date createDate() {
		return new java.util.Date(javaTime);
	}
}
