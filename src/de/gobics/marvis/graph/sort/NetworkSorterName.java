package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.MetabolicNetwork;

/**
 * Simple returns the names of the graphs as score. Using this algorithm the 
 * sub-networks are simple sorted by their name.
 *
 * @author manuel
 */
public class NetworkSorterName extends AbstractGraphScore {

	public NetworkSorterName(MetabolicNetwork p) {
		super(p);
	}
	
	public NetworkSorterName() {
		super();
	}
	
	

	@Override
	public Comparable<String> calculateScore(MetabolicNetwork graph) {
		return graph.getName() != null ? graph.getName().toLowerCase() : "Unkown";
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
