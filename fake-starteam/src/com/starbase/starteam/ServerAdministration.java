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
import org.ossnoize.fakestarteam.UserAccountProvider;
import org.ossnoize.fakestarteam.UserProvider;

public class ServerAdministration
{
  public UserAccount findUserAccount(int paramInt)
  {
		UserAccount account = UserAccountProvider.getInstance().getUserAccount(UserProvider.getInstance().getUser(paramInt).getID());
		if (null != account) {
			GroupAccount[] groups = account.getGroupAccounts();
			for (GroupAccount group : groups) {
				if (group.hasPermission(Permission.SERVER_ADMIN_USER_ACCOUNTS)) {
					return account;
				}
			}
			throw new ServerException();
		}
		return account;

  }
}
