package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.graphview.ReactionGraph;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import java.util.Collection;
import java.util.Map;

/**
 * Calculates the diameter of a sub-networks. For all pairs of reactions the
 * shortest paths between them are calculated. The diameter is the longest of
 * the found paths.
 *
 * For calculation of the shortest paths the Jung-Implementation of Dijkstras
 * algorithm is utilized.
 *
 * @author manuel &lt;manuel@gobics.de&gt;
 */
public class NetworkSorterDiameter extends AbstractGraphScore {

	public NetworkSorterDiameter(MetabolicNetwork p) {
		super(p);
	}

	public NetworkSorterDiameter() {
		super();
	}

	@Override
	public Integer calculateScore(MetabolicNetwork graph) {
		ReactionGraph reaction_graph = new ReactionGraph(graph, false, 25);

		DijkstraDistance<GraphObject, Relation> distance = new DijkstraDistance<>(reaction_graph);
		int max_length = 0;

		Collection<GraphObject> reactions = reaction_graph.getVertices();

		for (GraphObject r : reactions) {
			// Calculate the distance
			Map<GraphObject, Number> map = distance.getDistanceMap(r, reactions);

			// Find longest path in results
			for (GraphObject go : map.keySet()) {
				max_length= Math.max(max_length, map.get(go).intValue());
			}
		}

		return max_length;
	}

	@Override
	public String getName() {
		return "Diameter";
	}

	@Override
	public String getDescription() {
		return "Features networks with long reaction chains";
	}

	@Override
	public AbstractGraphScore like(MetabolicNetwork new_parent) {
		return new NetworkSorterDiameter(new_parent);
	}
}
