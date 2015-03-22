package fr.pludov.ardfocus.utils;

import java.util.ArrayList;
import java.util.IdentityHashMap;

public class WeakListenerOwner {
	final Object owner;
	final IdentityHashMap<Object, Integer> listeners;
	
	public WeakListenerOwner(Object realOwner) {
		this.owner = realOwner;
		this.listeners = new IdentityHashMap<Object, Integer>();
	}
	
	void addListener(Object listener)
	{
		Integer oldCount = listeners.put(listener, 1);
		if (oldCount != null) {
			listeners.put(listener, 1 + oldCount);
		}
	}
	
	void removeListener(Object listener)
	{
		Integer count = listeners.remove(listener);
		if (count != null && count.intValue() > 1) {
			listeners.put(listener, count.intValue() - 1);
		}
	}

}
