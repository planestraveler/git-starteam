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

import com.starbase.starteam.User;

public class UserProvider {

	private static UserProvider Reference = null;
	private static final String userList = "users.list";
	
	public static UserProvider getInstance() {
		if(Reference == null) {
			Reference = new UserProvider();
		}
		return Reference;
	}
	
	public static void deleteInstance() {
		Reference = null;
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
	
	public boolean deleteUser(int id) {
		if(users.containsKey(id)) {
			users.remove(id);
			writeUserList();
			return true;
		}
		return false;
	}

	public SerializableUser findUser(String uid) {
		if(null == uid)
			return null;
		for(Entry<Integer, SerializableUser> user : users.entrySet()) {
			if(uid.equals(user.getValue().getUID())) {
				return user.getValue();
			}
		}
		return null;
	}

	public void applyChanges() {
		writeUserList();
	}

	public User getUser(int id) {
		return users.get(id);
	}
}
