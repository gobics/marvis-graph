package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.Enzyme;
import de.gobics.marvis.graph.Gene;
import de.gobics.marvis.graph.Marker;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Reaction;
import de.gobics.marvis.graph.Transcript;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author manuel
 */
public class NetworkSorterSumOfWeights extends AbstractGraphScore {

	private Map<Reaction, Double> scores;

	public NetworkSorterSumOfWeights(MetabolicNetwork p) {
		super(p);
	}

	public NetworkSorterSumOfWeights() {
	}

	private void calculateWeights() {
		MetabolicNetwork p = getParent();
		scores = new TreeMap<>();
		if (p == null) {
			return;
		}
		for (Marker marker : p.getMarkers()) {
			LinkedList<Compound> compounds = p.getAnnotations(marker);

			for (Compound compound : compounds) {
				Set<Reaction> reactions = p.getReactions(compound);

				double addscore = (1d / compounds.size()) / reactions.size();
				for (Reaction r : reactions) {
					if (!scores.containsKey(r)) {
						scores.put(r, addscore);
					}
					else {
						scores.put(r, scores.get(r) + addscore);
					}
				}
			}
		}

		for (Transcript transcript : p.getTranscripts()) {
			LinkedList<Gene> genes = p.getGenes(transcript);

			for (Gene gene : genes) {
				LinkedList<Enzyme> enzymes = p.getEncodedEnzymes(gene);
				for (Enzyme enzyme : enzymes) {
					LinkedList<Reaction> reactions = p.getReactions(enzyme);
					double addscore = ((1d / genes.size()) / enzymes.size()) / reactions.size();
					for (Reaction r : reactions) {

						if (!scores.containsKey(r)) {
							scores.put(r, addscore);
						}
						else {
							scores.put(r, scores.get(r) + addscore);
						}
					}
				}
			}
		}

		// set maximum value to 1
		/*	Set<Reaction> reacs = scores.keySet();
		 for (Reaction r : reacs) {
		 scores.put(r, Math.min(1, scores.get(r)));
		 }
		 */
	}

	@Override
	public void setParent(MetabolicNetwork p) {
		super.setParent(p);
		calculateWeights();
	}

	@Override
	public Double calculateScore(MetabolicNetwork graph) {
		if (scores == null || scores.isEmpty()) {
			calculateWeights();
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
