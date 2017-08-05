package clayborn.universalremote.items;

import java.util.ArrayList;

public class ItemRegistry {
	
	public static class ItemList {
		
		// iterable list
		public ArrayList<ItemBase> All;
		
		// each item by name
		public ItemUniversalRemote UniveralRemote;
		
		// constructor
		public ItemList()
		{
			All = new ArrayList<>();
			
			// create the object instances!
			All.add(UniveralRemote = new ItemUniversalRemote("item_universal_remote", true));
		}
		
	}
	
	// singleton pattern
	private static ItemList m_itemList;
	
	public static ItemList Items()
	{
		if (m_itemList == null) m_itemList = new ItemList();
		return m_itemList;
	}
	
}
