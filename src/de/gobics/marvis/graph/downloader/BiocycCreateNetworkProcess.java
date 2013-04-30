package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.utils.ArrayUtils;
import de.gobics.marvis.utils.Formula;
import de.gobics.marvis.utils.StringUtils;
import de.gobics.marvis.utils.exception.ChemicalElementUnkownException;
import de.gobics.marvis.utils.reader.Pwtools;
import de.gobics.marvis.utils.reader.PwtoolsEntry;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 *
 * @author manuel
 */
public class BiocycCreateNetworkProcess extends AbstractNetworkCreator {

	/**
	 * Specifies the way metabolite-classes should be handled.
	 */
	public enum ClassesStategy {

		RemoveClasses, AsMetabolite, SingleReaction, ReactionVariants
	}
	private static final Logger logger = Logger.getLogger(BiocycCreateNetworkProcess.class.
			getName());
	private MetabolicNetwork graph = null;
	private File input_file;
	private ClassNode classes_tree = new ClassNode();
	private TreeMap<String, Collection<Reaction>> reactions = new TreeMap<>();
	private static final String base_url = "http://metacyc.org/META/NEW-IMAGE?object=";
	private static final ArrayUtils.MapPredicate go_to_id = new ArrayUtils.MapPredicate() {
		@Override
		public Object map(int index, Object obj) {
			return ((GraphObject) obj).getId();
		}
	};
	private ClassesStategy strategy = ClassesStategy.SingleReaction;

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

	public void setClassesStrategy(ClassesStategy s) {
		strategy = s;
	}

	@Override
	public MetabolicNetwork doTask() throws Exception {
		// Initialize variables for a new run
		graph = new MetabolicNetwork();
		graph.setName("Biocyc");


		if (input_file.isDirectory()) {
			parse_directory(input_file);
		}
		else if (input_file.isFile()) {
			parse_file(input_file);
		}
		logger.finer("Created network with " + graph.size() + " entities");
		return graph;
	}

	private void parse_file(File input_file) throws IOException {
		setProgressMax(8);
		setProgress(0);
		parse_classes(get_reader_for_tgz(input_file, "data/classes.dat"));
		incrementProgress();
		parse_compounds(get_reader_for_tgz(input_file, "data/compounds.dat"));

		//System.out.println(classes_tree.toString());

		incrementProgress();
		parse_reactions(get_reader_for_tgz(input_file, "data/reactions.dat"));
		incrementProgress();
		parse_enzymes(get_reader_for_tgz(input_file, "data/proteins.dat"));
		incrementProgress();
		parse_pathways(get_reader_for_tgz(input_file, "data/pathways.dat"));
		incrementProgress();
		parse_genes(get_reader_for_tgz(input_file, "data/genes.dat"));
		incrementProgress();
		parse_enzyme_reaction(get_reader_for_tgz(input_file, "data/enzrxns.dat"));
		incrementProgress();

		set_urls();
		incrementProgress();

	}

	private BufferedReader get_reader_for_tgz(File input_file, String entry_name) throws IOException {
		InputStream instream;
		String filename = input_file.getName().toLowerCase();
		if (filename.endsWith(".tgz") || filename.endsWith(".tar.gz")) {
			instream = new GZIPInputStream(new FileInputStream(input_file));
		}
		else if (filename.endsWith(".tar")) {
			instream = new FileInputStream(input_file);
		}
		else {
			throw new IOException("Can not handle file with unkown extension: " + filename);
		}

		TarArchiveInputStream tar = new TarArchiveInputStream(instream);
		TarArchiveEntry entry = null;
		while ((entry = tar.getNextTarEntry()) != null) {
			if (entry.isDirectory()) {
				continue;
			}
			String name = entry.getName();
			BufferedReader in;
			if (name.endsWith(entry_name)) {
				return new BufferedReader(new InputStreamReader(tar));
			}
		}

		throw new IOException("Can not find: " + entry_name);
	}

