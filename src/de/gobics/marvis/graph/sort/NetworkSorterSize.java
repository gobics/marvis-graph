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
public class NetworkSorterSize extends AbstractGraphScore {

	public NetworkSorterSize(MetabolicNetwork p) {
		super(p);
	}

	public NetworkSorterSize() {
		super();
	}

	@Override
	public Integer calculateScore(MetabolicNetwork graph) {
		return graph.getReactions().size();
	}

	@Override
	public String getName() {
		return "Size";
	}

	@Override
	public String getDescription() {
		return "Sort by number of reactions";
		
	}

	@Override
	public AbstractGraphScore like(MetabolicNetwork new_parent) {
		return new NetworkSorterSize(new_parent);
	}


}
