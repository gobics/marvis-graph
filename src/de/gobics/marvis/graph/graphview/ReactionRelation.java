/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.Compound;
import java.util.TreeSet;

/**
 *
 * @author manuel
 */
public class ReactionRelation {

	private final TreeSet<Compound> molecules = new TreeSet<Compound>();

	public void addMolecule(Compound m) {
		molecules.add(m);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Compound c : molecules) {
			sb.append(" - ").append(c.getId());
		}
		return sb.substring(3);
	}
}
