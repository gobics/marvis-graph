/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.MetabolicNetwork;

/**
 *
 * @author manuel
 */
public class NetworkSorterName extends AbstractGraphSort {

	public NetworkSorterName(MetabolicNetwork p) {
		super(p);
	}

	public NetworkSorterName() {
		super();
	}

	@Override
	public Comparable<String> calculateScore(MetabolicNetwork graph) {
		return graph.getName();
	}

	@Override
	public String getName() {
		return "Network name";
	}

	@Override
	public String getDescription() {
		return "Sort by name";
	}
}
