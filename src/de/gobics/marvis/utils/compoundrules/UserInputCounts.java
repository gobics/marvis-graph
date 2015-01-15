/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.compoundrules;

import java.util.TreeMap;

/**
 *
 * @author manuel
 */
public class UserInputCounts extends Rule {

	private TreeMap<String, int[]> bounds = new TreeMap<String, int[]>();
	private int[] current_low = new int[0];
	private int[] current_high = new int[0];

	@Override
	public String getName() {
		return "User defined compound bounds";
	}

	@Override
	public String getDescription() {
		return "Bound compound counts by user input";
	}

	public void setLowerBound(String element, int min) {
		setBound(element, min, Integer.MAX_VALUE);
	}

	public void setUpperBound(String element, int max) {
		setBound(element, 0, max);
	}

	public void setBound(String element, int min, int max) {
		if (min < 0) {
			min = 0;
		}
		max = Math.abs(max);

		bounds.put(element, new int[]{min, max});
	}

	public int[] getLowerBoundFormula(String[] elements) {
		int[] formula = new int[elements.length];
		for (int i = 0; i < elements.length; i++) {
			int[] bs = bounds.get(elements[i]);
			if (bs != null && bs.length == 2) {
				formula[i] = bs[0];
			}
		}
		return formula;

	}

	@Override
	public int check(int[] formula) {
		for (int i = 0; i < formula.length; i++) {
			if (formula[i] < current_low[i]) {
				return FAIL;
			} else if (formula[i] > current_high[i]) {
				return UPPER_BOUND;
			}
		}
		return OK;
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName() + "[";

		for (String e : bounds.keySet()) {
			int[] bs = bounds.get(e);
			s += bs[0] + "<=" + e + "<=" + bs[1] + ";";
		}

		return s.substring(0, s.length() - 1) + "]";
	}

	@Override
	public void prepare(double desiredMass, String[] elements) {
		current_low = new int[elements.length];
		current_high = new int[elements.length];

		for (int i = 0; i < elements.length; i++) {
			int[] bs = bounds.get(elements[i]);
			if (bs != null && bs.length == 2) {
				current_low[i] = bs[0];
				current_high[i] = bs[1];
			} else {
				current_low[i] = 0;
				current_high[i] = Integer.MAX_VALUE;
			}
		}

	}

	public void clear() {
		bounds = new TreeMap<String, int[]>();
		current_low = new int[0];
		current_high = new int[0];
	}

	public boolean equals(UserInputCounts other) {
		if (bounds.size() != other.bounds.size()) {
			return false;
		}
		for (String e : bounds.keySet()) {
			if (!other.bounds.containsKey(e)) {
				return false;
			}
			if (bounds.get(e)[0] != other.bounds.get(e)[0]
					|| bounds.get(e)[1] != other.bounds.get(e)[1]) {
				return false;
			}
		}

		return true;
	}
}
