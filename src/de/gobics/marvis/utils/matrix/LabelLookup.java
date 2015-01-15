package de.gobics.marvis.utils.matrix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class LabelLookup {

	private final HashMap<Object, HashSet<Integer>> lookup;
	
	public LabelLookup(Object[] labels){
		lookup = new HashMap<Object, HashSet<Integer>>();
		for(int idx = 0; idx < labels.length;idx++){
			Object l = labels[idx];
			if( ! lookup.containsKey(l)){
				lookup.put(l, new HashSet<Integer>());
			}
			lookup.get(l).add(idx);
		}
	}

	public LabelLookup(HashMap<Object, HashSet<Integer>> lookup) {
		this.lookup = lookup;
	}

	public HashSet<Integer> getQuick(Object label) {
		return lookup.get(label);
	}

	public int getFirst(Object label) {
		Set<Integer> set = getQuick(label);
		if (set == null || set.isEmpty()) {
			return -1;
		}

		Iterator<Integer> iter = set.iterator();
		if (iter.hasNext()) {
			return iter.next();
		}
		return -1;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName()).append(" [");
		for (Object key : lookup.keySet()) {
			sb.append(key).append("=>").append(lookup.get(key));
		}
		return sb.toString();
	}
}
