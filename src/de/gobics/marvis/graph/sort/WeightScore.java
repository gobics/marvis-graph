package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.ExperimentalMarker;
import de.gobics.marvis.graph.MetabolicNetwork;

/**
 * Calculates the score of a sub-networks as mean weight of the metabolites.
 * @author manuel
 */
public class WeightScore extends AbstractGraphScore {

	public WeightScore(MetabolicNetwork p) {
		super(p);
	}

	public WeightScore() {
		super();
	}

	@Override
	public Double calculateScore(MetabolicNetwork graph) {
		double score = 0d;
		double counter = 0;
		for (GraphObject o : graph.getAllObjects()) {
			if (o instanceof ExperimentalMarker && ((ExperimentalMarker) o).getScore() >= 0) {
				//System.out.println(score +" mal "+Math.log(((InputObject)o).getWeight()));
				//score *= Math.log(((InputObject)o).getWeight());
				score += ((ExperimentalMarker) o).getScore();
				counter++;
			}
		}

		if (counter == 0) {
			return Double.NEGATIVE_INFINITY;
		}

		return score / counter;
	}

	@Override
	public String getName() {
		return "Weight";
	}

	@Override
	public String getDescription() {
		return "Sort by the given weight (e.g. from MarVis-Filter)";
	}

	@Override
	public AbstractGraphScore like(MetabolicNetwork new_parent) {
		return new WeightScore(new_parent);
	}
}