	private void parse_directory(File directory) throws FileNotFoundException, IOException {
		File data_directory = new File(directory.getAbsolutePath() + File.separator + "data");
		File data_file;

		setProgressMax(8);

		if (!data_directory.exists()) {
			throw new RuntimeException("Data directory does not exist: " + directory.
					getAbsolutePath() + File.separator + "data");
		}

		// Parse classes
		data_file = new File(data_directory + File.separator + "classes.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find classes file: " + data_file.
					getAbsolutePath());
		}
		parse_classes(new BufferedReader(new FileReader(data_file)));
		incrementProgress();

		// Parse compounds
		data_file = new File(data_directory + File.separator + "compounds.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find compound file: " + data_file.
					getAbsolutePath());
		}
		parse_compounds(new BufferedReader(new FileReader(data_file)));
		incrementProgress();

		// Parse reactions
		data_file = new File(data_directory + File.separator + "reactions.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find reaction file: " + data_file.
					getAbsolutePath());
		}
		parse_reactions(new BufferedReader(new FileReader(data_file)));
		incrementProgress();

		// Parse enzymes/proteins
		data_file = new File(data_directory + File.separator + "proteins.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find enzyme file: " + data_file.
					getAbsolutePath());
		}
		parse_enzymes(new BufferedReader(new FileReader(data_file)));
		incrementProgress();

		// Parse pathways
		data_file = new File(data_directory + File.separator + "pathways.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find pathway file: " + data_file.
					getAbsolutePath());
		}
		parse_pathways(new BufferedReader(new FileReader(data_file)));
		incrementProgress();

		// Parse reactions
		data_file = new File(data_directory + File.separator + "genes.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find gene file: " + data_file.
					getAbsolutePath());
		}
		parse_genes(new BufferedReader(new FileReader(data_file)));
		incrementProgress();

		// Parse links between enzymes and reactions
		data_file = new File(data_directory + File.separator + "enzrxns.dat");
		if (!data_file.exists()) {
			throw new RuntimeException("Can not find enzyme-reaction file: " + data_file.
					getAbsolutePath());
		}
		parse_enzyme_reaction(new BufferedReader(new FileReader(data_file)));
		incrementProgress();

		set_urls();
		incrementProgress();
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

			if (entry.hasTag("TYPES")) {
				for (String class_id : entry.get("TYPES")) {
					class_id = class_id.replaceAll("\\|", "");
					if (!classes_tree.findAndAddInstance(class_id, c.getId())) {
						logger.warning("Can not find class: " + class_id);
					}
				}
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
		in.close();
	}

	private void parse_reactions(BufferedReader in) throws IOException {
		Pwtools database = new Pwtools(in);
		PwtoolsEntry entry;

		while ((entry = database.nextEntry()) != null) {
			String id = entry.getFirst("unique-id");
			String description = entry.hasTag("comment") ? entry.getAsString("comment") : null;
			String ec = entry.hasTag("ec-number") ? entry.getAsString("ec-number") : null;

			Collection<Reaction> reactions = new LinkedList<>();
			String[] left_reactants = entry.get("left");
			String[] right_reactants = entry.get("right");
			if (left_reactants == null) {
				left_reactants = new String[0];
			}
			if (right_reactants == null) {
				right_reactants = new String[0];
			}

			if (strategy.equals(ClassesStategy.AsMetabolite)) {
				Reaction r = graph.createReaction(id);
				for (String left : left_reactants) {
					graph.hasSubstrate(r, graph.createCompound(left));
				}
				for (String right : right_reactants) {
					graph.hasProduct(r, graph.createCompound(right));
				}
				reactions.add(r);
			}
			else if (strategy.equals(ClassesStategy.RemoveClasses)) {
				Reaction r = graph.createReaction(id);
				for (String left : left_reactants) {
					if (classes_tree.findClass(left) == null) { // is not a class
						graph.hasSubstrate(r, graph.createCompound(left));
					}
				}
				for (String right : right_reactants) {
					if (classes_tree.findClass(right) == null) { // is not a class
						graph.hasProduct(r, graph.createCompound(right));
					}
				}
				reactions.add(r);
			}
			else if (strategy.equals(ClassesStategy.SingleReaction)) {
				Reaction r = graph.createReaction(id);

				for (String left : left_reactants) {
					for (String left_instance : get_instances(left)) {
						graph.hasSubstrate(r, graph.createCompound(left_instance));
					}
				}
				for (String right : right_reactants) {
					for (String right_instance : get_instances(right)) {
						graph.hasProduct(r, graph.createCompound(right_instance));
					}
				}

				reactions.add(r);
			}
			else if (strategy.equals(ClassesStategy.ReactionVariants)) {
				int reaction_counter = 1;
				for (Collection<String> left_variants : parse_reactions_multiplex(get_instances(left_reactants))) {
					for (Collection<String> right_variants : parse_reactions_multiplex(get_instances(right_reactants))) {
						Reaction r = graph.createReaction(id + "-variant-" + reaction_counter);
						reaction_counter++;

						for (String cid : left_variants) {
							graph.hasSubstrate(r, graph.createCompound(cid));
						}
						for (String cid : right_variants) {
							graph.hasProduct(r, graph.createCompound(cid));
						}
						reactions.add(r);
					}
				}
			}

			this.reactions.put(id, reactions);

			for (Reaction r : reactions) {
				r.setDescription(description);
				r.setEcNumber(ec);
				r.setUrl(base_url + id);

				if (entry.hasTag("in-pathway")) {
					for (String pathway : entry.get("in-pathway")) {
						Pathway p = graph.createPathway(pathway);
						graph.happensIn(r, p);
					}
				}

				String eq_left = StringUtils.join(" + ", ArrayUtils.map(go_to_id, graph.getSubstrates(r)));
				String eq_right = StringUtils.join(" + ", ArrayUtils.map(go_to_id, graph.getProducts(r)));

				r.setEquation(eq_left + " <=> " + eq_right);
			}
		}

		in.close();
	}

	/**
	 * Calculates the possible reaction variants
	 *
	 * @param reactants
	 * @return
	 */
	public List<Collection<String>> parse_reactions_multiplex(List<Collection<String>> reactants) {
		// If only one reactions shall be created for 
		if (strategy.equals(ClassesStategy.SingleReaction)) {
			LinkedList<String> result = new LinkedList<>();
			for (Collection<String> variants : reactants) {
				result.addAll(variants);
			}
			List<Collection<String>> full_result = new LinkedList<>();
			full_result.add(result);
			return full_result;
		}


		// Result list
		// list in this collection will be appended with another reactant
		List<Collection<String>> result = new LinkedList<>();
		// Add empty list as initial "reactants"
		result.add(new ArrayList<String>(0));

		for (Collection<String> next_reactants : reactants) {
			List<Collection<String>> next_result = new LinkedList<>();

			for (String reactant : next_reactants) {
				for (Collection<String> former_reactants : result) {
					List<String> new_result = new ArrayList<>(former_reactants.size() + 1);
					new_result.addAll(former_reactants);
					new_result.add(reactant);
					next_result.add(new_result);
				}
			}

			result = next_result;
		}
		return result;
	}

	private Set<String> get_instances(String id) {
		ClassNode c = classes_tree.findClass(id.replaceAll("\\|", ""));
		if (c != null) {
			return c.getAllInstances();
		}
		Set<String> instance = new TreeSet<>();
		instance.add(id);
		return instance;
	}

	private List<Collection<String>> get_instances(String[] strings) {
		if (strings == null || strings.length == 0) {
			return new ArrayList<>(0);
		}
		List<Collection<String>> instances = new ArrayList<>(strings.length);
		for (String id : strings) {
			if (!strategy.equals(ClassesStategy.RemoveClasses)) {
				instances.add(get_instances(id));
			}
		}
		return instances;
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


				Enzyme e = graph.getEnzyme(eid);

				if (e == null) {
					logger.warning("No such enzyme in graph: " + eid);
					counter_fail_enzymes++;
				}
				else if (!reactions.containsKey(rid)) {
					logger.warning("No such reaction in graph: " + rid);
					counter_fail_reactions++;
				}
				else {
					for (Reaction r : reactions.get(rid)) {
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
			e.setEC(StringUtils.join("; ", ecs));
		}
		in.close();

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
		in.close();
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
		in.close();
	}

	private void set_urls() {
		for (GraphObject obj : graph.getAllObjects()) {
			if (!obj.hasUrl()) {
				obj.setUrl(base_url + obj.getId());
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
		in.close();
	}

	private void parse_classes(BufferedReader in) throws IOException {
		Pwtools database = new Pwtools(in);
		PwtoolsEntry entry;

		while ((entry = database.nextEntry()) != null) {
			String id = entry.getFirst("UNIQUE-ID");
			String parent = entry.getFirst("TYPES");
			ClassNode newclass = new ClassNode(id);

			if (!classes_tree.findAndAddClass(parent, newclass)) {
				classes_tree.addClass(newclass);
			}
		}

		in.close();
	}

	private static class ClassNode implements Comparable<ClassNode> {

		public final String id;
		private final Set<ClassNode> childnodes = new TreeSet<>();
		private final Set<String> instances = new TreeSet<>();

		public ClassNode() {
			this(null);
		}

		public ClassNode(String id) {
			if (id != null) {
				if (id.startsWith("|")) {
					id = id.substring(1);
				}
				if (id.endsWith("|")) {
					id = id.substring(0, id.length() - 1);
				}
			}
			this.id = id;
		}

		public void addClass(ClassNode node) {
			childnodes.add(node);
		}

		public Set<String> getAllInstances() {
			Set<String> all_instances = new TreeSet<>();
			Iterator<ClassNode> iter = iterAllClasses();
			while (iter.hasNext()) {
				all_instances.addAll(iter.next().instances);
			}
			return all_instances;
		}

		public boolean findAndAddClass(String id, ClassNode node) {
			ClassNode n = findClass(id);
			if (n != null) {
				n.addClass(node);
				return true;
			}
			return false;
		}

		public ClassNode findClass(String class_id) {
			if (class_id.startsWith("|")) {
				class_id = class_id.substring(1);
			}
			if (class_id.endsWith("|")) {
				class_id = class_id.substring(0, class_id.length() - 1);
			}
			if (this.id != null && this.id.equals(class_id)) {
				return this;
			}
			for (ClassNode c : childnodes) {
				ClassNode found = c.findClass(class_id);
				if (found != null) {
					return found;
				}
			}
			return null;
		}

		public boolean findAndAddInstance(String class_id, String instance_id) {
			ClassNode n = findClass(class_id);
			if (n != null) {
				n.addInstance(instance_id);
				return true;
			}
			return false;
		}

		@Override
		public int compareTo(ClassNode other) {
			return id.compareTo(other.id);
		}

		private void addInstance(String instance_id) {
			instances.add(instance_id);
		}

		private Iterator<ClassNode> iterAllClasses() {
			final TreeSet<ClassNode> current = new TreeSet<>();
			current.add(this);

			return new Iterator<ClassNode>() {
				private Set<ClassNode> found = current;

				@Override
				public boolean hasNext() {
					return !found.isEmpty();
				}

				@Override
				public ClassNode next() {
					ClassNode next = found.iterator().next();
					found.remove(next);
					found.addAll(next.childnodes);
					return next;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Not supported yet.");
				}
			};
		}

		@Override
		public String toString() {
			return toString("");
		}

		public String toString(String indent) {
			StringBuilder sb = new StringBuilder(indent).append(id != null ? id : "NULL").append(":");
			for (String inst : instances) {
				sb.append(" ").append(inst);
			}
			sb.append("\n");
			for (ClassNode child_node : childnodes) {
				sb.append(indent).append(child_node.toString(indent + " "));
			}
			return sb.toString();
		}
	}
}