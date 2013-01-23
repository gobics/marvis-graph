package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Reaction;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import java.util.Map;

/**
 *
 * @author manuel
 */
public class NetworkSorterSumOfWeights extends AbstractGraphScore {

	private Map<Reaction, Double> scores = null;

	public NetworkSorterSumOfWeights(MetabolicNetwork p) {
		super(p);
	}

	public NetworkSorterSumOfWeights() {
	}

	@Override
	public void setParent(MetabolicNetwork p) {
		super.setParent(p);
		scores = new CalculateNetworksRWR(p).calculateInitialScores(false);
	}

	@Override
	public Double calculateScore(MetabolicNetwork graph) {
		if (scores == null) {
			throw new RuntimeException("Can not calculate scores. Set parent first!");
		}
		double sum = 0;
		for (Reaction r : graph.getReactions()) {
			sum += scores.containsKey(r) ? scores.get(r) : 0;
		}
		return sum;
	}

	@Override
	public String getName() {
		return "Sum of weights";
	}

	@Override
	public String getDescription() {
		return "Sums up the scores for reactions delivered by markers and transcripts";
	}
}
