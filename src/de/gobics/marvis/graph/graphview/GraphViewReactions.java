/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.Relation.RelationshipType.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class GraphViewReactions extends GraphViewAbstract<ReactionRelation> {

	private static final Logger logger = Logger.getLogger(GraphViewReactions.class.
			getName());
	private final SparseGraph<GraphObject, ReactionRelation> view = new SparseGraph<GraphObject, ReactionRelation>();
	private final boolean conntect_via_explainable_compounds_only;

	public GraphViewReactions(MetabolicNetwork parent) {
		super(parent);
		conntect_via_explainable_compounds_only = false;
	}

	public GraphViewReactions(MetabolicNetwork parent, boolean conntect_via_explainable_compounds_only) {
		super(parent);
		this.conntect_via_explainable_compounds_only = conntect_via_explainable_compounds_only;
	}

	private void initView() {
		// Copy all reactions:
		final Collection<Reaction> reactions = getParent().getReactions();
		for (Reaction r : reactions) {
			view.addVertex(r);
		}

		// Iterate over all reactions
		for (Reaction start_reaction : reactions) {
			int counter = 0;
			// Iterate over all reaction neighbors
			final TreeSet<Compound> compounds = new TreeSet<Compound>();
			compounds.addAll(getParent().getProducts(start_reaction));
			compounds.addAll(getParent().getSubstrates(start_reaction));

			for (Compound c : compounds) {

				if (conntect_via_explainable_compounds_only && !getParent().
						isExplainable(c)) {
					continue;
				}

				for (Reaction neighbor : getParent().getProductToReaction(c)) {
					if (neighbor.equals(start_reaction)) {
						continue;
					}

					// There is no edge from the neighbor to current (may have
					// been added first), add a new edge
					ReactionRelation found = view.findEdge(neighbor, start_reaction);
					if (found == null) {
						found = new ReactionRelation();
						view.addEdge(found, start_reaction, neighbor);
					}
					found.addMolecule(c);

				}
				for (Reaction neighbor : getParent().getSubstrateToReaction(c)) {
					if (neighbor.equals(start_reaction)) {
						continue;
					}

					// There is no edge from the neighbor to current (may have
					// been added first), add a new edge
					ReactionRelation found = view.findEdge(neighbor, start_reaction);
					if (found == null) {
						found = new ReactionRelation();
						view.addEdge(found, start_reaction, neighbor);
					}
					found.addMolecule(c);

				}

			}
		}
	}

	@Override
	public Graph<GraphObject, ReactionRelation> getView() {
		if (view.getVertexCount() == 0) {
			initView();
		}
		return view;
	}

	@Override
	public Set<Relation> getEnvironment(GraphObject input_object) {
		Set<Relation> rels = new TreeSet<Relation>();
		if (!(input_object instanceof Reaction)) {
			return rels;
		}
		Reaction reaction = (Reaction) input_object;
		MetabolicNetwork network = getParent();

		// Extract compounds and corresponding marker
		for (Compound s : network.getSubstrates(reaction)) {
			rels.add(new Relation(Relation.RelationshipType.REACTION_HAS_SUBSTRATE, reaction, s));
			for (Marker m : network.getAnnotatingMarker(s)) {
				rels.add(new Relation(Relation.RelationshipType.MARKER_ANNOTATION_COMPOUND, m, s));
			}
		}

		for (Compound p : network.getProducts(reaction)) {
			rels.add(new Relation(Relation.RelationshipType.REACTION_HAS_PRODUCT, reaction, p));
			for (Marker m : network.getAnnotatingMarker(p)) {
				rels.add(new Relation(Relation.RelationshipType.MARKER_ANNOTATION_COMPOUND, m, p));
			}
		}

		// Extract pathways
		for (Pathway p : network.getPathways(reaction)) {
			rels.add(new Relation(Relation.RelationshipType.REACTION_HAPPENSIN_PATHWAY, reaction, p));
		}

		// Extract enzymes, ...
		for (Enzyme e : network.getEnzymes(reaction)) {
			rels.add(new Relation(Relation.RelationshipType.REACTION_NEEDS_ENZYME, reaction, e));

			// ... genes,
			for (Gene g : network.encodedByGenes(e)) {
				rels.add(new Relation(Relation.RelationshipType.GENE_ENCODES_ENZYME, g, e));

				// ... and transcripts
				for (Transcript t : network.getTranscripts(g)) {
					rels.add(new Relation(Relation.RelationshipType.TRANSCRIPT_ISFROM_GENE, t, g));
				}
			}

		}

		return rels;


	}
}
