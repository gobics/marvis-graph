
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.downloader;

import de.gobics.keggapi.InfoResult;
import de.gobics.keggapi.KeggAPI;
import de.gobics.keggapi.KeggMysqlCache;
import de.gobics.marvis.graph.*;
import de.gobics.marvis.utils.reader.KeggEntry;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class KeggCreateNetworkProcess extends AbstractNetworkCreator {

	private static final Logger logger = Logger.getLogger(KeggCreateNetworkProcess.class.
			getName());
	private KeggAPI api = new KeggAPI();
	private String organism;
	private MetabolicNetwork graph = null;
	private final String kegg_bget_url = "http://www.genome.jp/dbget-bin/www_bget?";

	public KeggCreateNetworkProcess() {
		this("map");
	}

	public KeggCreateNetworkProcess(String organism) {
		setOrganism(organism);


		KeggMysqlCache api2 = new KeggMysqlCache();
		if (api2.tryConnect()) {
			api = api2;
		}
	}

	public void setOrganism(String new_organism) {
		if (new_organism == null || new_organism.isEmpty()) {
			throw new NullPointerException("Set organism can not be null or empty");
		}
		organism = new_organism;
	}

	public String getOrganism() {
		return organism;
	}

	public void setKeggAPI(KeggAPI new_api) {
		if (new_api == null) {
			throw new NullPointerException("API parameter can not be null");
		}
		this.api = new_api;
	}

	@Override
	public MetabolicNetwork doTask() throws Exception {
		InfoResult info = api.info(getOrganism().equals("map") ? "ko" : getOrganism());
		if (info == null) {
			throw new IllegalArgumentException("No such databass: " + getOrganism());
		}

		// Initialize variables for a new run
		graph = new MetabolicNetwork();
		graph.setName("KEGG " + getOrganism() + " (" + info.release + ")");


		// Build global relations of all pathways
		setTaskDescription("Building relations");
		buildRelations(graph);

		// Parallel fetching of descriptions and names
		setTaskDescription("Fetching information about the objects");

		LinkedList<GraphObject> objects = graph.getAllObjects();
		setProgressMax(objects.size());
		setProgress(0);
		logger.finer("Invoking parallel jobs for data fetching");
		ExecutorService workerpool = Executors.newFixedThreadPool(10);
		
		for (GraphObject go : graph.getAllObjects()) {
			workerpool.submit(new InfoFetcher(this, go));
		}
		
		workerpool.shutdown();
		
		while(! workerpool.isTerminated() ){
			if(isCanceled()){
				logger.warning("Creation of network is aborted by user signal");
				workerpool.shutdownNow();
				return null;
			}
			Thread.sleep(1000);
		}


		logger.info("Created new metabolic network with " + graph.size() + " objects and " + graph.
				countRelations() + " relations");
		return graph;
	}

	private void buildRelations(MetabolicNetwork graph) throws MalformedURLException, IOException {
		Map<String, TreeSet<String>> links;

		// Genes
		setTaskDescription("Fetching gene list for '" + organism + "' from KEGG");
		for (String gid : api.list_genes_of_organism(organism)) {
			graph.createGene(gid);
		}

		// Enzymes (bi-directional)
		setTaskDescription("Fetching enzymes list for '" + organism + "' from KEGG");
		links = api.link_as_map("enzyme", organism);
		for (String gene_id : links.keySet()) {
			Gene gene = graph.createGene(gene_id);
			for (String enzyme_id : links.get(gene_id)) {
				Enzyme enzyme = graph.createEnzyme(enzyme_id);
				graph.addRelation(RelationshipType.GENE_ENCODES_ENZYME, gene, enzyme);
			}
		}
		links = api.link_as_map(organism, "enzyme");
		for (String enzyme_id : links.keySet()) {
			Enzyme enzyme = graph.createEnzyme(enzyme_id);
			for (String gene_id : links.get(enzyme_id)) {
				Gene gene = graph.createGene(gene_id);
				graph.addRelation(RelationshipType.GENE_ENCODES_ENZYME, gene, enzyme);
			}
		}
		links = api.link_as_map("ec", organism);
		for (String gene_id : links.keySet()) {
			Gene gene = graph.createGene(gene_id);
			for (String enzyme_id : links.get(gene_id)) {
				Enzyme enzyme = graph.createEnzyme(enzyme_id);
				graph.addRelation(RelationshipType.GENE_ENCODES_ENZYME, gene, enzyme);
			}
		}
		links = api.link_as_map(organism, "ec");
		for (String enzyme_id : links.keySet()) {
			Enzyme enzyme = graph.createEnzyme(enzyme_id);
			for (String gene_id : links.get(enzyme_id)) {
				Gene gene = graph.createGene(gene_id);
				graph.addRelation(RelationshipType.GENE_ENCODES_ENZYME, gene, enzyme);
			}
		}

		// Reactions
		setTaskDescription("Fetching Reactions of '" + organism + "'");
		for (String rid : api.fetch_list(0, "list", "reaction")) {
			graph.createReaction(rid);
		}
		links = api.link_as_map("reaction", organism);
		for (TreeSet<String> set : links.values()) {
			for (String reaction_id : set) {
				graph.createReaction(reaction_id);
			}
		}
		links = api.link_as_map(organism, "reaction");
		for (String reaction_id : links.keySet()) {
			graph.createReaction(reaction_id);
		}
		links = api.link_as_map("enzyme", "reaction");
		for (String reaction_id : links.keySet()) {
			Reaction r = graph.getReaction(reaction_id);
			if (r == null) {
				continue;
			}
			for (String enzyme_id : links.get(reaction_id)) {
				Enzyme e = graph.getEnzyme(enzyme_id);
				if (e != null) {
					graph.addRelation(RelationshipType.REACTION_NEEDS_ENZYME, r, e);
				}
			}
		}


		// Pathways (Reactions have to be fetched previously)
		setTaskDescription("Fetching pathways of '" + organism + "'");
		for (String pid : api.list_pathways_of_organism(organism)) {
			if (!pid.endsWith("01110") && !pid.endsWith("01100")) {
				graph.createPathway(pid);
			}
		}
		setTaskDescription("Linking pathways and reactions");
		links = api.link_as_map("pathway", "reaction");
		for (Reaction r : graph.getReactions()) {
			if (!links.containsKey(r.getId())) {
				continue;
			}
			for (String pathway_id : links.get(r.getId())) {
				pathway_id = pathway_id.replaceFirst("map", organism);
				Pathway p = graph.getPathway(pathway_id);
				if (p != null) {
					graph.addRelation(RelationshipType.REACTION_HAPPENSIN_PATHWAY, r, p);
				}
			}
		}


		// Compounds
		setTaskDescription("Fetching compounds of '" + organism + "'");
		for (String cid : api.fetch_list(0, "list", "compound")) {
			graph.createCompound(cid);
		}
		

		// Iterate the reactions to find compounds
		setProgressMax(graph.getReactions().size());
		setTaskDescription("Iterating over reactions to identify compounds");
		for (Reaction r : graph.getReactions()) {
			incrementProgress();
			links = api.link_as_map("compound", r.getId());
			for (String compound_id : links.keySet()) {
				graph.createCompound(compound_id);
			}
			links = api.link_as_map("glycan", r.getId());
			for (String compound_id : links.keySet()) {
				graph.createCompound(compound_id);
			}
		}
	}

	private class InfoFetcher implements Callable<Void> {

		private final GraphObject go;
		private final KeggCreateNetworkProcess parent;

		private InfoFetcher(KeggCreateNetworkProcess parent, GraphObject object) {
			this.parent = parent;
			this.go = object;
		}

		@Override
		public Void call() {
			try {
				expand();
			}
			catch (Exception e) {
				logger.log(Level.WARNING, "Can not expand " + go + ": ", e);
			}
			incrementProgress();
			return null;
		}

		public void expand() throws Exception {
			logger.finer("Expanding metabolic network entity: " + go);
			if (go instanceof Compound) {
				expand((Compound) go);
			}
			else {
				if (go instanceof Reaction) {
					expand((Reaction) go);
				}
				else {
					if (go instanceof Enzyme) {
						expand((Enzyme) go);
					}
					else {
						if (go instanceof Gene) {
							expand((Gene) go);
						}
						else {
							if (go instanceof Pathway) {
								expand((Pathway) go);
							}
							else {
								logger.warning("Can not expand type: " + go.getClass());
								return;
							}
						}
					}
				}
			}

			// If GraphObject is a valid kegg object set the url
			go.setUrl(kegg_bget_url + go.getId());

		}

		private void expand(Compound compound) throws Exception {
			KeggEntry info = api.get(compound.getId());
			if (info.getId() == null) {
				logger.warning("Can not fetch info for: " + compound.getId());
				return;
			}

			compound.setName(info.getAsLine("name"));
			if (info.get("exact_mass") != null) {
				compound.setMass(new Float(info.getFirst("exact_mass")));
			}
			else {
				if (info.get("MOL_WEIGHT") != null) {
					compound.setMass(new Float(info.getFirst("MOL_WEIGHT")));
				}
			}
			compound.setFormula(info.getAsLine("formula"));
			compound.setDescription(info.getAsString("comment"));
		}

		private void expand(Reaction reaction) throws Exception {
			// Query the Kegg API
			KeggEntry info = api.get(reaction.getId());
			if (info.getId() == null) {
				logger.warning("Can not fetch info for: " + reaction.getId());
				return;
			}
			reaction.setName(info.getAsLine("name"));
			reaction.setDescription(info.getAsString("definition"));
			reaction.setEquation(info.getAsLine("equation"));

			boolean product_side_of_equation = false;
			String tokens[] = new String[0];
			if (reaction.getEquation() != null) {
				tokens = reaction.getEquation().split("\\s");
			}

			// Iterate over the token of the equation line
			for (int j = 0; j < tokens.length; j++) {
				if (tokens[j].equals("<=>")) {
					product_side_of_equation = true;
				}
				else {
					if (tokens[j].matches("C\\d\\d\\d\\d\\d")) {
						String compound_id = "cpd:" + tokens[j];

						if (graph.getCompound(compound_id) == null) {
							logger.warning("Missed required compound: " + compound_id);
						}
						Compound c = graph.createCompound(compound_id);

						if (product_side_of_equation) {
							graph.hasProduct(reaction, c);
						}
						else {
							graph.hasSubstrate(reaction, c);
						}
					}
				}
			}
		}

		private void expand(Enzyme enzyme) throws Exception {
			KeggEntry info = api.get(enzyme.getId());
			if (info.getId() == null) {
				logger.warning("Can not fetch info for: " + enzyme.getId());
				return;
			}
			if (info.get("name") != null) {
				enzyme.setName(info.getAsLine("name"));
			}
			else {
				enzyme.setName(info.getAsLine("class"));
			}
			enzyme.setDescription(info.getAsLine("reaction"));

			// Kegg enzymes contain the EC number as ID
			enzyme.setEC(enzyme.getId().substring(3));
		}

		private void expand(Gene gene) throws Exception {
			KeggEntry info = api.get(gene.getId());
			if (info.getId() == null) {
				logger.warning("Can not fetch info for: " + gene.getId());
				return;
			}
			gene.setName(info.getAsLine("name"));
			gene.setDefinition(info.getAsString("definition"));
		}

		/**
		 * Pathways are expanded in a two-step procedure. Because not all
		 * organisms contain all pathways, they will lack some information.
		 *
		 * @param pathway
		 * @throws Exception
		 */
		private void expand(Pathway pathway) throws Exception {
			KeggEntry info = api.get(pathway.getId().replace(":"+organism, ":map"));
			if (info.getId() == null) {
				logger.log(Level.WARNING, "Can not fetch general (map) info for: {0}", pathway.getId());
				return;
			}
			pathway.setName(info.getAsLine("name"));
			pathway.setDescription(info.getAsString("description"));

			info = api.get(pathway.getId());
			if (info.getId() == null) {
				logger.log(Level.WARNING, "Can not fetch info for: {0}", pathway.getId());
				return;
			}
			pathway.setName(info.getAsLine("name"));
			pathway.setDescription(info.getAsString("description"));

		}
	}
}
