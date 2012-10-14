package org.ossnoize.fakestarteam.exception;

public class ObjectIdNotFoundError extends Error {

	public ObjectIdNotFoundError(int Id) {
		super("Object of id number " + Id + " was not found in the object database.");
	}
}
