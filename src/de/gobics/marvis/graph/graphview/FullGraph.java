package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.*;
import java.util.Collection;

/**
 * The full graph is basically a wrapper for the {@link MetabolicNetwork}. It
 * simply returns the {@link Relation}s and {@link GraphObject}s that exist in 
 * the parental network.
 *
 * @author manuel
 */
public class FullGraph extends AbstractGraph {


	public FullGraph(MetabolicNetwork basis_network) {
		super(basis_network);
	}

	@Override
	public Collection<Relation> getEdges() {
		return getParent().getRelations();
	}

	@Override
	public Collection<GraphObject> getVertices() {
		return getParent().getAllObjects();
	}

	
	@Override
	public Collection<Relation> getIncidentEdges(GraphObject v) {
		return getParent().getRelations(v);
	}
}
