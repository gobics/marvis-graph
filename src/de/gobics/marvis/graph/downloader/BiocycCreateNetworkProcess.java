
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.utils.Formula;
import de.gobics.marvis.utils.StringUtils;
import de.gobics.marvis.utils.exception.ChemicalElementUnkownException;
import de.gobics.marvis.utils.reader.Pwtools;
import de.gobics.marvis.utils.reader.PwtoolsEntry;
import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class BiocycCreateNetworkProcess extends AbstractNetworkCreator {

	private static final Logger logger = Logger.getLogger(BiocycCreateNetworkProcess.class.
			getName());
	private MetabolicNetwork graph = null;
	private final TreeSet<GraphObject> checked = new TreeSet<GraphObject>();
	private final TreeSet<GraphObject> to_check = new TreeSet<GraphObject>();
	private File input_file;

	public BiocycCreateNetworkProcess(File input_file) {
		setInputFile(input_file);
	}

	public void setInputFile(File new_input_file) {
		if (new_input_file == null || !new_input_file.exists()) {
			throw new NullPointerException("Set can not be null and must exist");
		}
		input_file = new_input_file;
	}

	public File getInputFile() {
		return input_file;
	}

	@Override
	public MetabolicNetwork performTask() throws Exception {
		// Initialize variables for a new run
		graph = new MetabolicNetwork();
		graph.setName("Biocyc");


		if (input_file.isDirectory()) {
			parse_directory(input_file);
		}
		else {
			throw new RuntimeException("File parsing not supported yet");
		}

		return graph;
	}

	private void parse_directory(File directory) throws FileNotFoundException, IOException {
		File data_directory = new File(directory.getAbsolutePath() + File.separator + "data");
		File data_file;
		BufferedReader in;

		if (!data_directory.exists()) {
			throw new RuntimeException("Data directory does not exist: " + directory.
					getAbsolutePath() + File.separator + "data");
		}

		// Parse compounds
		data_file = new File(data_directory + File.separator + "compounds.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find compound file: " + data_file.
					getAbsolutePath());
		}
		in = new BufferedReader(new FileReader(data_file));
		parse_compounds(in);
		in.close();

		// Parse reactions
		data_file = new File(data_directory + File.separator + "reactions.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find reaction file: " + data_file.
					getAbsolutePath());
		}
		in = new BufferedReader(new FileReader(data_file));
		parse_reactions(in);
		in.close();

		// Parse enzymes/proteins
		data_file = new File(data_directory + File.separator + "proteins.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find enzyme file: " + data_file.
					getAbsolutePath());
		}
		in = new BufferedReader(new FileReader(data_file));
		parse_enzymes(in);
		in.close();

		// Parse pathways
		data_file = new File(data_directory + File.separator + "pathways.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find pathway file: " + data_file.
					getAbsolutePath());
		}
		in = new BufferedReader(new FileReader(data_file));
		parse_pathways(in);
		in.close();

		// Parse reactions
		data_file = new File(data_directory + File.separator + "genes.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find gene file: " + data_file.
					getAbsolutePath());
		}
		in = new BufferedReader(new FileReader(data_file));
		parse_genes(in);
		in.close();

		// Parse links between enzymes and reactions
		data_file = new File(data_directory + File.separator + "enzrxns.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find enzyme-reaction file: " + data_file.
					getAbsolutePath());
		}
		in = new BufferedReader(new FileReader(data_file));
		parse_enzyme_reaction(in);
		in.close();
	}

	private void parse_compounds(BufferedReader in) throws IOException {
		Pwtools database = new Pwtools(in);
		PwtoolsEntry entry;

		while ((entry = database.nextEntry()) != null) {
			Compound c = graph.createCompound(entry.getId());
			String name = StringUtils.join("; ", entry.get(new String[]{"Common-name", "SYNONYMS"}));
			c.setName(name);
			if (entry.hasTag("comment")) {
				c.setDescription(entry.getAsString("comment"));
			}

			// Search for mass
			if (entry.hasTag("MOLECULAR-WEIGHT")) {
				c.setMass(new Double(entry.getFirst("MOLECULAR-WEIGHT")).
						floatValue());
			}
			if (entry.hasTag("MONOISOTOPIC-MW")) {
				c.setMass(new Double(entry.getFirst("MONOISOTOPIC-MW")).
						floatValue());
			}

			// Formula and mass
			Formula formula = null;
			if (entry.hasTag("CHEMICAL-FORMULA")) {
				String formula_string = StringUtils.join("", entry.get("CHEMICAL-FORMULA"));
				formula_string = formula_string.replace("(", "").replace(")", "").
						replaceAll("\\s", "").replace(".", "");
				try {
					formula = Formula.createFormulaFromInChIString(formula_string);
				}
				catch (ChemicalElementUnkownException ex) {
					logger.warning("Can not calculate formula/mass: " + ex.
							getMessage());
				}
				catch (NumberFormatException ex) {
					logger.warning("Can not calculate formula/mass: " + ex.
							getMessage());
				}

			}
			if (entry.hasTag("inchi")) {
				try {
					formula = Formula.createFormulaFromInChIString(entry.
							getFirst("inchi"));
				}
				catch (ChemicalElementUnkownException ex) {
					logger.warning("Can not calculate formula/mass from InChI '" + entry.
							getFirst("inchi") + "': " + ex.getMessage());
				}
				catch (NumberFormatException ex) {
					logger.warning("Can not calculate formula/mass from InChI '" + entry.
							getFirst("inchi") + "': " + ex.getMessage());
				}

			}
			if (formula != null) {
				c.setFormula(formula.getAsString());
				c.setMass((float) formula.getMass());
			}

		}
	}

	private void parse_reactions(BufferedReader in) throws IOException {
		Pwtools database = new Pwtools(in);
		PwtoolsEntry entry;

		while ((entry = database.nextEntry()) != null) {
			Reaction r = graph.createReaction(entry.getId());

			if (entry.hasTag("comment")) {
				r.setDescription(entry.getAsString("comment"));
			}
			if (entry.hasTag("ec-number")) {
				r.setEcNumber(entry.getAsLine("ec-number"));
			}

			LinkedList<String> lefts = new LinkedList<String>();
			LinkedList<String> rights = new LinkedList<String>();

			if (entry.hasTag("left")) {
				for (String compound : entry.get("left")) {
					Compound c = graph.createCompound(compound);
					lefts.add(compound);
					graph.hasSubstrate(r, c);
				}
			}
			if (entry.hasTag("right")) {
				for (String compound : entry.get("right")) {
					Compound c = graph.createCompound(compound);
					rights.add(compound);
					graph.hasProduct(r, c);
				}
			}
			if (entry.hasTag("in-pathway")) {
				for (String pathway : entry.get("in-pathway")) {
					Pathway p = graph.createPathway(pathway);
					graph.happensIn(r, p);
				}
			}

			StringBuilder sb = new StringBuilder(250);
			if (!lefts.isEmpty()) {
				sb.append(lefts.getFirst());
				for (int idx = 1; idx < lefts.size(); idx++) {
					sb.append(" + ").append(lefts.get(idx));
				}
			}
			sb.append(" <=> ");
			if (!rights.isEmpty()) {
				sb.append(rights.getFirst());
				for (int idx = 1; idx < rights.size(); idx++) {
					sb.append(" + ").append(rights.get(idx));
				}
			}

			r.setEquation(sb.toString());
		}

	}

	private void parse_enzyme_reaction(BufferedReader in) throws IOException {
		Pwtools database = new Pwtools(in);
		PwtoolsEntry entry;

		int counter_fail_reactions = 0;
		int counter_fail_enzymes = 0;

		while ((entry = database.nextEntry()) != null) {
			String eid = entry.getFirst("enzyme");
			String rid = entry.getFirst("reaction");
			if (rid != null && eid != null) {

				Reaction r = graph.getReaction(rid);
				Enzyme e = graph.getEnzyme(eid);
				if (r == null || e == null) {
					if (r == null) {
						logger.warning("No such reaction in graph: " + rid);
						counter_fail_reactions++;
					}
					if (e == null) {
						logger.warning("No such enzyme in graph: " + eid);
						counter_fail_enzymes++;
					}
				}
				else {
					if (entry.hasTag("COMMON-NAME") || entry.hasTag("SYNONYMS")) {
						if (r.getName() != null) {
							r.setName(r.getName() + "; " + entry.getAsLine(new String[]{"common-name", "synonyms"}));
						}
						else {
							r.setName(entry.getAsLine(new String[]{"common-name", "synonyms"}));
						}
					}
					graph.needsEnzyme(r, graph.getEnzyme(eid));
				}
			}
		}

		if (counter_fail_enzymes > 0 || counter_fail_reactions > 0) {
			logger.severe("Could not find " + counter_fail_reactions + " reactions and " + counter_fail_enzymes + " enzymes");
		}

		// Adjust the EC Numbers
		for (Enzyme e : graph.getEnzymes()) {
			Collection<Object> ecs = new HashSet<Object>();
			for (Reaction r : graph.getReactions(e)) {
				if (r.getEcNumber() != null && !r.getEcNumber().isEmpty()) {
					ecs.add(r.getEcNumber());
				}
			}
			e.setEC( StringUtils.join("; ", ecs));
		}

	}

	private void parse_pathways(BufferedReader in) throws IOException {
		Pwtools database = new Pwtools(in);
		PwtoolsEntry entry;

		while ((entry = database.nextEntry()) != null) {
			Pathway p = graph.createPathway(entry.getId());
			p.setName(StringUtils.join("; ", entry.get(new String[]{"common-name", "synonyms"})));
			p.setDescription(entry.getAsString("comment"));

			for (String rid : entry.get("reaction-list")) {
				graph.happensIn(graph.createReaction(rid), p);
			}

		}
	}

	private void parse_genes(BufferedReader in) throws IOException {
		Pwtools database = new Pwtools(in);
		PwtoolsEntry entry;

		while ((entry = database.nextEntry()) != null) {
			Gene g = graph.createGene(entry.getId());
			g.setName(StringUtils.join("; ", entry.get(new String[]{"common-name", "synonyms"})));
			g.setDefinition(entry.getAsString("comment"));

			if (entry.hasTag("product")) {
				for (String eid : entry.get("product")) {
					graph.encodesFor(g, graph.createEnzyme(eid));
				}
			}

		}
	}

	private void parse_enzymes(BufferedReader in) throws IOException {
		Pwtools database = new Pwtools(in);
		PwtoolsEntry entry;

		while ((entry = database.nextEntry()) != null) {
			Enzyme e = graph.createEnzyme(entry.getId());
			e.setName(StringUtils.join("; ", entry.get(new String[]{"common-name", "synonyms"})));
			e.setDescription(entry.getAsString("comment"));

			if (entry.hasTag("gene")) {
				for (String eid : entry.get("gene")) {
					graph.encodesFor(graph.createGene(eid), e);
				}
			}

		}
	}
}