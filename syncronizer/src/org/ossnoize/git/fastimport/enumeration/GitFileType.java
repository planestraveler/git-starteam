package org.ossnoize.git.fastimport.enumeration;

public enum GitFileType {

	Normal("100644"),
	Executable("100777");
	
	private String OctalRepresentation;

	public String getOctalRepresentation() {
		return OctalRepresentation;
	}

	private GitFileType(String octalRepresentation) {
		OctalRepresentation = octalRepresentation;
	}
	
}
