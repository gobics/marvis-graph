package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Reaction;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author manuel
 */
public class WeightedDirectedReactionGraph implements DirectedGraph<Reaction, DirectedReactionRelation>, Transformer<DirectedReactionRelation, Number> {

	private final MetabolicNetwork network;
	private final Map<Reaction, Set<DirectedReactionRelation>> out_edges = new TreeMap<Reaction, Set<DirectedReactionRelation>>();
	private final Map<Reaction, Set<DirectedReactionRelation>> in_edges = new TreeMap<Reaction, Set<DirectedReactionRelation>>();

	public WeightedDirectedReactionGraph(MetabolicNetwork network) {
		this.network = network;

		for (Reaction r : network.getReactions()) {
			Set<DirectedReactionRelation> rels = new TreeSet<DirectedReactionRelation>();

			Set<Compound> compounds = new TreeSet<>();
			compounds.addAll(network.getSubstrates(r));
			compounds.addAll(network.getProducts(r));

			for (Compound c : compounds) {
				for (Reaction r2 : network.getReactions(c)) {
					if (!r.equals(r2)) {
						rels.add(new DirectedReactionRelation(r, r2, c));
					}
				}
			}

			if (!rels.isEmpty()) {
				out_edges.put(r, rels);
			}
		}
	}

	@Override
	public Collection<DirectedReactionRelation> getInEdges(Reaction v) {
		if (!in_edges.containsKey(v)) {
			return new ArrayList<DirectedReactionRelation>(0);
		}
		return in_edges.get(v);
	}

	@Override
	public Collection<DirectedReactionRelation> getOutEdges(Reaction v) {
		if (!out_edges.containsKey(v)) {
			return new ArrayList<DirectedReactionRelation>(0);
		}
		return out_edges.get(v);
	}

	@Override
	public Collection<Reaction> getPredecessors(Reaction v) {
		Set<Reaction> reactions = new TreeSet<Reaction>();
		for (DirectedReactionRelation edge : getInEdges(v)) {
			reactions.add(edge.getFrom());
		}
		return reactions;
	}

	@Override
	public Collection<Reaction> getSuccessors(Reaction v) {
		Set<Reaction> reactions = new TreeSet<Reaction>();
		for (DirectedReactionRelation edge : getOutEdges(v)) {
			reactions.add(edge.getTo());
		}
		return reactions;
	}

	@Override
	public int inDegree(Reaction v) {
		return getInEdges(v).size();
	}

	@Override
	public int outDegree(Reaction v) {
		return getOutEdges(v).size();
	}

	@Override
	public boolean isPredecessor(Reaction v, Reaction v1) {
		return getPredecessors(v).contains(v1);
	}

	@Override
	public boolean isSuccessor(Reaction v, Reaction v1) {
		return getSuccessors(v).contains(v1);
	}

	@Override
	public int getPredecessorCount(Reaction v) {
		return getPredecessors(v).size();
	}

	@Override
	public int getSuccessorCount(Reaction v) {
		return getSuccessors(v).size();
	}

	@Override
	public Reaction getSource(DirectedReactionRelation e) {
		return e.getFrom();
	}

	@Override
	public Reaction getDest(DirectedReactionRelation e) {
		return e.getTo();
	}

	@Override
	public boolean isSource(Reaction v, DirectedReactionRelation e) {
		return v.equals(e.getFrom());
	}

	@Override
	public boolean isDest(Reaction v, DirectedReactionRelation e) {
		return v.equals(e.getTo());
	}

