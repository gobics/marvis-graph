package de.gobics.marvis.utils.reader;

import de.gobics.marvis.utils.Metagroup;
import de.gobics.marvis.utils.Molecule;
import de.gobics.marvis.utils.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Read a attribute-value files exported by Pathway Tools and return the results
 * as {@link PwtoolsEntry}. A helper method returning the entry as molecule is
 * available
 *
 * @author manuel
 */
public class Pwtools extends BasicReader {

	private static final Logger logger = Logger.getLogger(Pwtools.class.getName());

	public Pwtools(File f) throws FileNotFoundException, IOException {
		super(f, "\n//\n");
	}

	public Pwtools(BufferedReader in) {
		super(in, "\n//\n");
	}

	public PwtoolsEntry nextEntry() throws IOException {
		String chunk = nextChunk();
		if (chunk == null) {
			return null;
		}
		return new PwtoolsEntry(chunk);

	}

	public Molecule nextMolecule(Metagroup metagroup) throws IOException {
		PwtoolsEntry entry = nextEntry();
		if (entry == null) {
			return null;
		}

		if (entry.getId() == null) {
			return null;
		}
		Molecule molecule = new Molecule(entry.getId(), metagroup);
		molecule.setInchi(entry.getAsLine("INCHI"));
		molecule.setSmiles(entry.getAsLine("SMILES"));
		if (entry.hasTag("MOLECULAR-WEIGHT")) {
			molecule.setMass(new Double(entry.getFirst("MOLECULAR-WEIGHT")));
		}

		// Names
		molecule.setName(
				StringUtils.join_unique("; ",
				entry.get(new String[]{"COMMON-NAME", "SYNONYMS"})));

		// Formula
		StringBuilder sb = new StringBuilder("");

		if (entry.hasTag("CHEMICAL-FORMULA")) {
			for (String c : entry.get("CHEMICAL-FORMULA")) {
				// Remove parentheses
				c = c.substring(1, c.indexOf(')'));
				String[] tokens = c.split("\\s+");
				if (tokens[0].length() > 1) {
					tokens[0] = tokens[0].substring(0, 1).toUpperCase() + tokens[0].
							substring(1).toLowerCase();
				}
				sb.append(tokens[0]).append(tokens[1]).append(" ");
			}
		}
		molecule.setFormula(sb.toString().trim());

		// Cross database references
		if (entry.hasTag("DBLINKS")) {
			for (String content : entry.get("DBLINKS")) {
				String[] tokens = content.substring(1, content.indexOf(')')).
						split("\\s+");
				String dbid = tokens[0].toLowerCase();
				String mid = tokens[1].replaceAll("\"", "");

				if (dbid.equals("ligand-cpd")) {
					dbid = "kegg";
				}

				logger.info("Adding cross database reference: " + dbid + "/" + mid);

				molecule.addDatabaseReference(dbid, mid);
			}
		}


		return molecule;
	}
}
