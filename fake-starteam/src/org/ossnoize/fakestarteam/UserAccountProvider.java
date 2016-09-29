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

import java.util.HashMap;
import java.util.Map;

import com.starbase.starteam.UserAccount;

public class UserAccountProvider {

	private static UserAccountProvider Reference = null;
	
	public static UserAccountProvider getInstance() {
		if(Reference == null) {
			Reference = new UserAccountProvider();
		}
		return Reference;
	}
	
	public static void deleteInstance() {
		Reference = null;
	}
	
	private Map<Integer, UserAccount> accounts = new HashMap<Integer, UserAccount>();
	
	private UserAccountProvider() {
	}
	

	public void addUserAccount(Integer uid) {
		if (accounts.containsKey(uid)) {
			throw new Error("Duplicate user id " + uid);
		}
		accounts.put(uid, new FakeUserAccount(uid));
	}
	
	public boolean deleteUserAccount(int id) {
		if (accounts.containsKey(id)) {
			accounts.remove(id);
			return true;
		}
		return false;
	}

	public UserAccount getUserAccount(Integer uid) {
		if(null == uid)
			return null;
		return accounts.get(uid);
	}
}
