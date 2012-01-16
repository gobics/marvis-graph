/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.graphview.*;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import java.util.*;

/**
 *
 * @author manuel
 */
public class NetworkSorterLongestShortestPath extends AbstractGraphSort {

	public NetworkSorterLongestShortestPath(MetabolicNetwork p) {
		super(p);
	}

	public NetworkSorterLongestShortestPath() {
		super();
	}

	@Override
	public Integer calculateScore(MetabolicNetwork graph) {

		DijkstraDistance<GraphObject, ReactionRelation> distance = new DijkstraDistance<GraphObject, ReactionRelation>(new GraphViewReactions(graph));
		int max_length = 0;

		Collection<Reaction> reactions = graph.getReactions();
		Collection<GraphObject> reactions_graphobjects = new TreeSet<GraphObject>(reactions);

		for (Reaction r : reactions) {
			// Calculate the distance
			Map<GraphObject, Number> map = distance.getDistanceMap(r, reactions_graphobjects);
			
			// Find longest path in results
			for (GraphObject go : map.keySet()) {
				if (go instanceof Reaction && max_length < map.get(go).intValue()) {
					max_length = map.get(go).intValue();
				}
			}
		}

		return max_length;
	}

	@Override
	public String getName() {
		return "Longest shortest path";
	}

	@Override
	public String getDescription() {
		return "Features networks with long reaction chains";
	}
}
