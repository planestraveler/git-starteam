package com.starbase.starteam;

public class Status {
	private Status() {}
	
	public static final int CURRENT = 0;
	public static final int MERGE = 1;
	public static final int MISSING = 2;
	public static final int MODIFIED = 3;
	public static final int NEW = 4;
	public static final int OUTOFDATE = 5;
	public static final int UNKNOWN = 6;
}
