package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.Relation.RelationshipType.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Displays the pathway overview of the network. Two pathways are connected if
 * they share a compound
 *
 * @author manuel
 */
public class GraphViewPathways extends GraphViewAbstract<PathwayRelation> {
	
	private static final Logger logger = Logger.getLogger(GraphViewPathways.class.
			getName());
	private final SparseGraph<GraphObject, PathwayRelation> view = new SparseGraph<GraphObject, PathwayRelation>();
	
	public GraphViewPathways(MetabolicNetwork parent) {
		super(parent);
	}
	
	private void initView() {
		logger.finer("Initializing the view");
		
		final Pathway[] pathways = getParent().getPathways().toArray(new Pathway[0]);
		for (Pathway r : pathways) {
			view.addVertex(r);
		}
		
		logger.finer("Searching for neighbor relationships");
		// Iterate over all reactions
		for (int idx_i = 0; idx_i < pathways.length; idx_i++) {
			final Pathway start_pathway = pathways[idx_i];
			final TreeSet<Pathway> neighbors = new TreeSet<Pathway>();
			
			for (Reaction reaction : getParent().getReactions(start_pathway)) {
				neighbors.addAll(getParent().getPathways(reaction));
				neighbors.addAll(getParent().getPathways(reaction));
			}
			neighbors.remove(start_pathway);
			
			for (Pathway neighbor : neighbors) {
				if (findEdgeSet(neighbor, start_pathway).isEmpty()) {
					logger.fine("Create relationship edge between "+start_pathway+" and "+neighbor);
					PathwayRelation r = new PathwayRelation();
					addEdge(r, start_pathway, neighbor);
				}
			}
			
		}
	}
	
	@Override
	public Graph<GraphObject, PathwayRelation> getView() {
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
