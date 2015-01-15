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
class MoleculeComparator implements Comparator<Molecule> {

	@Override
	public int compare(Molecule t, Molecule t1) {
		return t.getId().compareTo(t1.getId());
	}

}
