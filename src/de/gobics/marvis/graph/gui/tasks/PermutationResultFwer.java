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
public class PermutationResultFwer {

	public final MetabolicNetwork network;
	public final int errors;
	public final double fwer;
	public final double score;

	public PermutationResultFwer(MetabolicNetwork n, double score, int errors, int NUM_PERMUTES) {
		this.network = n;
		this.score = score;
		this.errors = errors;
		this.fwer = ((double) errors) / ((double) NUM_PERMUTES);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PermutationResultFwer other = (PermutationResultFwer) obj;
		if (!Objects.equals(this.network, other.network)) {
			return false;
		}
		if (this.errors != other.errors) {
			return false;
		}
		if (this.fwer != other.fwer) {
			return false;
		}
		if (Double.doubleToLongBits(this.score) != Double.doubleToLongBits(other.score)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + Objects.hashCode(this.network);
		hash = 97 * hash + this.errors;
		hash = 97 * hash + (int) (Double.doubleToLongBits(this.fwer) ^ (Double.doubleToLongBits(this.fwer) >>> 32));
		hash = 97 * hash + (int) (Double.doubleToLongBits(this.score) ^ (Double.doubleToLongBits(this.score) >>> 32));
		return hash;
	}
}