/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author manuel
 */
public abstract class GraphViewAbstract<E> implements Graph<GraphObject, E> {

	private final MetabolicNetwork parent;

	public GraphViewAbstract(MetabolicNetwork basis_network) {
		if (basis_network == null) {
			throw new NullPointerException("Given base network is NULL");
		}
		this.parent = basis_network;
	}

	public MetabolicNetwork getParent() {
		return parent;
	}

	public MetabolicNetwork getFirstParentNetwork() {
		MetabolicNetwork n = getParent();
		while (n.isSubnetwork()) {
			n = n.getParent();
		}
		return n;
	}

	public boolean parentContains(Class<? extends GraphObject> c) {
		return !getParent().getAllObjects(c).isEmpty();
	}

	abstract public Graph<GraphObject, E> getView();

	public boolean isExplainable(GraphObject o) {
		return parent.isExplainable(o);
	}

	public boolean isExplainable(GraphObject o, int allowed_gaps) {
		if (parent.isExplainable(o)) {
			return true;
		}

		if (allowed_gaps > 1) {
			for (GraphObject neighbor : this.getNeighbors(o)) {
				if (isExplainable(o, allowed_gaps - 1)) {
					return true;
				}
			}
		}

		return false;
	}

	abstract public Set<Relation> getEnvironment(GraphObject o);

	@Override
	public Collection<E> getInEdges(GraphObject v) {
		return getView().getInEdges(v);
	}

	@Override
	public Collection<E> getOutEdges(GraphObject v) {
		return getView().getOutEdges(v);
	}

	@Override
	public Collection<GraphObject> getPredecessors(GraphObject v) {
		return getView().getPredecessors(v);
	}

	@Override
	public Collection<GraphObject> getSuccessors(GraphObject v) {
		return getView().getSuccessors(v);
	}

	@Override
	public int inDegree(GraphObject v) {
		return getView().inDegree(v);
	}

	@Override
	public int outDegree(GraphObject v) {
		return getView().outDegree(v);
	}

	@Override
	public boolean isPredecessor(GraphObject v, GraphObject v1) {
		return getView().isPredecessor(v, v1);
	}

	@Override
	public boolean isSuccessor(GraphObject v, GraphObject v1) {
		return getView().isSuccessor(v, v1);
	}

	@Override
	public int getPredecessorCount(GraphObject v) {
		return getView().getPredecessorCount(v);
	}

	@Override
	public int getSuccessorCount(GraphObject v) {
		return getView().getSuccessorCount(v);
	}

	@Override
	public GraphObject getSource(E e) {
		return getView().getSource(e);
	}

	@Override
	public GraphObject getDest(E e) {
		return getView().getDest(e);
	}

	@Override
	public boolean isSource(GraphObject v, E e) {
		return getView().isSource(v, e);
	}

	@Override
	public boolean isDest(GraphObject v, E e) {
		return getView().isDest(v, e);
	}

	@Override
	public boolean addEdge(E e, GraphObject v, GraphObject v1) {
		return getView().addEdge(e, v, v1);
	}

	@Override
	public boolean addEdge(E e, GraphObject v, GraphObject v1, EdgeType et) {
		return getView().addEdge(e, v, v1, et);
	}

	@Override
	public Pair<GraphObject> getEndpoints(E e) {
		return getView().getEndpoints(e);
	}

	@Override
	public GraphObject getOpposite(GraphObject v, E e) {
		return getView().getOpposite(v, e);
	}

	@Override
	public Collection<E> getEdges() {
		return getView().getEdges();
	}

	@Override
	public Collection<GraphObject> getVertices() {
		return getView().getVertices();
	}

	@Override
	public boolean containsVertex(GraphObject v) {
		return getView().containsVertex(v);
	}

	@Override
	public boolean containsEdge(E e) {
		return getView().containsEdge(e);
	}

	@Override
	public int getEdgeCount() {
		return getView().getEdgeCount();
	}

	@Override
	public int getVertexCount() {
		return getView().getVertexCount();
	}

	@Override
	public Collection<GraphObject> getNeighbors(GraphObject v) {
		return getView().getNeighbors(v);
	}

	@Override
	public Collection<E> getIncidentEdges(GraphObject v) {
		return getView().getIncidentEdges(v);
	}

	@Override
	public Collection<GraphObject> getIncidentVertices(E e) {
		return getView().getIncidentVertices(e);
	}

	@Override
	public E findEdge(GraphObject v, GraphObject v1) {
		return getView().findEdge(v, v1);
	}

	@Override
	public Collection<E> findEdgeSet(GraphObject v, GraphObject v1) {
		return getView().findEdgeSet(v, v1);
	}

	@Override
	public boolean addVertex(GraphObject v) {
		return getView().addVertex(v);
	}

	@Override
	public boolean addEdge(E e, Collection<? extends GraphObject> clctn) {
		return getView().addEdge(e, clctn);
	}

	@Override
	public boolean addEdge(E e, Collection<? extends GraphObject> clctn, EdgeType et) {
		return getView().addEdge(e, clctn, et);
	}

	@Override
	public boolean removeVertex(GraphObject v) {
		return false;
	}

	@Override
	public boolean removeEdge(E e) {
		return false;
	}

	@Override
	public boolean isNeighbor(GraphObject v, GraphObject v1) {
		return getView().isNeighbor(v, v1);
	}

	@Override
	public boolean isIncident(GraphObject v, E e) {
		return getView().isIncident(v, e);
	}

	@Override
	public int degree(GraphObject v) {
		return getView().degree(v);
	}

	@Override
	public int getNeighborCount(GraphObject v) {
		return getView().getNeighborCount(v);
	}

	@Override
	public int getIncidentCount(E e) {
		return getView().getIncidentCount(e);
	}

	@Override
	public EdgeType getEdgeType(E e) {
		return getView().getEdgeType(e);
	}

	@Override
	public EdgeType getDefaultEdgeType() {
		return getView().getDefaultEdgeType();
	}

	@Override
	public Collection<E> getEdges(EdgeType et) {
		return getView().getEdges(et);
	}

	@Override
	public int getEdgeCount(EdgeType et) {
		return getView().getEdgeCount(et);
	}
}
