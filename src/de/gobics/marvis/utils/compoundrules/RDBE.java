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
public class RDBE extends Rule {
	private int[] current_valences = new int[0];

	@Override
	public String getName() {
		return "RDBE";
	}

	@Override
	public String getDescription() {
		return "Rings plus double bonds (Degree of unsaturation)";
	}

	@Override
	public int check(int[] formula) {
		int dba = 2;
		for(int i = 0; i < formula.length; i++){
			int v = current_valences[i];
			if( v == 4 )
				dba += 2 * formula[i];
			else if( v == 3)
				dba += formula[i];
			else if( v == 1)
				dba -= formula[i];
		}
		dba /= 2;


		return dba % 2 == 0 ? OK : FAIL;
	}

	@Override
	public void prepare(double d, String[] els) {
		current_valences = new int[els.length];

		for (int i = 0; i < current_valences.length; i++) {
			current_valences[i] = Formula.getValence(els[i]);
		}

	}
}
