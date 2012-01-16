package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.InputObject;

/**
 *
 * @author manuel
 */
public class pValueScore extends AbstractGraphSort {

	public pValueScore(MetabolicNetwork p) {
		super(p);
	}

	public pValueScore() {
		super();
	}

	@Override
	public Double calculateScore(MetabolicNetwork graph) {
		double score = 0d;
		double counter = 0;
		for (GraphObject o : graph.getAllObjects()) {
			if (o instanceof InputObject && ((InputObject) o).getWeight() > 0) {
				//System.out.println(score +" mal "+Math.log(((InputObject)o).getWeight()));
				//score *= Math.log(((InputObject)o).getWeight());
				score += ((InputObject) o).getWeight();
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
}