	@Override
	public boolean addEdge(DirectedReactionRelation e, Reaction v, Reaction v1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean addEdge(DirectedReactionRelation e, Reaction v, Reaction v1, EdgeType et) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Pair<Reaction> getEndpoints(DirectedReactionRelation e) {
		return new Pair<Reaction>(e.getFrom(), e.getTo());
	}

	@Override
	public Reaction getOpposite(Reaction v, DirectedReactionRelation e) {
		return e.getFrom().equals(v) ? e.getTo() : e.getFrom();
	}

	@Override
	public Collection<DirectedReactionRelation> getEdges() {
		Set<DirectedReactionRelation> edges = new TreeSet<DirectedReactionRelation>();
		for (Set<DirectedReactionRelation> edges2 : in_edges.values()) {
			edges.addAll(edges2);
		}
		for (Set<DirectedReactionRelation> edges2 : out_edges.values()) {
			edges.addAll(edges2);
		}
		return edges;
	}

	@Override
	public Collection<Reaction> getVertices() {
		return network.getReactions();
	}

	@Override
	public boolean containsVertex(Reaction v) {
		return network.containsObject(v);
	}

	@Override
	public boolean containsEdge(DirectedReactionRelation e) {
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
	public Collection<Reaction> getNeighbors(Reaction v) {
		Set<Reaction> reactions = new TreeSet<Reaction>();
		for (DirectedReactionRelation edge : getIncidentEdges(v)) {
			reactions.add(getOpposite(v, edge));
		}
		return reactions;
	}

	@Override
	public Collection<DirectedReactionRelation> getIncidentEdges(Reaction v) {
		Collection<DirectedReactionRelation> edges = getInEdges(v);
		edges.addAll(getOutEdges(v));
		return edges;
	}

	@Override
	public Collection<Reaction> getIncidentVertices(DirectedReactionRelation e) {
		Collection<Reaction> reactions = new ArrayList<Reaction>(2);
		reactions.add(e.getFrom());
		reactions.add(e.getTo());
		return reactions;
	}

	@Override
	public DirectedReactionRelation findEdge(Reaction v, Reaction v1) {
		for (DirectedReactionRelation edge : getOutEdges(v)) {
			if (v1.equals(edge.getTo())) {
				return edge;
			}
		}
		return null;
	}

	@Override
	public Collection<DirectedReactionRelation> findEdgeSet(Reaction v, Reaction v1) {
		Collection<DirectedReactionRelation> edges = new TreeSet<DirectedReactionRelation>();
		for (DirectedReactionRelation edge : getOutEdges(v)) {
			if (v1.equals(edge.getTo())) {
				edges.add(edge);
			}
		}
		return edges;
	}

	@Override
	public boolean addVertex(Reaction v) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean addEdge(DirectedReactionRelation e, Collection<? extends Reaction> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean addEdge(DirectedReactionRelation e, Collection<? extends Reaction> clctn, EdgeType et) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean removeVertex(Reaction v) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean removeEdge(DirectedReactionRelation e) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isNeighbor(Reaction v, Reaction v1) {
		return getNeighbors(v).contains(v1);
	}

	@Override
	public boolean isIncident(Reaction v, DirectedReactionRelation e) {
		return e.getFrom().equals(v) || e.getTo().equals(v);
	}

	@Override
	public int degree(Reaction v) {
		return getIncidentEdges(v).size();
	}

	@Override
	public int getNeighborCount(Reaction v) {
		return getNeighbors(v).size();
	}

	@Override
	public int getIncidentCount(DirectedReactionRelation e) {
		return 2;
	}

	@Override
	public EdgeType getEdgeType(DirectedReactionRelation e) {
		return EdgeType.DIRECTED;
	}

	@Override
	public EdgeType getDefaultEdgeType() {
		return EdgeType.DIRECTED;
	}

	@Override
	public Collection<DirectedReactionRelation> getEdges(EdgeType et) {
		return et.equals(EdgeType.DIRECTED) ? getEdges() : new ArrayList<DirectedReactionRelation>(0);
	}

	@Override
	public int getEdgeCount(EdgeType et) {
		return getEdges(et).size();
	}

	@Override
	public Number transform(DirectedReactionRelation i) {
		// Build sum of all weights
		Reaction from = i.getFrom();
		int sum_of_weights = 0;
		for (DirectedReactionRelation edge : getOutEdges(from)) {
			Compound c = edge.getSharedCompound();
			sum_of_weights += network.getReactions(c).size() - 1;
		}

		return sum_of_weights - network.getReactions(i.getSharedCompound()).size() + 1;
	}
}
