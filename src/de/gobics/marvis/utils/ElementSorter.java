/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils;

import java.util.Comparator;

/**
 *
 * @author manuel
 */
public class ElementSorter implements Comparator<String> {

	public int compare(String o1, String o2) {
		if (o1.equals(o2)) {
			return 0;
		}

		if (o1.equals("C") || o2.equals("H")) {
			return -1;
		}

		if (o1.equals("H") || o2.equals("C")) {
			return 1;
		}

		return Double.compare(Formula.getMass(o2), Formula.getMass(o1));
	}
}
