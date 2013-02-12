package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.graphview.ReactionGraph;
import de.gobics.marvis.utils.RandomWalkWithRestart;
import de.gobics.marvis.utils.matrix.DenseDoubleMatrix1D;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Calculates networks based on a version of the Random-Walk-with-Restart
 * algorithm.
 *
 * @author manuel
 */
public class CalculateNetworksRWR extends AbstractNetworkCalculation {

	private static final Logger logger = Logger.getLogger(CalculateNetworksRWR.class.
			getName());
	
	/**
	 * A reaction based view on the metabolic network. This view depends on the
	 * co-factor setting.
	 */
	private ReactionGraph reactions_view;
	/**
	 * Basic restart probability.
	 */
	private double restart_probability = 0.8;
	private boolean use_input_weights;

	public CalculateNetworksRWR(MetabolicNetwork network) {
		super(network);
		setCofactorThreshold(-1);
	}

	@Override
	public CalculateNetworksRWR like(MetabolicNetwork network) {
		CalculateNetworksRWR clone = new CalculateNetworksRWR(network);
		clone.setCofactorThreshold(reactions_view.getCofactorThreshold());
		clone.setRestartProbability(restart_probability);
		clone.useInputWeights(use_input_weights);
		return clone;
	}

	public void setRestartProbability(double probability) {
		this.restart_probability = probability;
	}

	public void setCofactorThreshold(int cofactor_threshold) {
		reactions_view = new ReactionGraph(getRootNetwork(), false, cofactor_threshold);
	}

	@Override
	protected MetabolicNetwork[] doTask() throws Exception {
		return calculateNetworks(getRootNetwork());
	}

	public MetabolicNetwork[] calculateNetworks(MetabolicNetwork root) throws Exception {
		// Find explainable nodes
		setTaskTitle("Calculate sub-networks with Random Walk");
		setTaskDescription("Search start nodes");
		setProgressMax(5);
		incrementProgress();
		logger.finer("Calculating reaction start probabilities");
		Map<Reaction, Double> initial = calculateInitialScores(true);

		setTaskDescription("Performing random walk for reaction scoring");
		incrementProgress();
		logger.log(Level.FINER, "Perfoming random walk process with {0} initial nodes and {1} edges", new Object[]{initial.size(), reactions_view.getEdgeCount()});
		RandomWalkWithRestart process = new RandomWalkWithRestart(reactions_view, restart_probability, 0.0000001);
		DenseDoubleMatrix1D result = process.walk(initial);
		logger.log(Level.FINER, "Random-walk finished with {0} reactions with non-zero probability", result.cardinality());
		System.gc();

		// Build list of reactions above the threshold
		incrementProgress();
		LinkedList<Reaction> reactions_for_networks = new LinkedList<>();
		for (int i = 0; i < result.size(); i++) {
			if (result.getQuick(i) >= (1 - restart_probability)) {
				reactions_for_networks.add((Reaction) result.getLabel(i));
			}
		}
		logger.log(Level.FINER, "Found {0} reactions for the subnetworks", reactions_for_networks.size());

		setTaskDescription("Generating new metabolic sub-networks");
		incrementProgress();
		Collection<MetabolicNetwork> subs = getSubnetworks(reactions_for_networks);
		logger.log(Level.FINER, "Found {0} subnetworks", subs.size());
		incrementProgress();

		return subs.toArray(new MetabolicNetwork[subs.size()]);
	}

	/**
	 * Builds networks from the reactions.
	 *
	 * @param reactions
	 * @return
	 */
	private Collection<MetabolicNetwork> getSubnetworks(LinkedList<Reaction> reactions) {
		LinkedList<MetabolicNetwork> subs = new LinkedList<>();

		TreeSet<Reaction> visited = new TreeSet<>();
		while (!reactions.isEmpty()) {
			TreeSet<Reaction> reactions_for_subnet = new TreeSet<>();
			LinkedList<Reaction> to_visit = new LinkedList<>();
			to_visit.add(reactions.poll());

			while (!to_visit.isEmpty()) {
				Reaction cur = to_visit.poll();
				reactions_for_subnet.add(cur);

				for (GraphObject neighbor_object : reactions_view.getNeighbors(cur)) {
					Reaction neighbor_reaction = (Reaction) neighbor_object;
					if (reactions.contains(neighbor_reaction) && !visited.contains(neighbor_reaction)) {
						to_visit.add(neighbor_reaction);
					}
					visited.add(neighbor_reaction);
				}
			}

			if (reactions_for_subnet.size() > 1) {
				MetabolicNetwork sub = generate_network(reactions_for_subnet);
				subs.add(sub);
			}

			for (Reaction r : reactions_for_subnet) {
				reactions.remove(r);
			}
		}

		return subs;
	}

	public Map<Reaction, Double> calculateInitialScores(boolean normalize_to_1) {
		Map<Reaction, Double> reaction_scores = new TreeMap<>();

		for (Marker marker : getRootNetwork().getMarkers()) {
			LinkedList<Compound> compounds = getRootNetwork().getAnnotations(marker);
			double score_per_compound = getInitialScore(marker) / compounds.size();


			for (Compound compound : compounds) {
				LinkedList<Reaction> reactions = new LinkedList<>();
				reactions.addAll(getRootNetwork().getSubstrateToReaction(compound));
				reactions.addAll(getRootNetwork().getProductToReaction(compound));

				double score_per_reaction = score_per_compound / reactions.size();
				for (Reaction r : reactions) {

					if (!reaction_scores.containsKey(r)) {
						reaction_scores.put(r, score_per_reaction);
					}
					else {
						reaction_scores.put(r, reaction_scores.get(r) + score_per_reaction);
					}
				}
			}
		}

		for (Transcript transcript : getRootNetwork().getTranscripts()) {
			LinkedList<Gene> genes = getRootNetwork().getGenes(transcript);
			double score_for_gene = getInitialScore(transcript) / genes.size();

			for (Gene gene : genes) {
				LinkedList<Enzyme> enzymes = getRootNetwork().getEncodedEnzymes(gene);
				double score_for_enzyme = score_for_gene / enzymes.size();

				for (Enzyme enzyme : enzymes) {
					LinkedList<Reaction> reactions = getRootNetwork().getReactions(enzyme);
					double addscore = score_for_enzyme / reactions.size();
					for (Reaction r : reactions) {

						if (!reaction_scores.containsKey(r)) {
							reaction_scores.put(r, addscore);
						}
						else {
							reaction_scores.put(r, reaction_scores.get(r) + addscore);
						}
					}
				}
			}
		}

		// set maximum value to 1
		if (normalize_to_1) {
			Set<Reaction> reacs = reaction_scores.keySet();
			for (Reaction r : reacs) {
				reaction_scores.put(r, Math.min(1, reaction_scores.get(r)));
			}
		}

		return reaction_scores;
	}

	public void useInputWeights(boolean use_input_weights) {
		this.use_input_weights = use_input_weights;
	}

	private double getInitialScore(InputObject io) {
		return use_input_weights ? io.getWeight() : 1;
	}
}