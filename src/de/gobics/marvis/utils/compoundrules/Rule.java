/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.compoundrules;

/**
 *
 * @author manuel
 */
public abstract class Rule implements Comparable<Rule> {
	public static final int OK = 0;
	public static final int FAIL = 1;
	public static final int UPPER_BOUND = 2;

	public abstract String getName();
	public abstract String getDescription();
	public abstract int check(int[] formula);

	@Override
	public int compareTo(Rule other){
		return this.getClass().toString().compareTo( other.getClass().toString() );
	}

	public boolean equals(Rule other){
		return this.getClass().equals( other.getClass() );
	}

	public void prepare(double desiredMass, String[] elements) {
		// Ignore
	}
}
