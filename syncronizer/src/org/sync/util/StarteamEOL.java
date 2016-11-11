package org.sync.util;

public enum StarteamEOL {
	BINARY(0),
	CLIENTDEFINE(256),
	CR(257),
	CRLF(258),
	LF(259);
	
	final private int value;
	
	StarteamEOL(int value){
		this.value = value;
	}
	
	public int value(){
		return value;
	}
}
