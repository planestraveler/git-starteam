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
package com.starbase.starteam;

import org.ossnoize.fakestarteam.InternalPropertiesProvider;
import org.ossnoize.fakestarteam.ProjectProvider;
import org.ossnoize.fakestarteam.SerializableUser;
import org.ossnoize.fakestarteam.UserProvider;

import com.starbase.util.OLEDate;
import com.starbase.starteam.ServerAdministration;

public class Server {

	private String Address;
	private int Port;
	private boolean connected;
	private User loggedUser;
	private PropertyNames propertyNames = new PropertyNames();
	private PropertyEnums propertyEnums = new PropertyEnums();
	private TypeNames typeNames = new TypeNames();

	public Server(String address, int port) {
		Address = address;
		Port = port;
		InternalPropertiesProvider.getInstance().setCurrentServer(this);
	}
	
	public ServerAdministration getAdministration()	{
		return new ServerAdministration();
	}
	
	public int getPort() {
		return Port;
	}
	
	public String getAddress() {
		return Address;
	}
	
	public boolean ping() {
		return true;
	}
	
	public void connect() {
		connected = true;
	}
	
	public boolean isLoggedOn() {
		return (null != loggedUser);
	}
	
	public Project[] getProjects() {
		if(connected && (null != loggedUser)) {
			return ProjectProvider.getInstance().listProject();
		}
		return new Project[0];
	}
	
	public int logOn(java.lang.String logOnName, java.lang.String password) {
		SerializableUser u = UserProvider.getInstance().findUser(logOnName);
		if(null != u) {
			if(u.isCorrectPassword(password)) {
				loggedUser = u;
				return u.getID();	
			} else {
				System.err.println("Wrong password");
			}
		} else {
			System.err.println("Unknown user");
		}
		return 0;
	}
	
	public Type typeForName(String typeName) {
		if(null == typeName)
			throw new IllegalArgumentException("Typename cannot be null");
		if(!TypeNames.typeNamesList.contains(typeName))
			throw new TypeNotFoundException(typeName);
		return new Type(typeName, this);
	}
	
	public MyUserAccount getMyUserAccount() {
		return new MyUserAccount(loggedUser);
	}
	
	public User getUser(int id) {
		return UserProvider.getInstance().getUser(id);
	}
	
	public PropertyNames getPropertyNames() {
		return propertyNames;
	}
	
	public PropertyEnums getPropertyEnums() {
		return propertyEnums;
	}

	public void disconnect() {
		connected = false;
	}
	
	public OLEDate getCurrentTime() {
		return InternalPropertiesProvider.getInstance().getCurrentTime();
	}

    public void setAutoReconnectEnabled(boolean bEnabled) {
        // nothing to do
    }

    public void setAutoReconnectAttempts(int nAttempts) {
        // nothing to do
    }

    public void setAutoReconnectWait(int nSeconds) {
        // nothing to do
    }

	public TypeNames getTypeNames() {
		return typeNames;
	}

	public void setKeepAlive(boolean b) {
		// Nothing to do
	}

	public void setKeepAliveInterval(int i) {
		// Nothing to do
	}
}
