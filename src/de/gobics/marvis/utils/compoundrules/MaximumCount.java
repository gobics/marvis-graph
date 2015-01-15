/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.compoundrules;

/**
 *
 * @author manuel
 */
public class MaximumCount extends Rule {

	private static final String[] elements = new String[]{"C", "H", "N", "O", "P", "S", "F", "Cl", "Br", "Si"};
	private static final int[] max_500 = new int[]{39, 72, 20, 20, 9, 10, 16, 10, 4, 8};
	private static final int[] max_1000 = new int[]{78, 126, 25, 27, 9, 14, 34, 12, 8, 14};
	private static final int[] max_2000 = new int[]{156, 236, 32, 63, 9, 14, 48, 12, 10, 15};
	private static final int[] max_3000 = new int[]{162, 208, 48, 78, 6, 9, 16, 11, 25};
	private static final int max_for_unkown_elements = 10;

	private int[] current_max = new int[0];

	@Override
	public int check(int[] formula) {
		for( int i = 0 ; i < formula.length ; i++ )
			if(formula[i] > current_max[i] )
				return UPPER_BOUND;
		return OK;
	}

	@Override
	public String getName() {
		return "Max. element count (Fiehn 1)";
	}

	@Override
	public String getDescription() {
		return "Use an upper bound for element counts defined by heuristic counting";
	}

	@Override
	public void prepare(double desiredMass, String[] els) {
		current_max = new int[els.length];
		for (int i = 0; i < els.length; i++) {
			int idx = indexOf(els[i]);
			if (idx >= 0) {
				if (desiredMass < 500) {
					current_max[i] = max_500[idx];
				}
				else if (desiredMass < 1000) {
					current_max[i] = max_1000[idx];
				}
				else if (desiredMass < 2000) {
					current_max[i] = max_2000[idx];
				}
				else if (desiredMass < 3000) {
					current_max[i] = max_3000[idx];
				}
				else {
					current_max[i] = Integer.MAX_VALUE;
				}
			}
			else {
				// We have no upper limit
				current_max[i] = max_for_unkown_elements;
			}
		}
	}

	private int indexOf(String e) {
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].equals(e)) {
				return i;
			}
		}
		return -1;
	}
}
