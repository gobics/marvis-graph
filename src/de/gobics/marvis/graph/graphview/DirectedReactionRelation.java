/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.Reaction;
import java.util.Objects;

/**
 *
 * @author manuel
 */
public class DirectedReactionRelation implements Comparable<DirectedReactionRelation> {

	private final Reaction from;
	private final Reaction to;
	private final Compound sharedCompound;

	public DirectedReactionRelation(Reaction from, Reaction to, Compound share) {
		this.from = from;
		this.to = to;
		this.sharedCompound = share;
	}

	public Reaction getFrom() {
		return from;
	}

	public Reaction getTo() {
		return to;
	}

	public Compound getSharedCompound() {
		return sharedCompound;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + Objects.hashCode(this.from);
		hash = 97 * hash + Objects.hashCode(this.to);
		hash = 97 * hash + Objects.hashCode(this.sharedCompound);
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
		final DirectedReactionRelation other = (DirectedReactionRelation) obj;
		if (!Objects.equals(this.from, other.from)) {
			return false;
		}
		if (!Objects.equals(this.to, other.to)) {
			return false;
		}
		if (!Objects.equals(this.sharedCompound, other.sharedCompound)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(DirectedReactionRelation t) {
		int c = from.compareTo(t.from);
		if (c != 0) {
			return c;
		}
		c = to.compareTo(t.to);
		if (c != 0) {
			return c;
		}
		return sharedCompound.compareTo(t.sharedCompound);
	}
}
