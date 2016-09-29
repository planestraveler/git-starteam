/*****************************************************************************
 * All public interface based on Starteam API are a property of Borland, 
 * those interface are reproduced here only for testing purpose. You should
 * never use those interface to create a competitive product to the Starteam
 * Server. 
 * 
 * The implementation is given AS-IS and should not be considered a reference 
 * to the API. The behavior on a lots of method and class will not be the
 * same as the real API. The reproduction only seek to mimic some basic 
 * operation. You will not find anything here that can be deduced by using
 * the real API.
 * 
 * Fake-Starteam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *****************************************************************************/
package com.starbase.starteam;

import org.ossnoize.fakestarteam.SerializableUser;
import org.ossnoize.fakestarteam.UserProvider;

public class UserAccount extends CacheRef
{
	private SerializableUser aUser;
	private GroupAccount[]   aGroup;
	public UserAccount(Server server) {
		aUser = null;
		aGroup = new GroupAccount[1];
		aGroup[0] = new GroupAccount();
	}

	protected UserAccount(int uid) {
		aUser = (SerializableUser) UserProvider.getInstance().getUser(uid);
		aGroup = new GroupAccount[1];
		aGroup[0] = new GroupAccount();
	}

	protected UserAccount(SerializableUser internal) {
		aUser = internal;
		aGroup = new GroupAccount[1];
		aGroup[0] = new GroupAccount();
	}
	
  public String getName()
  {
		return aUser.getName();
  }
  
  public String getEmailAddress()
  {
		return aUser.getEMail();
  }
  
  public GroupAccount[] getGroupAccounts()
  {
	  return aGroup;
  }

}
