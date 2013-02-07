/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.utils.Formula;
import de.gobics.marvis.utils.StringUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javacyc.Javacyc;

/**
 *
 * @author manuel
 */
public class PwtoolsCreateNetworkProcess extends AbstractNetworkCreator {

	private static final Logger logger = Logger.getLogger(PwtoolsCreateNetworkProcess.class.
			getName());
	private Javacyc javacyc;
	//private final de.gobics.marvis.pathway.Client marvis_client = new Client("134.76.74.151", 31422, new User("marvis", "graph"));
	private String organism = "META";
	private MetabolicNetwork graph = null;
	private final TreeSet<GraphObject> checked = new TreeSet<GraphObject>();
	private final TreeSet<GraphObject> to_check = new TreeSet<GraphObject>();
	private String base_url = null;

	public PwtoolsCreateNetworkProcess() {
		this("META");
	}

	public PwtoolsCreateNetworkProcess(String organism) {
		javacyc = new Javacyc(getOrganism());
		setOrganism(organism);
		setBaseUrl("http://biofung.gobics.de:1555/" + organism.toUpperCase() + "/substring-search?object=");
	}

	public void setOrganism(String new_organism) {
		if (organism == null || organism.isEmpty()) {
			throw new NullPointerException("Need valid organism, not '" + organism + "'");
		}
		organism = new_organism.toUpperCase();
		javacyc.selectOrganism(organism);
	}

	public String getOrganism() {
		return organism;
	}

	public String getBaseUrl() {
		return base_url;
	}

	public String getBaseUrl(String entity) {
		return base_url + entity;
	}

	public void setBaseUrl(String base_url) {
		this.base_url = base_url;
	}

	@Override
	public MetabolicNetwork doTask() throws Exception {
		// Initialize variables for a new run
		graph = new MetabolicNetwork();
		graph.setName(StringUtils.ucfirst(organism) + "Cyc");
		to_check.clear();
		checked.clear();

		// Download all pathways
		setTaskDescription("Fetching list of pathways");
		for (Object pid : javacyc.allPathways()) {
			graph.createPathway(pid.toString());
		}
		setTaskDescription("Fetching list of reactions");
		for (Object rid : javacyc.allRxns()) {
			graph.createReaction(rid.toString());
		}
		setTaskDescription("Fetching list of enzymes");
		for (Object eid : javacyc.allEnzymes()) {
			graph.createEnzyme(eid.toString());
		}
		setTaskDescription("Fetching gene list for '" + organism + "' from KEGG");
		//for (String gid : javacyc.api.get_genes_by_organism(organism, 1, Integer.MAX_VALUE)) {
		//	graph.createGene(gid);
		//}
		to_check.addAll(graph.getAllObjects());

		setTaskDescription("Fetching data");

		while (to_check.size() > 0) {
			GraphObject go = to_check.first();
			setProgressMax(checked.size()+to_check.size());
			setProgress(checked.size());
			expand(go);
			checked(go);

			if (isCanceled()) {
				return null;
			}
		}

		/*
		// Curate the compound masses
		CompoundMasses masses = marvis_client.getCompoundMassesObj("kegg/reactions");
		for (int i = 0; i < masses.compoundIdsVec.length; i++) {
		String kegg_id = masses.compoundIdsVec[i];
		
		Compound c = graph.getCompound("cpd:" + kegg_id.substring(5));
		if (c == null) {
		logger.warning("Can not find compound for ID: cpd:" + kegg_id.
		substring(5));
		}
		else {
		c.setMass((float) masses.compoundMassesVec[i]);
		}
		
		if (isCancelled()) {
		return null;
		}
		}*/

		return graph;
	}

	private void expand(Compound compound) throws Exception {
		logger.finer("Expanding compound: " + compound);

		ArrayList list_slots = javacyc.getFrameSlots(compound.getId());
		if (list_slots == null || list_slots.isEmpty()) {
			return;
		}

		compound.setName(getNameSlots(compound.getId()));

		String mass_string = getFirstSlot(compound.getId(), "MOLECULAR-WEIGHT", "Molecular-Weight");
		if (valid(mass_string)) {
			mass_string = mass_string.replaceAll("d.*", "");
			float mass = new Double(mass_string).floatValue();
			if (mass > 0) {
				compound.setMass(mass);
			}
		}
		String inchi = getFirstSlot(compound.getId(), "INCHI", "InChI");
		if (valid(inchi)) {
			inchi = inchi.replaceAll("\\.", "");
			Formula formula = Formula.createFormulaFromInChIString(inchi);
			compound.setFormula(formula.getAsString());
			compound.setMass((float) formula.getMass());
		}

		compound.setDescription(getSlots(compound.getId(), "COMMENT"));

		ArrayList reaction_ids = javacyc.reactionsOfCompound(compound.getId());
		for (Object rid : reaction_ids) {
			to_check(graph.createReaction(rid.toString()));
		}
		logger.finer("Expanded compound: " + compound);

	}

