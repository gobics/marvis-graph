package de.gobics.marvis.utils;

import de.gobics.marvis.utils.matrix.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.collections15.Transformer;
import org.jblas.DoubleMatrix;

/**
 *
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class RandomWalkWithRestartDense<V, E> extends RandomWalkWithRestart<V, E> {

	private static final Logger logger = Logger.getLogger(RandomWalkWithRestartDense.class.
			getName());
	private ArrayList<V> vertices;

	public RandomWalkWithRestartDense(Graph<V, E> network) {
		this(network, 0.9, 0.000001);
	}

	public RandomWalkWithRestartDense(Graph<V, E> network, double restart_probability, double convergence_limit) {
		this(network, null, restart_probability, convergence_limit);
	}

	public RandomWalkWithRestartDense(Graph<V, E> network, Transformer<E, Number> edge_weight_transformer, double restart_probability, double convergence_limit) {
		super(network, edge_weight_transformer, restart_probability, convergence_limit);

		vertices = new ArrayList<>(network.getVertexCount());
		for (V v : network.getVertices()) {
			if (network.getNeighborCount(v) > 0) {
				vertices.add(v);
			}
		}
		vertices.trimToSize();
	}

	public double[] walk(Map<V, Double> initial_nodes, List<double[]> steps) {
		// Get normalized adjacency matrix
		DoubleMatrix adjacency = getAdjacencyMatrix(vertices);
		//logger.finer("Initial adjacency matrix: " + adjacency);

		// Build the restart vector V
		DoubleMatrix initial = new DoubleMatrix(vertices.size(), 1);

		int counter = 0;
		for (V i : initial_nodes.keySet()) {
			int idx = vertices.indexOf(i);
			if (idx >= 0) {
				initial.put(idx, initial_nodes.get(i));
				counter++;
			}
		}
		//logger.finer("Initial score vector: " + initial.toString());

		// Initialize vector of score as initial scores
		DoubleMatrix scores = initial.dup();

		// Scale the initial scores vector with the restart probability (otherwise 
		// this needs to be done in every step of the walk!
		initial.muli(getRestartProbability());

		// Transpose the adjacency matrix for the same reason
		adjacency = adjacency.transpose();

		int step = 0;
		//logger.fine("Scores at step " + (step++) + ": " + scores.toString());
		double difference = getConvergenceThreshold()
				+ 1;

		while (difference > getConvergenceThreshold() || step > getMaxIterations()) {
			step++;
			//System.out.println(step + " scores: " + scores);
			if (steps != null) {
				steps.add(scores.data);
			}

			// Transpose score-vector because it is smaller
			DoubleMatrix part1 = adjacency.mmul(scores);
			//System.out.println(step + " part1.1: " + part1);
			part1.muli(1 - getRestartProbability());//algebra.mult(adjacency, scores);
			//System.out.println(step + " part1.2: " + part1);
			//logger.finer("Part1 : " + part1.toString());
			//logger.finer("Part2 : " + part2.toString());
			DoubleMatrix new_scores = part1.add(initial);

			difference = difference(scores.data, new_scores.data);
			scores = new_scores;
			
			//System.out.println(step + " scores new: " + scores);
			//System.out.println(step + " scores diff: " + difference);
			//logger.fine("Scores at step " + step + ": " + scores.toString());
			
		}

		return scores.data;
	}

	/**
	 * Returns the per-row-normalized adjacency matrix of the network under
	 * research.
	 *
	 * @return
	 */
	private DoubleMatrix getAdjacencyMatrix(List<V> vertices) {
		DoubleMatrix a = Graph2Matrix.transform(getNetwork(), getWeightTransformer(), vertices);
		DoubleMatrix cs = a.rowSums();

		// Ensure, that we do not divide by zero
		for (int idx = 0; idx < cs.length; idx++) {
			if (cs.data[idx] == 0) {
				cs.data[idx] = 1;
			}
		}
		a.diviColumnVector(cs);
		return a;
	}

	@Override
	public List<V> getVertices() {
		return vertices;
	}
}