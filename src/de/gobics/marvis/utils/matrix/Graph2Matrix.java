package de.gobics.marvis.utils.matrix;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.collections15.Transformer;
import org.jblas.DoubleMatrix;

/**
 * The awesome new Graph2Matrix
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class Graph2Matrix {

	public static <V, E> DoubleMatrix transform(Hypergraph<V, E> network, List<V> vertices) {
		return transform(network, null, vertices);
	}

	public static <V, E> DoubleMatrix transform(Hypergraph<V, E> network, Transformer<E, Number> edge_weight_calculator) {
		return transform(network, edge_weight_calculator, new ArrayList<>(network.getVertices()));
	}

	/**
	 * This transforms a network into an adjacency matrix. The second parameter
	 * is a list of vertices that will be represented in the matrix. In general
	 * this should be a list of all vertices in the network. Nonetheless, you
	 * can supply a subset of the vertices if you only want to focus on them.
	 *
	 * This list is needed, to let the caller know of the objects that are
	 * represented by the matrix. The rows and columns will be in the order of
	 * vertices in the given list.
	 *
	 * @param <V> the vertices of the graph
	 * @param <E> the edges of the graph
	 * @param network the network to transform
	 * @param vertices a list of vertices represented in the matrix
	 * @return the adjacency matrix
	 */
	public static <V, E> DoubleMatrix transform(Hypergraph<V, E> network, Transformer<E, Number> edge_weight_calculator, List<V> vertices) {
		DoubleMatrix A = new DoubleMatrix(vertices.size(), vertices.size());

		// Insert edge weight for connections
		for (E edge : network.getEdges()) {
			if (network.getEdgeType(edge).equals(EdgeType.DIRECTED)) {
				V start = network.getSource(edge);
				V end = network.getDest(edge);

				int start_idx = vertices.indexOf(start);
				int end_idx = vertices.indexOf(end);

				if (start_idx >= 0 && end_idx >= 0) {
					A.put(start_idx, end_idx, edge_weight_calculator != null ? edge_weight_calculator.transform(edge).doubleValue() : 1d);
				}
			}
			else {
				Collection<V> vs = network.getIncidentVertices(edge);
				for (V v1 : vs) {
					for (V v2 : vs) {
						int start_idx = vertices.indexOf(v1);
						int end_idx = vertices.indexOf(v2);
						if (start_idx >= 0 && end_idx >= 0) {
							A.put(start_idx, end_idx, edge_weight_calculator != null ? edge_weight_calculator.transform(edge).doubleValue() : 1d);
						}
					}
				}

			}
		}
		return A;
	}
}
