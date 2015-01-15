/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.compoundrules;

import de.gobics.marvis.utils.Formula;

/**
 *
 * @author manuel
 */
public class LewisAndSenior extends Rule {

	private int[] current_valences = new int[0];
	private int max_valence = 0;

	@Override
	public String getName() {
		return "Lewis and Senior (Fiehn 2)";
	}

	@Override
	public String getDescription() {
		return "Returns true if the number of valences is even";
	}

	@Override
	public int check(int[] formula) {
		int last_sum = 0;
		int last_atom_count = 0;
		int atoms_with_odd_valence = 0;
		
		for (int i = 0; i < current_valences.length; i++) {
			last_sum += current_valences[i] * formula[i];
			last_atom_count += formula[i];
			if (current_valences[i] % 2 != 0) {
				atoms_with_odd_valence += formula[i];
			}
		}
		
		return ( last_sum % 2 == 0 || atoms_with_odd_valence % 2 == 0) && last_sum >= 2 * max_valence
				&&  last_sum >= 2 * last_atom_count - 1 ? OK : FAIL;
	}


	@Override
	public void prepare(double d, String[] els) {
		current_valences = new int[els.length];

		for (int i = 0; i < current_valences.length; i++) {
			current_valences[i] = Formula.getValence(els[i]);
			if (current_valences[i] > max_valence) {
				max_valence = current_valences[i];
			}
		}

	}
}
