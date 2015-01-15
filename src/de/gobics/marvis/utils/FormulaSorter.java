/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils;

import java.util.Comparator;
import java.util.TreeMap;

/**
 *
 * @author manuel
 */
public class FormulaSorter implements Comparator<Formula> {

	private ElementSorter sorter = new ElementSorter();

	@Override
	public int compare(Formula t1, Formula t2) {
		TreeMap<String, Integer> tm1 = t1.split();
		TreeMap<String, Integer> tm2 = t2.split();
		String[] e1 = tm1.keySet().toArray(new String[0]);
		String[] e2 = tm2.keySet().toArray(new String[0]);
		Integer[] c1 = tm1.values().toArray(new Integer[0]);
		Integer[] c2 = tm2.values().toArray(new Integer[0]);

		for (int i = 0; i < e1.length && i < e2.length; i++) {
			if (e1[i].equals(e2[i])) {
				if (c1[i] < c2[i]) {
					return -1;
				}
				else if (c1[i] > c2[i]) {
					return 1;
				}
			}
			else {
				return sorter.compare(e1[i], e2[i]);
			}
		}

		if (e1.length == e2.length) {
			return 0;
		}
		return e1.length < e2.length ? -1 : 1;
	}
}
