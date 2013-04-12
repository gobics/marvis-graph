package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.graphview.ReactionGraph;
import de.gobics.marvis.utils.RandomWalkWithRestart;
import de.gobics.marvis.utils.RandomWalkWithRestartDense;
import de.gobics.marvis.utils.RandomWalkWithRestartSparse;
import de.gobics.marvis.utils.matrix.DenseDoubleMatrix1D;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jblas.DoubleMatrix;

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
	 * Basic restart probability.
	 */
	private double restart_probability = 0.8;
	private double weight_threshold = 1 - restart_probability;
	private boolean use_input_weights;
	private boolean use_sparse_algorithm = true;

	public CalculateNetworksRWR(MetabolicNetwork network) {
		this(network, true);
	}

	public CalculateNetworksRWR(MetabolicNetwork network, boolean use_sparse_algorithm) {
		super(network);
		setCofactorThreshold(-1);
		setTaskTitle("Detect subnetworks with Random-Walk");
		this.use_sparse_algorithm = use_sparse_algorithm;
	}

	@Override
	public CalculateNetworksRWR like(MetabolicNetwork network) {
		CalculateNetworksRWR clone = new CalculateNetworksRWR(network);
		clone.setCofactorThreshold(getCofactorThreshold());
		clone.setRestartProbability(restart_probability);
		clone.setThreshold(weight_threshold);
		clone.useInputWeights(use_input_weights);
		return clone;
	}

	public void setRestartProbability(double probability) {
		this.restart_probability = probability;
	}

	public void setThreshold(double threshold) {
		this.weight_threshold = Math.abs(threshold);
	}

	public double getThreshold() {
		return weight_threshold;
	}

	@Override
	public MetabolicNetwork[] calculateNetworks() throws Exception {
		// Find explainable nodes
		setTaskTitle("Calculate sub-networks with Random Walk");
		setTaskDescription("Search start nodes");
		setProgressMax(5);
		incrementProgress();
		logger.finer("Calculating reaction start probabilities");
		Map<Reaction, Double> initial = calculateInitialScores(true);

		setTaskDescription("Performing random walk for reaction scoring");
		incrementProgress();
		RandomWalkWithRestart process = use_sparse_algorithm
				? new RandomWalkWithRestartSparse(getReactionView(), restart_probability, 0.0000001)
				: new RandomWalkWithRestartDense(getReactionView(), restart_probability, 0.0000001);
		List<GraphObject> vertices = process.getVertices();
		logger.log(Level.FINER, "Perfoming random walk process with {0} initial nodes of {2} and {1} edges on network: {3}", new Object[]{initial.size(), getReactionView().getEdgeCount(), vertices.size(), getRootNetwork().getName()});
		double[] result = process.walk(initial);
		// Build list of reactions above the threshold
		incrementProgress();
		//logger.finer("Filtering for threshold "+weight_threshold+": " + Arrays.toString(result));
		TreeSet<Reaction> reactions_for_networks = new TreeSet<>();
		for (int i = 0; i < result.length; i++) {
			if (result[i] >= weight_threshold) {
				reactions_for_networks.add((Reaction) vertices.get(i));
			}
			
		}
		logger.log(Level.FINER, "Found {0} reactions for the subnetworks", reactions_for_networks.size());


		setTaskDescription("Generating new metabolic sub-networks");
		incrementProgress();
		Collection<MetabolicNetwork> subs = getSubnetworks(new LinkedList(reactions_for_networks));
		logger.log(Level.FINER, "Found {0} subnetworks", subs.size());
		incrementProgress();

		return subs.toArray(new MetabolicNetwork[subs.size()]);
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
