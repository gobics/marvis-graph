/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.*;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class GraphViewReactions implements GraphView<Reaction, ReactionRelation> {

    private static final Logger logger = Logger.getLogger(GraphViewReactions.class.
	    getName());
    private int cofactor_threshold = -1;
    private final boolean conntect_via_explainable_compounds_only;
    private final MetabolicNetwork parent;
    private final TreeMap<Reaction, TreeSet<Reaction>> neighbor_cache = new TreeMap<>();

    /**
     * Equivalent to
     * <code> new GraphViewReactions(parent, false, -1)</code>
     *
     * @param parent
     */
    public GraphViewReactions(MetabolicNetwork parent) {
	this(parent, false, -1);
    }

    /**
     * Equivalent to
     * <code> new GraphViewReactions(parent, conntect_via_explainable_compounds_only, -1)</code>
     *
     * @param parent
     * @param conntect_via_explainable_compounds_only
     */
    public GraphViewReactions(MetabolicNetwork parent, boolean conntect_via_explainable_compounds_only) {
	this(parent, conntect_via_explainable_compounds_only, -1);
    }

    /**
     * Equivalent to
     * <code> new GraphViewReactions(parent, false, cofactor_threshold)</code>
     *
     * @param parent
     * @param cofactor_threshold
     */
    public GraphViewReactions(MetabolicNetwork parent, int cofactor_threshold) {
	this(parent, false, cofactor_threshold);
    }

    /**
     * Creates a new view of the parental network with specific options. A
     * boolean flag specifies, whether reactions should only be connected via
     * compounds with experimental evidence. Furthermore, if
     * {@code cofactor_threshold} is greater than zero, reactions will only be
     * connected when they share a molecule that is not considered to be a
     * cofactor (contributes in less than {@code cofactor_threshold} reactions).
     *
     * @param parent
     * @param conntect_via_explainable_compounds_only
     * @param cofactor_threshold
     */
    public GraphViewReactions(MetabolicNetwork parent, boolean conntect_via_explainable_compounds_only, int cofactor_threshold) {
	this.parent = parent;
	this.conntect_via_explainable_compounds_only = conntect_via_explainable_compounds_only;
	this.cofactor_threshold = cofactor_threshold;
    }

    private boolean accept(Compound m) {
	if (cofactor_threshold > 0 && parent.getReactions(m).size() >= cofactor_threshold) {
	    return false;
	}
	if (conntect_via_explainable_compounds_only && !parent.getAnnotatingMarker(m).isEmpty()) {
	    return false;
	}
	return true;
    }

    @Override
    public Collection<Reaction> getNeighbors(Reaction v) {
	TreeSet<Reaction> neighbors = neighbor_cache.get(v);
	if (neighbors == null) {
	    neighbors = new TreeSet<>();
	    neighbor_cache.put(v, neighbors);
	    for (Compound m : parent.getCompounds(v)) {
		if (accept(m)) {
		    neighbors.addAll(parent.getReactions(m));
		}
	    }
	    neighbors.remove(v);
	}
	return neighbors;
    }

    @Override
    public Collection<ReactionRelation> getInEdges(Reaction v) {
	return getIncidentEdges(v);
    }

    @Override
    public Collection<ReactionRelation> getOutEdges(Reaction v) {
	return getInEdges(v);
    }

    @Override
    public Collection<Reaction> getPredecessors(Reaction v) {
	return getNeighbors(v);
    }

    @Override
    public Collection<Reaction> getSuccessors(Reaction v) {
	return getNeighbors(v);
    }

    @Override
    public int inDegree(Reaction v) {
	return getInEdges(v).size();
    }

    @Override
    public int outDegree(Reaction v) {
	return getInEdges(v).size();
    }

    @Override
    public boolean isPredecessor(Reaction v, Reaction v1) {
	return getNeighbors(v).contains(v1);
    }

    @Override
    public boolean isSuccessor(Reaction v, Reaction v1) {
	return getNeighbors(v).contains(v1);
    }

    @Override
    public int getPredecessorCount(Reaction v) {
	return getNeighbors(v).size();
    }

    @Override
    public int getSuccessorCount(Reaction v) {
	return getNeighbors(v).size();
    }

    @Override
    public Reaction getSource(ReactionRelation e) {
	return null;
    }

    @Override
    public Reaction getDest(ReactionRelation e) {
	return null;
    }

    @Override
    public boolean isSource(Reaction v, ReactionRelation e) {
	return e.start.equals(v);
    }

    @Override
    public boolean isDest(Reaction v, ReactionRelation e) {
	return e.end.equals(v);
    }

    @Override
    public boolean addEdge(ReactionRelation e, Reaction v, Reaction v1) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addEdge(ReactionRelation e, Reaction v, Reaction v1, EdgeType et) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Pair<Reaction> getEndpoints(ReactionRelation e) {
	return new Pair<Reaction>(e.start, e.end);
    }

    @Override
    public Reaction getOpposite(Reaction v, ReactionRelation e) {
	if (e.start.equals(v)) {
	    return e.end;
	}
	return e.start;
    }

    @Override
    public Collection<ReactionRelation> getEdges() {
	TreeSet<ReactionRelation> rels = new TreeSet<>();
	for (Reaction r : getVertices()) {
	    rels.addAll(getInEdges(r));
	}
	return rels;
    }

    @Override
    public Collection<Reaction> getVertices() {
	return parent.getReactions();
    }

    @Override
    public boolean containsVertex(Reaction v) {
	return parent.containsObject(v);
    }

    @Override
    public boolean containsEdge(ReactionRelation e) {
	return getEdges().contains(e);
    }

    @Override
    public int getEdgeCount() {
	return getEdges().size();
    }

    @Override
    public int getVertexCount() {
	return getVertices().size();
    }

    @Override
    public Collection<ReactionRelation> getIncidentEdges(Reaction v) {
	Collection<Reaction> neighbors = getNeighbors(v);
	Collection<ReactionRelation> rels = new ArrayList<>(neighbors.size());
	for (Reaction r : neighbors) {
	    rels.add(new ReactionRelation(v, r));
	}
	return rels;
    }

    @Override
    public Collection<Reaction> getIncidentVertices(ReactionRelation e) {
	Collection<Reaction> reacs = new ArrayList<>(2);
	reacs.add(e.start);
	reacs.add(e.end);
	return reacs;
    }

    @Override
    public ReactionRelation findEdge(Reaction v, Reaction v1) {
	if (isNeighbor(v, v1)) {
	    return new ReactionRelation(v, v1);
	}
	return null;
    }

    @Override
    public Collection<ReactionRelation> findEdgeSet(Reaction v, Reaction v1) {
	Collection<ReactionRelation> rels = new TreeSet<>();
	ReactionRelation r = findEdge(v, v1);
	if (r != null) {
	    rels.add(r);
	}
	return rels;
    }

    @Override
    public boolean addVertex(Reaction v) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addEdge(ReactionRelation e, Collection<? extends Reaction> clctn) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addEdge(ReactionRelation e, Collection<? extends Reaction> clctn, EdgeType et) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeVertex(Reaction v) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeEdge(ReactionRelation e) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isNeighbor(Reaction v, Reaction v1) {
	return getNeighbors(v).contains(v1);
    }

    @Override
    public boolean isIncident(Reaction v, ReactionRelation e) {
	return v.equals(e.start) || v.equals(e.end);
    }

    @Override
    public int degree(Reaction v) {
	return getNeighborCount(v);
    }

    @Override
    public int getNeighborCount(Reaction v) {
	return getNeighbors(v).size();
    }

    @Override
    public int getIncidentCount(ReactionRelation e) {
	return 1;
    }

    @Override
    public EdgeType getEdgeType(ReactionRelation e) {
	return EdgeType.UNDIRECTED;
    }

    @Override
    public EdgeType getDefaultEdgeType() {
	return EdgeType.UNDIRECTED;
    }

    @Override
    public Collection<ReactionRelation> getEdges(EdgeType et) {
	return getEdges();
    }

    @Override
    public int getEdgeCount(EdgeType et) {
	return getEdges().size();
    }

    @Override
    public boolean isExplainable(GraphObject o) {
	return parent.isExplainable(o);
    }
}
