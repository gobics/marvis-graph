package de.gobics.marvis.utils;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections15.Transformer;

/**
 *
 * Abstract class to perform a random walk.
 *
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public abstract class RandomWalkWithRestart<V, E> {

	private final Graph<V, E> network;
	private double restart_probability;
	private double convergence_threshold;
	private int max_iterations = 100;
	private final Transformer<E, Number> edge_weight;

	public RandomWalkWithRestart(Graph<V, E> network) {
		this(network, 0.9, 0.000001);
	}

	public RandomWalkWithRestart(Graph<V, E> network, double restart_probability, double convergence_limit) {
		this(network, null, restart_probability, convergence_limit);
	}

	public RandomWalkWithRestart(Graph<V, E> network, Transformer<E, Number> edge_weight_transformer, double restart_probability, double convergence_limit) {
		this.network = network;
		this.edge_weight = edge_weight_transformer;
		setRestartProbability(restart_probability);
		setConvergenceThreshold(convergence_limit);
	}

	protected Graph<V, E> getNetwork() {
		return network;
	}

	/**
	 * Get the convergence threshold
	 *
	 * @return the threshold
	 */
	public double getConvergenceThreshold() {
		return convergence_threshold;
	}

	/**
	 * Set the convergence threshold.
	 *
	 * @param convergence_threshold double value greater than zero
	 */
	public final void setConvergenceThreshold(double convergence_threshold) {
		if (convergence_threshold <= 0) {
			throw new IllegalArgumentException("Convergence threshold must be a positive number without 0");
		}
		this.convergence_threshold = convergence_threshold;
	}

	/**
	 * Returns the restart probability.
	 *
	 * @return double between 0 and 1 (inclusive)
	 */
	public double getRestartProbability() {
		return restart_probability;
	}

	/**
	 * Set the restart probability.
	 *
	 * @param double between 0 and 1 (inclusive)
	 */
	public final void setRestartProbability(double restart_probability) {
		if (restart_probability < 0 || restart_probability > 1) {
			throw new IllegalArgumentException("Restart probability must be a positive number in [0;1]");
		}

		this.restart_probability = restart_probability;
	}

	public void setMaxIteration(int max) {
		this.max_iterations = Math.abs(max);
	}

	public int getMaxIterations() {
		return max_iterations;
	}

	public Transformer<E, Number> getWeightTransformer() {
		return edge_weight;
	}

	/**
	 * Returns the list of vertices that corresponds to the rows in the scores
	 * vectors.
	 *
	 * @return
	 */
	public abstract List<V> getVertices();

	/**
	 * Performs a random walk with the set of initial nodes.
	 *
	 * @param initial_nodes
	 * @return
	 */
	public double[] walk(Collection<V> initial_nodes) {
		return walk(initial_nodes, null);
	}

	public double[] walk(Collection<V> initial_nodes, List<double[]> steps) {
		Map<V, Double> map = new HashMap<>();
		for (V v : initial_nodes) {
			map.put(v, 1d);
		}
		return walk(map, steps);
	}

	public double[] walk(Map<V, Double> initial_nodes) {
		return walk(initial_nodes, null);
	}

	public abstract double[] walk(Map<V, Double> initial_nodes, List<double[]> steps);

	/**
	 * Calculate the difference between the both vectors as euklidean distance
	 * (squared differences).
	 *
	 * @param n first operant
	 * @param o second operant
	 * @return euklidean distance between both (>=0)
	 */
	protected double difference(double[] n, double[] o) {
		if (n.length != o.length) {
			throw new RuntimeException("Arrays not of equal length");
		}
		double diff = 0;

		for (int i = 0; i < n.length; i++) {
			diff += Math.pow(n[i] - o[i], 2);
		}
		return diff;
	}

	protected double getEdgeWeight(E edge) {
		if (edge_weight == null) {
			return 1d;
		}
		return edge_weight.transform(edge).doubleValue();
	}
}
