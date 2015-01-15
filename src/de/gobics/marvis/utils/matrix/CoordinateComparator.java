/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.matrix;

import java.util.Comparator;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
class CoordinateComparator implements Comparator<int[]> {

	public int compare(int[] t, int[] t1) {
		if (t.length < t1.length) {
			return -1;
		}
		if (t.length > t1.length) {
			return 1;
		}

		for (int i = 0; i < t.length; i++) {
			if (t[i] < t1[i]) {
				return -1;
			}
			if (t[i] > t1[i]) {
				return 1;
			}
		}

		return 0;
	}
}
