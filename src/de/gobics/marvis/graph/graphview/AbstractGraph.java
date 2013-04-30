package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author manuel
 */
public abstract class AbstractGraph implements GraphView<GraphObject, Relation> {

	/**
	 * A set of listeners, that will be notified, if the graphs structure has
	 * been changed somehow.
	 */
	private final HashSet<GraphViewListener> listener = new HashSet<>();
	/**
	 * The metabolic network that this graph depends on.
	 */
	private final MetabolicNetwork parent;

	public AbstractGraph(MetabolicNetwork basis_network) {
		if (basis_network == null) {
			throw new NullPointerException("Given base network is NULL");
		}
		this.parent = basis_network;
	}

	/**
	 * Returns the metabolic network that this view is based on.
	 *
	 * @return
	 */
	@Override
	public final MetabolicNetwork getMetabolicNetwork() {
		return parent;
	}

	/**
	 * The network that this view is based on might be a subnetwork. In that
	 * case, this method will return the absolute root network. If the current
	 * network is the root network it will return the current network. This also
	 * works for sub-sub-networks as the line of ancestors will be traversed to
	 * the ultimate root network.
	 *
	 * @return
	 */
	public final MetabolicNetwork getRootNetwork() {
		MetabolicNetwork n = getMetabolicNetwork();
		while (n.isSubnetwork()) {
			n = n.getParent();
		}
		return n;
	}

	/**
	 * Adds a listener to the graph view.
	 *
	 * @param l the listener to notify on changes
	 */
	@Override
	public void addGraphViewListener(GraphViewListener l) {
		listener.add(l);
	}

	/**
	 * Removes a given listener from the graph view.
	 *
	 * @param l
	 */
	@Override
	public void removeGraphViewListener(GraphViewListener l) {
		listener.remove(l);
	}

	/**
	 * Helper method that notifies all the listeners about a structural change in 
	 * the graph.
	 */
	protected void fireGraphChangeEvent(GraphViewListener.GraphChangeType type) {
		for (GraphViewListener l : listener) {
			l.graphChanged(this, type);
		}
	}

	@Override
	public final boolean isExplainable(GraphObject o) {
		return getRootNetwork().isExplainable(o);
	}

	public final boolean isExplainable(GraphObject o, int allowed_gaps) {
		return getRootNetwork().isExplainableWithGap(o, allowed_gaps);
	}

	@Override
	public final boolean containsVertex(GraphObject v) {
		return getMetabolicNetwork().containsObject(v);
	}

	@Override
	public final boolean containsEdge(Relation e) {
		return getMetabolicNetwork().containsRelation(e);
	}

	@Override
	public final int getEdgeCount() {
		return getEdges().size();
	}

	@Override
	public final int getVertexCount() {
		return getVertices().size();
	}

	@Override
	public final Collection<GraphObject> getNeighbors(GraphObject v) {
		TreeSet<GraphObject> objects = new TreeSet<>();
		for (Relation r : getIncidentEdges(v)) {
			objects.add(r.getOther(v));
		}
		return objects;
	}

	@Override
	public final Collection<GraphObject> getIncidentVertices(Relation e) {
		ArrayList<GraphObject> list = new ArrayList<>(2);
		list.add(e.getStart());
		list.add(e.getEnd());
		return list;
	}

	@Override
	public final Collection<Relation> getInEdges(GraphObject v) {
		Set<Relation> rels = new TreeSet<>();
		for (Relation r : getIncidentEdges(v)) {
			if (r.getEnd().equals(v)) {
				rels.add(r);
			}
		}
		return rels;
	}

	@Override
	public final Collection<Relation> getOutEdges(GraphObject v) {
		Set<Relation> rels = new TreeSet<>();
		for (Relation r : getIncidentEdges(v)) {
			if (r.getStart().equals(v)) {
				rels.add(r);
			}
		}
		return rels;
	}

