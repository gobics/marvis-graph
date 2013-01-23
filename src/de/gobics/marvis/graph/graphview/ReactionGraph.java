/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.*;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class ReactionGraph extends AbstractGraph {

	private static final Logger logger = Logger.getLogger(ReactionGraph.class.
			getName());
	private final int cofactor_threshold;
	private final boolean conntect_via_explainable_compounds_only;
	private final TreeMap<Reaction, TreeSet<Relation>> relation_cache = new TreeMap<>();

	/**
	 * Equivalent to
	 * <code> new GraphViewReactions(parent, false, -1)</code>
	 *
	 * @param parent
	 */
	public ReactionGraph(MetabolicNetwork parent) {
		this(parent, false, -1);
	}

	/**
	 * Equivalent to
	 * <code> new GraphViewReactions(parent, conntect_via_explainable_compounds_only, -1)</code>
	 *
	 * @param parent
	 * @param conntect_via_explainable_compounds_only
	 */
	public ReactionGraph(MetabolicNetwork parent, boolean conntect_via_explainable_compounds_only) {
		this(parent, conntect_via_explainable_compounds_only, -1);
	}

	/**
	 * Equivalent to
	 * <code> new GraphViewReactions(parent, false, cofactor_threshold)</code>
	 *
	 * @param parent
	 * @param cofactor_threshold
	 */
	public ReactionGraph(MetabolicNetwork parent, int cofactor_threshold) {
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
	public ReactionGraph(MetabolicNetwork parent, boolean conntect_via_explainable_compounds_only, int cofactor_threshold) {
		super(parent);
		this.conntect_via_explainable_compounds_only = conntect_via_explainable_compounds_only;
		this.cofactor_threshold = cofactor_threshold;
	}

	/**
	 * This method checks whether or not a {@link Compound} is accepted in this
	 * network. It check the {@code cofactor_threshold} as well if it is
	 * explainable (if needed).
	 *
	 * @param m
	 * @return
	 */
	private boolean accept(Compound m) {
		if (cofactor_threshold > 0 && getRootNetwork().getReactions(m).size() >= cofactor_threshold) {
			return false;
		}
		if (conntect_via_explainable_compounds_only && !getRootNetwork().getAnnotatingMarker(m).isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	public Collection<Relation> getEdges() {
		TreeSet<Relation> rels = new TreeSet<>();
		for (GraphObject reaction : getVertices()) {
			rels.addAll(getIncidentEdges(reaction));
		}
		return rels;
	}

	@Override
	public Collection<GraphObject> getVertices() {
		return new TreeSet<GraphObject>(getParent().getReactions());
	}

	@Override
	public Collection<Relation> getIncidentEdges(GraphObject v) {
		if (!(v instanceof Reaction)) {
			throw new RuntimeException("Can only work with Reaction objects, not: " + v.getClass().getName());
		}
		TreeSet<Relation> neighbors = relation_cache.get((Reaction) v);
		if (neighbors == null) {
			neighbors = new TreeSet<>();
			for (Compound compound : getParent().getCompounds((Reaction) v)) {
				if (accept(compound)) {
					for (Reaction neighbor : getParent().getReactions(compound)) {
						neighbors.add(new ReactionRelation((Reaction) v, neighbor));
					}
				}
			}
			relation_cache.put((Reaction) v, neighbors);

		}
		return neighbors;
	}
}
