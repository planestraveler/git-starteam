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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ItemList implements Runnable, Items {

	private List<Item> items;
	
	public ItemList() {
		items = new ArrayList<Item>();
	}
	
	@Override
	public Item getAt(int pos) {
		return items.get(pos);
	}

	@Override
	public int size() {
		return items.size();
	}
	
	public void addItem(Item item) {
		items.add(item);
	}
	
	public void populateNow(String[] properties) {
		// TODO: nothing as we are not network based.
	}

	@Override
	@Deprecated()
	public void run() {
	}

}
