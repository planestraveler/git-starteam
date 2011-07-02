package org.ossnoize.fakestarteam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class UserProvider {

	private static UserProvider Reference = null;
	private static final String userList = "users.list";
	
	public static UserProvider getInstance() {
		if(Reference == null) {
			Reference = new UserProvider();
		}
		return Reference;
	}
	
	private Map<Integer, SerializableUser> users = new HashMap<Integer, SerializableUser>();
	
	private UserProvider() {
		readUserList();
	}
	
	private File getUsersFile() throws IOException {
		File rootDir = InternalPropertiesProvider.getInstance().getFile();
		String path = rootDir.getCanonicalPath() + File.separator + userList;
		return new File(path);
	}
	
	private void readUserList() {
		users.clear();
		ObjectInputStream in = null;
		
		try {
			File userFile = getUsersFile();
			if(userFile.exists()) {
				in = new ObjectInputStream(new FileInputStream(userFile));
				users = (Map<Integer, SerializableUser>) in.readObject();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(null != in) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private void writeUserList() {
		ObjectOutputStream out = null;
		
		try {
			File userFile = getUsersFile();
			out = new ObjectOutputStream(new FileOutputStream(userFile));
			out.writeObject(users);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(null != out) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void createNewUser(String uid) {
		for(Entry<Integer, SerializableUser> user : users.entrySet()) {
			if(user.getValue().getUID().equalsIgnoreCase(uid)) {
				throw new Error("Duplicate user id " + uid);
			}
		}
		int lastUserID = 1;
		if(users.size() > 0) {
			lastUserID = Collections.max(users.keySet()) + 1;
		}
		users.put(lastUserID, new SerializableUser(uid, lastUserID));
		writeUserList();
	}
}
