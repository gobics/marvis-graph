/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.Reaction;
import java.util.TreeSet;

/**
 *
 * @author manuel
 */
public class PathwayGraph {

	private final TreeSet<Reaction> reactions = new TreeSet<Reaction>();

	public void addReaction(Reaction m) {
		reactions.add(m);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Reaction c : reactions) {
			sb.append(" - ").append(c.getId());
		}
		if( sb.length() > 3){
			return sb.substring(3);
		}
		return "";
	}
}
