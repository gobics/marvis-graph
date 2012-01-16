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
public class NetworkSorterRelativeSize extends AbstractGraphSort {

	public NetworkSorterRelativeSize(MetabolicNetwork p) {
		super(p);
	}

	public NetworkSorterRelativeSize() {
		super();
	}

	@Override
	public Double calculateScore(MetabolicNetwork graph) {
		return new Double(graph.getMarkers().size() + graph.getTranscripts().size())/graph.size();
	}

	@Override
	public String getName() {
		return "Relative size";
	}

	@Override
	public String getDescription() {
		return "Size devided by the number of markers and transcripts";
	}


}
