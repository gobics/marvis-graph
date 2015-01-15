package de.gobics.marvis.utils;

import edu.uci.ics.jung.graph.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections15.Transformer;

/**
 *
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class RandomWalkWithRestartSparse<V, E> extends RandomWalkWithRestart<V, E> {

	private static final Logger logger = Logger.getLogger(RandomWalkWithRestartSparse.class.
			getName());
	private final List<V> vertices;
	private final int[][] matrix_indizes;
	private final double[][] matrix_values;

	public RandomWalkWithRestartSparse(Graph<V, E> network) {
		this(network, 0.9, 0.000001);
	}

	public RandomWalkWithRestartSparse(Graph<V, E> network, double restart_probability, double convergence_limit) {
		this(network, null, restart_probability, convergence_limit);
	}

	public RandomWalkWithRestartSparse(Graph<V, E> network, Transformer<E, Number> edge_weight_transformer, double restart_probability, double convergence_limit) {
		super(network, edge_weight_transformer, restart_probability, convergence_limit);
		vertices = new LinkedList<>(network.getVertices());
		matrix_indizes = new int[vertices.size()][];
		matrix_values = new double[vertices.size()][];
	}

	@Override
	public List<V> getVertices() {
		return new ArrayList<>(vertices);
	}

	@Override
	public double[] walk(Map<V, Double> initial_nodes, List<double[]> steps) {
		//System.out.println("Walking");
		buildAdjacencyMap();
		//logger.finer("Initial adjacency matrix: " + adjacency);
		final double restart_probability = getRestartProbability();
		final double convergence_threshold = getConvergenceThreshold();

		// Build the restart vector
		double[] initial = new double[vertices.size()];
		for (V i : initial_nodes.keySet()) {
			int idx = vertices.indexOf(i);
			if (idx >= 0) {
				initial[idx] = initial_nodes.get(i);
			}
		}
		//logger.finer("Initial score vector: " + initial.toString());

		// Initialize vector of score as initial scores
		double[] scores = initial.clone();

		// Scale the initial scores vector with the restart probability (otherwise 
		// this needs to be done in every step of the walk!
		for (int i = 0; i < scores.length; i++) {
			initial[i] *= restart_probability;
		}

		int step = 0;
		//logger.fine("Scores at step " + (step++) + ": " + scores.toString());

		// Initialize difference for at least one step
		double difference = convergence_threshold + 1;

		while (difference > convergence_threshold || step > getMaxIterations()) {
			step++;
			//System.out.println(step + " scores: " + Arrays.toString(scores));
			if (steps != null) {
				steps.add(scores);
			}
			double[] new_scores = new double[scores.length];

			// TODO: Calculate the following in parallel
			for (V v : vertices) {
				int idx = vertices.indexOf(v);
				// calculate the score that comes into this node from other nodes
				double score = 0;
				int[] mis = matrix_indizes[idx];
				double[] mvs = matrix_values[idx];
				for (int i = 0; i < mis.length; i++) {
					score += scores[mis[i]] * mvs[i];
				}
				score *= (1 - restart_probability);

				new_scores[idx] = score + initial[idx]; // remark: initial already scaled
			}

			difference = difference(scores, new_scores);
			scores = new_scores;

			//System.out.println(step + " scores sum: " + ArrayUtils.sum(scores));
			//System.out.println(step + " scores diff: " + difference);
			//logger.fine("Scores at step " + step + ": " + scores.toString());
		}

		logger.log(Level.FINER, "System convergence is reached after {0} steps", step);

		return scores;
	}

	private void buildAdjacencyMap() {
		for (V v : vertices) {
			int idx = vertices.indexOf(v);

			LinkedList<Integer> mi = new LinkedList<>();
			LinkedList<Double> mv = new LinkedList<>();
			Collection<E> edges = getNetwork().getInEdges(v);

			// Calculate the adjacency factor for each edge
			for (E edge : edges) {
				V other = getNetwork().getOpposite(v, edge);
				mi.add(vertices.indexOf(other));
				
				// Calculate outgoing weight of predecessor
				double overall_weight = 0;
				// Calculate the sum of outgoing edge weights
				for (E edge2 : getNetwork().getOutEdges(other)) {
					overall_weight += getEdgeWeight(edge2);
				}
				
				// set weight of current edge
				mv.add(getEdgeWeight(edge) / overall_weight);
			}


			int[] mis = ArrayUtils.toNativeArray(mi.toArray(new Integer[mi.size()]));
			double[] mvs = ArrayUtils.toNativeArray(mv.toArray(new Double[mv.size()]));
			//System.out.println(v + ": " + Arrays.toString(mvs));
			matrix_indizes[idx] = mis;
			matrix_values[idx] = mvs;
		}
	}
}
