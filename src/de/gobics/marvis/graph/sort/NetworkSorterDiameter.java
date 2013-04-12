package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Reaction;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.graphview.ReactionGraph;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
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

	private int cofactor_threshold = 10;

	public NetworkSorterDiameter(MetabolicNetwork p) {
		super(p);
	}

	public NetworkSorterDiameter() {
		super();
	}

	@Override
	public Integer calculateScore(MetabolicNetwork graph) {
		ReactionGraph reaction_graph = new ReactionGraph(graph, false, cofactor_threshold);

		DijkstraDistance<GraphObject, Relation> distance = new DijkstraDistance<>(reaction_graph);
		int max_length = 0;

		Reaction start = null, end = null;

		Collection<GraphObject> reactions = reaction_graph.getVertices();

		for (GraphObject r : reactions) {
			// Calculate the distance
			Map<GraphObject, Number> map = distance.getDistanceMap(r, reactions);
//			System.out.println("Starting from "+r);

			// Find longest path in results
			for (GraphObject go : map.keySet()) {
	//			System.out.println("\t"+go+" in "+map.get(go).intValue());
				if (max_length < map.get(go).intValue()) {
					max_length = map.get(go).intValue();
					start = (Reaction) r;
					end = (Reaction) go;
				}
			}
		}

		DijkstraShortestPath<GraphObject, Relation> djisktra = new DijkstraShortestPath<>(reaction_graph);

//		System.out.println("Longest path from " + start + " to " + end + ": " + max_length);
//		System.out.println(djisktra.getPath(start, end));

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
		NetworkSorterDiameter clone = new NetworkSorterDiameter(new_parent);
		clone.setCofactorThreshold(cofactor_threshold);
		return clone;
	}

	public void setCofactorThreshold(int cofactorThreshold) {
		this.cofactor_threshold = Math.abs(cofactorThreshold);
	}
}
