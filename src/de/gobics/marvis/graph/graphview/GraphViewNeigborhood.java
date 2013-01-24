package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.MetabolicNetwork;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * A graphical representation of the network that can be customized by the user
 * through the GUI. Users can select specific classes and/or nodes to hide.
 *
 * @author manuel
 */
public class GraphViewNeigborhood extends GraphViewCustomizable {

	private static final Logger logger = Logger.getLogger(GraphViewNeigborhood.class.
			getName());
	/**
	 * A specific set of {@link GraphObject}s that are used as base classes.
	 */
	private final Set<GraphObject> base_objects = new TreeSet<>();
	/**
	 * A collection of vertices, that is reachable from the base objects in the
	 * set amount of steps.
	 */
	private final TreeSet<GraphObject> reachable_vertices = new TreeSet<>();

	/**
	 * Create a new customizable graphical view based on the given network.
	 *
	 * @param network
	 */
	public GraphViewNeigborhood(MetabolicNetwork network) {
		this(network, (GraphObject[]) null);
	}

	public GraphViewNeigborhood(MetabolicNetwork network, GraphObject base) {
		this(network, new GraphObject[]{base});
	}

	public GraphViewNeigborhood(MetabolicNetwork network, GraphObject[] base_objects) {
		super(network);

		if (base_objects != null && base_objects.length > 0) {
			this.base_objects.addAll(Arrays.asList(base_objects));
		}

		setMaximumDistance(1);
	}

	/**
	 * Sets the maximum distance that is allowed.
	 *
	 * @param maximal_distance
	 */
	public void setMaximumDistance(int maximal_distance) {
		FullGraph graph = new FullGraph(getMetabolicNetwork());
		reachable_vertices.clear();

		for (GraphObject base : base_objects) {
			breathSearch(graph, base, maximal_distance);
		}

		fireGraphChangeEvent();
	}

	@Override
	public boolean acceptVertex(GraphObject o) {
		return reachable_vertices.contains(o);
	}

	private void breathSearch(FullGraph graph, GraphObject start, int depth_to_go) {
		reachable_vertices.add(start);
		if (depth_to_go <= 0) {
			return;
		}
		for (GraphObject neighbor : graph.getNeighbors(start)) {
			if (!reachable_vertices.contains(neighbor) && super.acceptVertex(neighbor)) {
				breathSearch(graph, neighbor, depth_to_go - 1);
			}
		}
	}
}
