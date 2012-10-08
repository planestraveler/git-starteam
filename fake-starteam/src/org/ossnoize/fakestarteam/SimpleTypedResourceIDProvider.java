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
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.starbase.starteam.SimpleTypedResource;
import com.starbase.starteam.View;

public class SimpleTypedResourceIDProvider implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5926765067144233123L;
	private static final String resourceIDFile = "resourceid.gz";
	private static SimpleTypedResourceIDProvider provider = null;
	
	private Set<Integer> assignedResourceID = new HashSet<Integer>();
	private transient Map<View, Map<Integer, SimpleTypedResource>> existingResource; 

	private Random generator = new Random(); 
	
	private SimpleTypedResourceIDProvider() {	
	}

	public static SimpleTypedResourceIDProvider getProvider() {
		if(null == provider) {
			if(!readFromFile()) {
				provider = new SimpleTypedResourceIDProvider();
				provider.postInit();
			}
		}
		return provider;
	}
	
	private void postInit() {
		existingResource = new HashMap<View, Map<Integer,SimpleTypedResource>>();
	}
	
	private static boolean readFromFile() {
		boolean ret = false;
		ObjectInputStream in = null;
		GZIPInputStream gzin = null;
		
		try {
			File rootDir = InternalPropertiesProvider.getInstance().getFile();
			File path = new File(rootDir.getCanonicalPath() + File.separator + resourceIDFile);
			if(path.exists()) {
				gzin = new GZIPInputStream(new FileInputStream(path));
				in = new ObjectInputStream(gzin);
				Object obj = in.readObject();
				if(obj instanceof SimpleTypedResourceIDProvider) {
					provider = (SimpleTypedResourceIDProvider) obj;
					provider.postInit();
					ret = true;
				}
			}
		} catch (IOException ie) {
			ie.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			FileUtility.close(in, gzin);
		}
		return ret;
	}

	public int registerNew(View view, SimpleTypedResource resource) {
		int id = generator.nextInt();
		if(assignedResourceID.contains(id) || 0 == id)
		{
			return registerNew(view, resource);
		}
		else
		{
			assignedResourceID.add(id);
			if(!existingResource.containsKey(view)) {
				existingResource.put(view, new HashMap<Integer, SimpleTypedResource>());
			}
			existingResource.get(view).put(id, resource);
			saveNewID();
		}
		return id;
	}

	private void saveNewID() {
		GZIPOutputStream gzout = null;
		ObjectOutputStream out = null;

		try {
			File rootDir = InternalPropertiesProvider.getInstance().getFile();
			File path = new File(rootDir.getCanonicalPath() + File.separator + resourceIDFile);
			
			gzout = new GZIPOutputStream(new FileOutputStream(path));
			out = new ObjectOutputStream(gzout);
			out.writeObject(this);
			System.out.println("Saved " + assignedResourceID.size() + " ressource ID");
		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			FileUtility.close(gzout, out);
		}
	}

	public void registerExisting(View view, int id, SimpleTypedResource resource) {
		if(!existingResource.containsKey(view)) {
			existingResource.put(view, new WeakHashMap<Integer, SimpleTypedResource>());
		}
		existingResource.get(view).put(id, resource);
		if(!assignedResourceID.contains(id)) {
			assignedResourceID.add(id);
			saveNewID();
		}
	}

	public SimpleTypedResource findExisting(View view, int id) {
		if(existingResource.containsKey(view)) {
			if(existingResource.get(view).containsKey(id)) {
				return existingResource.get(view).get(id);
			}
		}
		return null;
	}

	public void clearExisting(View view, int objectID) {
		if(existingResource.containsKey(view)) {
			if(existingResource.get(view).containsKey(objectID)) {
				existingResource.get(view).remove(objectID);
			}
		}
		
	}
	
}