	@SuppressWarnings("unchecked")
	protected void expand(Reaction reaction) throws Exception {
		logger.finer("Expanding reaction: " + reaction);



		reaction.setName(getNameSlots(reaction.getId()));
		reaction.setDescription(getSlots(reaction.getId(), "COMMENT", ""));
		reaction.setEcNumber(javacyc.getSlotValue(reaction.getId(), "EC-NUMBER"));

		// Build equation
		ArrayList left = javacyc.getSlotValues(reaction.getId(), "LEFT");
		left.addAll(javacyc.getSlotValues(reaction.getId(), "Left"));
		ArrayList right = javacyc.getSlotValues(reaction.getId(), "RIGHT");
		right.addAll(javacyc.getSlotValues(reaction.getId(), "Right"));

		reaction.setEquation(StringUtils.join(" + ", left) + "  <=>  " + StringUtils.
				join(" + ", right));

		for (Object l : left) {
			Compound c = graph.createCompound(l.toString());
			graph.hasSubstrate(reaction, c);
			to_check(c);
		}
		for (Object l : right) {
			Compound c = graph.createCompound(l.toString());
			graph.hasProduct(reaction, c);
			to_check(c);
		}


		for (Object oid : javacyc.enzymesOfReaction(reaction.getId())) {
			Enzyme e = graph.createEnzyme(oid.toString());
			graph.needsEnzyme(reaction, e);
			to_check(e);
		}

		// Can not fetch pathways. No such function
	}

	protected void expand(Enzyme enzyme) throws Exception {
		logger.finer("Expanding enzyme: " + enzyme);

		enzyme.setName(getNameSlots(enzyme.getId()));
		enzyme.setDescription(getSlots(enzyme.getId(), "COMMENT"));

		for (Object rid : javacyc.reactionsOfEnzyme(enzyme.getId())) {
			Reaction r = graph.createReaction(rid.toString());
			graph.needsEnzyme(r, enzyme);
			to_check(r);
		}

		// Query to expand links to genes
		for (Object gid : javacyc.genesOfProtein(enzyme.getId())) {
			Gene g = graph.createGene(gid.toString());
			graph.encodesFor(g, enzyme);
			to_check(g);
		}
	}

	protected void expand(Gene gene) throws Exception {
		logger.finer("Expanding gene: " + gene);
		gene.setName(getNameSlots(gene.getId()));
		gene.setDefinition(getSlots(gene.getId(), "COMMENT"));

		for (Object eid : javacyc.enzymesOfGene(gene.getId())) {
			Enzyme e = graph.createEnzyme(eid.toString());
			graph.encodesFor(gene, e);
			to_check(e);
		}
	}

	private void expand(GraphObject go) throws Exception {
		if (go instanceof Compound) {
			expand((Compound) go);
		}
		else if (go instanceof Reaction) {
			expand((Reaction) go);
		}
		else if (go instanceof Enzyme) {
			expand((Enzyme) go);
		}
		else if (go instanceof Gene) {
			expand((Gene) go);
		}
		else if (go instanceof Pathway) {
			expand((Pathway) go);
		}
		else {
			logger.fine("Can not expand type: " + go.getClass());
			return;
		}

		// If GraphObject is a valid kegg object set the url
		go.setUrl(getBaseUrl(go.getId()));

	}

	protected void expand(Pathway pathway) throws Exception {
		logger.finer("Expanding pathway " + pathway);


		for (Object rid : javacyc.getReactionList(pathway.getId())) {
			Reaction r = graph.createReaction(rid.toString());
			graph.happensIn(r, pathway);
			to_check(r);
		}


		pathway.setName(getNameSlots(pathway.getId()));
		pathway.setDescription(getSlots(pathway.getId(), "COMMENT"));

	}

	private void to_check(GraphObject go) {
		if (!checked.contains(go)) {
			to_check.add(go);
		}
	}

	private void checked(GraphObject go) {
		checked.add(go);
		to_check.remove(go);
	}

	private String pureId(String string) throws IllegalArgumentException {
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(string);
		if (m.find()) {
			return string.substring(m.start(), m.end());
		}
		else {
			throw new IllegalArgumentException("The string does not seem like a valid id: " + string);
		}
	}

	@SuppressWarnings("unchecked")
	private String getSlots(final String frame_id, final String... slots) {
		LinkedList<String> results = new LinkedList<String>();

		for (String slot : slots) {
			results.addAll(javacyc.getSlotValues(frame_id, slot));
			results.addAll(javacyc.getSlotValues(frame_id, StringUtils.ucfirst(slot, "-")));
		}

		// Remove non-valid strings (e.g. errors);
		int idx = 0;
		while (idx < results.size()) {
			if (!valid(results.get(idx))) {
				results.remove(idx);
			}
			else {
				idx++;
			}
		}

		return StringUtils.join("; ", results.toArray(new String[results.size()]));
	}

	@SuppressWarnings("unchecked")
	private String getFirstSlot(final String frame_id, final String... slots) {
		for (String slot : slots) {
			String result = javacyc.getSlotValue(frame_id, slot);
			if (valid(result)) {
				return result;
			}
		}
		return null;
	}

	private String getNameSlots(final String frame_id) {
		return getSlots(frame_id, "SYSTEMATIC-NAME", "NAME", "COMMON-NAME", "SYNONYMS");
	}

	private boolean valid(String s) {
		return s != null && !s.isEmpty() && !s.toLowerCase().equals(":error") && !s.
				toLowerCase().equals("nil");
	}
}
