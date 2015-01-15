/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.compoundrules;

/**
 *
 * @author manuel
 */
public class MinimumCount extends Rule {

	private static final String[] elements = new String[]{"C", "H", "O"};
	private static final int[] min_500 = new int[]{0, 0, 0};
	private static final int[] min_1000 = new int[]{6, 0, 0};
	private static final int[] min_2000 = new int[]{31, 32, 1};
	private static final int[] min_3000 = new int[]{96, 107, 26};
	private int[] current_min = new int[0];

	@Override
	public int check(int[] formula) {
		for (int i = 0; i < formula.length; i++) {
			if (formula[i] < current_min[i]) {
				return FAIL;
			}
		}
		return OK;
	}

	@Override
	public String getName() {
		return "Min. element count (ML 1)";
	}

	@Override
	public String getDescription() {
		return "Force to use a minimum count for C, O, H";
	}

	@Override
	public void prepare(double desiredMass, String[] els) {
		current_min = new int[els.length];
		for (int i = 0; i < els.length; i++) {
			int idx = indexOf(els[i]);
			if (idx >= 0) {
				if (desiredMass < 500) {
					current_min[i] = min_500[idx];
				} else if (desiredMass < 1000) {
					current_min[i] = min_1000[idx];
				} else if (desiredMass < 2000) {
					current_min[i] = min_2000[idx];
				} else if (desiredMass < 3000) {
					current_min[i] = min_3000[idx];
				} else {
					current_min[i] = 0;
				}
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
