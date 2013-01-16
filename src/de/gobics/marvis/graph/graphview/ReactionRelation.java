/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.Reaction;
import java.util.Objects;
import java.util.TreeSet;

/**
 *
 * @author manuel
 */
public class ReactionRelation implements Comparable<ReactionRelation> {

    public final Reaction start, end;
    public final TreeSet<Compound> molecules = new TreeSet<>();

    public ReactionRelation(Reaction r1, Reaction r2) {
	if (r1.compareTo(r2) < 0) {
	    start = r1;
	    end = r2;
	}
	else {
	    start = r2;
	    end = r1;
	}
    }

    public void addMolecule(Compound m) {
	molecules.add(m);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	for (Compound c : molecules) {
	    sb.append(" - ").append(c.getId());
	}
	return sb.substring(3);
    }

    @Override
    public int compareTo(ReactionRelation other) {
	int c = start.compareTo(other.start);
	if (c != 0) {
	    return c;
	}
	return end.compareTo(other.end);
    }

    @Override
    public int hashCode() {
	int hash = 3;
	hash = 83 * hash + Objects.hashCode(this.start);
	hash = 83 * hash + Objects.hashCode(this.end);
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
	final ReactionRelation other = (ReactionRelation) obj;
	if (!Objects.equals(this.start, other.start)) {
	    return false;
	}
	if (!Objects.equals(this.end, other.end)) {
	    return false;
	}
	return true;
    }
}