	@Override
	public final Collection<GraphObject> getPredecessors(GraphObject v) {
		Set<GraphObject> rels = new TreeSet<>();
		for (Relation r : getIncidentEdges(v)) {
			if (r.getEnd().equals(v)) {
				rels.add(r.getStart());
			}
		}
		return rels;
	}

	@Override
	public final Collection<GraphObject> getSuccessors(GraphObject v) {
		Set<GraphObject> rels = new TreeSet<>();
		for (Relation r : getIncidentEdges(v)) {
			if (r.getStart().equals(v)) {
				rels.add(r.getEnd());
			}
		}
		return rels;
	}

	@Override
	public final int inDegree(GraphObject v) {
		return getInEdges(v).size();
	}

	@Override
	public final int outDegree(GraphObject v) {
		return getOutEdges(v).size();
	}

	@Override
	public final boolean isPredecessor(GraphObject v, GraphObject v1) {
		return getPredecessors(v).contains(v1);
	}

	@Override
	public final boolean isSuccessor(GraphObject v, GraphObject v1) {
		return getSuccessors(v).contains(v1);
	}

	@Override
	public final int getPredecessorCount(GraphObject v) {
		return getPredecessors(v).size();
	}

	@Override
	public final int getSuccessorCount(GraphObject v) {
		return getSuccessors(v).size();
	}

	@Override
	public final GraphObject getSource(Relation e) {
		return e.getStart();
	}

	@Override
	public final GraphObject getDest(Relation e) {
		return e.getEnd();
	}

	@Override
	public final boolean isSource(GraphObject v, Relation e) {
		return v.equals(e.getStart());
	}

	@Override
	public final boolean isDest(GraphObject v, Relation e) {
		return v.equals(e.getEnd());
	}

	@Override
	public final boolean addEdge(Relation e, GraphObject v, GraphObject v1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public final boolean addEdge(Relation e, GraphObject v, GraphObject v1, EdgeType et) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public final Pair<GraphObject> getEndpoints(Relation e) {
		return new Pair<>(e.getStart(), e.getEnd());
	}

	@Override
	public final GraphObject getOpposite(GraphObject v, Relation e) {
		return e.getOther(v);
	}

	@Override
	public final Relation findEdge(GraphObject v, GraphObject v1) {
		for (Relation r : getIncidentEdges(v)) {
			if (r.getOther(v).equals(v1)) {
				return r;
			}
		}
		return null;
	}

	@Override
	public final Collection<Relation> findEdgeSet(GraphObject v, GraphObject v1) {
		Set<Relation> rels = new TreeSet<>();
		for (Relation r : getIncidentEdges(v)) {
			if (r.getOther(v).equals(v1)) {
				rels.add(r);
			}
		}
		return rels;
	}

	@Override
	public final boolean addVertex(GraphObject v) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public final boolean addEdge(Relation e, Collection<? extends GraphObject> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public final boolean addEdge(Relation e, Collection<? extends GraphObject> clctn, EdgeType et) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public final boolean removeVertex(GraphObject v) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public final boolean removeEdge(Relation e) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public final boolean isNeighbor(GraphObject v, GraphObject v1) {
		return !findEdgeSet(v, v1).isEmpty();

	}

	@Override
	public final boolean isIncident(GraphObject v, Relation e) {
		return e.getStart().equals(v) || e.getEnd().equals(v);
	}

	@Override
	public final int degree(GraphObject v) {
		return getIncidentEdges(v).size();
	}

	@Override
	public final int getNeighborCount(GraphObject v) {
		return degree(v);
	}

	@Override
	public final int getIncidentCount(Relation e) {
		return 2;
	}

	@Override
	public final EdgeType getEdgeType(Relation e) {
		return EdgeType.UNDIRECTED;
	}

	@Override
	public final EdgeType getDefaultEdgeType() {
		return EdgeType.UNDIRECTED;
	}

	@Override
	public final Collection<Relation> getEdges(EdgeType et) {
		return getEdges();
	}

	@Override
	public final int getEdgeCount(EdgeType et) {
		return getEdgeCount();
	}
}
