/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.MetabolicNetwork;
import java.util.Objects;

/**
 *
 * @author manuel
 */
public class PermutationTestResult {

	public final MetabolicNetwork network;
	public final double fdr;
	public final double fwer;
	public final int num_permutations;
	public final Comparable score;

	public PermutationTestResult(MetabolicNetwork n, Comparable score, int fdr_errors, int fwer_errors, int NUM_PERMUTES) {
		this.network = n;
		this.score = score;
		this.num_permutations = NUM_PERMUTES;
		this.fdr = ((double) fdr_errors) / ((double) NUM_PERMUTES);
		this.fwer = ((double) fwer_errors) / ((double) NUM_PERMUTES);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + Objects.hashCode(this.network);
		hash = 97 * hash + (int) (Double.doubleToLongBits(this.fdr) ^ (Double.doubleToLongBits(this.fdr) >>> 32));
		hash = 97 * hash + (int) (Double.doubleToLongBits(this.fwer) ^ (Double.doubleToLongBits(this.fwer) >>> 32));
		hash = 97 * hash + this.num_permutations;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PermutationTestResult other = (PermutationTestResult) obj;
		if (!Objects.equals(this.network, other.network)) {
			return false;
		}
		if (Double.doubleToLongBits(this.fdr) != Double.doubleToLongBits(other.fdr)) {
			return false;
		}
		if (Double.doubleToLongBits(this.fwer) != Double.doubleToLongBits(other.fwer)) {
			return false;
		}
		if (this.num_permutations != other.num_permutations) {
			return false;
		}
		if (!Objects.equals(this.score, other.score)) {
			return false;
		}
		return true;
	}


}