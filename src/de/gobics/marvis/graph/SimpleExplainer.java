package de.gobics.marvis.graph;

import java.util.*;
import java.util.logging.Logger;

public class SimpleExplainer implements ExplainablePredicate {

	protected static final Logger logger = Logger.getLogger(SimpleExplainer.class.getName());
	HashMap<MetabolicNetwork, TreeMap<GraphObject, Boolean>> cache = new HashMap<MetabolicNetwork, TreeMap<GraphObject, Boolean>>();
	HashMap<MetabolicNetwork, Integer> graph_size = new HashMap<MetabolicNetwork, Integer>();
	HashMap<MetabolicNetwork, Integer> graph_relations = new HashMap<MetabolicNetwork, Integer>();

	@Override
	public boolean evaluate(MetabolicNetwork graph, GraphObject graphobject) {
		Boolean is_explainable = getCache(graph, graphobject);
		if (is_explainable != null) {
			//logger.finer("Evaluation " + graphobject + " in graph '" + graph+"':"+is_explainable);
			return is_explainable;
		}

		is_explainable = false;

		if (graphobject instanceof Compound) {
			is_explainable = evaluateCompound(graph, (Compound) graphobject);
		}
		else if (graphobject instanceof Enzyme) {
			is_explainable = evaluateEnzyme(graph, (Enzyme) graphobject);
		}
		else if (graphobject instanceof Gene) {
			is_explainable = evaluateGene(graph, (Gene) graphobject);
		}
		else if (graphobject instanceof Marker) {
			is_explainable = true;
		}
		else if (graphobject instanceof Pathway) {
			is_explainable = evaluatePathway(graph, (Pathway) graphobject);
		}
		else if (graphobject instanceof Reaction) {
			is_explainable = evaluateReaction(graph, (Reaction) graphobject);
		}
		else if (graphobject instanceof Transcript) {
			is_explainable = true;
		}
		else {
			throw new IllegalArgumentException("Can not evaluate object of unkown Class: " + graphobject);
		}

		setCache(graph, graphobject, is_explainable);
		//logger.finer("Object " + graphobject + " in graph '" + graph + "' is explainable: " + is_explainable);
		return is_explainable;
	}

	public void clearCache() {
		cache.clear();
	}

	public void clearCache(MetabolicNetwork graph) {
		if (cache.containsKey(graph)) {
			cache.get(graph).clear();
		}
	}

	protected Boolean getCache(MetabolicNetwork graph, GraphObject object) {
		if (graph_size.containsKey(graph) && graph_size.get(graph) != graph.size()) {
			clearCache(graph);
			return null;
		}
		if (graph_relations.containsKey(graph) && graph_relations.get(graph) != graph.countRelations()) {
			clearCache(graph);
			return null;
		}

		if (!cache.containsKey(graph)) {
			return null;
		}
		if (!cache.get(graph).containsKey(object)) {
			return null;
		}
		return cache.get(graph).get(object);
	}

	protected void setCache(MetabolicNetwork graph, GraphObject object, Boolean value) {
		if (!cache.containsKey(graph)) {
			cache.put(graph, new TreeMap<GraphObject, Boolean>());
		}

		graph_size.put(graph, graph.size());
		graph_relations.put(graph, graph.countRelations());
		cache.get(graph).put(object, value);
	}

	protected boolean evaluatePathway(MetabolicNetwork graph, Pathway pathway) {
		double explainable_counter = 0;
		LinkedList<Reaction> reactions = graph.getReactions(pathway);
		for (Reaction r : reactions) {
			if (evaluateReaction(graph, r)) {
				explainable_counter++;
			}
		}
		if (reactions.size() < 1) {
			return false;
		}
		return (explainable_counter / new Double(reactions.size())) > 0.5;
	}

	protected boolean evaluateCompound(MetabolicNetwork graph, Compound m) {
		if (!graph.hasMarkers()) {
			/*if (!graph.hasTranscripts()) {
			 logger.finer("MetabolicNetwork does not contain input");
			 return true;
			 }
			
			 logger.finer("Evaluating transcript only");
			 for (Reaction r : graph.getProductToReaction(m)) {
			 if (evaluateReaction(graph, r)) {
			 logger.finer("Compound is explainable because reaction " + r + " is");
			 return true;
			 }
			 }
			 for (Reaction r : graph.getSubstrateToReaction(m)) {
			 if (evaluateReaction(graph, r)) {
			 logger.finer("Compound is explainable because reaction " + r + " is");
			 return true;
			 }
			 }*/
			return true;
		}

		//logger.finer("Compound " + m + " is annotated by markers: " + graph.getAnnotatingMarker(m));
		return graph.getAnnotatingMarker(m).size() > 0;
	}

	protected boolean evaluateEnzyme(MetabolicNetwork graph, Enzyme enzyme) {
		// If there is no encoding Gene, assume existence.
		if (graph.getEncodingGenes(enzyme).isEmpty()) {
			return true;
		}

		// Find at least one explainable gene.
		for (Gene g : graph.encodedByGenes(enzyme)) {
			if (evaluateGene(graph, g)) {
				//logger.finer(enzyme + " evaluates true by gene " + g);
				return true;
			}
		}
		return false;
	}

	protected boolean evaluateGene(MetabolicNetwork graph, Gene gene) {
		if (!graph.hasTranscripts()) {
			//logger.finer("Gene is valid because there are not transcripts at all");
			return true;
		}
		//logger.finer("Gene " + gene + " is annotated by " + graph.getTranscripts(gene).size() + " transcripts");
		return graph.getTranscripts(gene).size() > 0;
	}

	protected boolean evaluateReaction(MetabolicNetwork graph, Reaction r) {
		boolean has_molecule = false, has_enzyme = false;

		for (Enzyme enzyme : graph.getEnzymes(r)) {
			if (evaluateEnzyme(graph, enzyme)) {
				has_enzyme = true;
				break;
			}
		}

		for (Compound c : graph.getCompounds(r)) {
			if (evaluateCompound(graph, c)) {
				has_molecule = true;
				break;
			}
		}

		return has_enzyme && has_molecule;
	}
}
