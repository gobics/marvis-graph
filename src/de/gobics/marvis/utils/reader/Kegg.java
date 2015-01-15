/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.reader;

import de.gobics.marvis.utils.*;
import java.io.*;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class Kegg extends BasicReader {

	private static final Logger logger = Logger.getLogger(Kegg.class.getName());

	public Kegg(File f) throws FileNotFoundException, IOException {
		super(f);
		setSeparator("\n///\n");
	}

	public KeggEntry nextEntry() throws IOException {
		String chunk = nextChunk();
		if (chunk == null || chunk.isEmpty()) {
			return null;
		}
		return new KeggEntry(chunk);
	}

	public Molecule nextMolecule(Metagroup mg) throws IOException {
		KeggEntry entry = nextEntry();
		if (entry == null) {
			return null;
		}

		return parseMolecule(mg, entry);
	}

	public static Molecule parseMolecule(Metagroup mg, KeggEntry entry){
		String id = entry.getId();
		if (id == null || id.isEmpty()) {
			logger.warning("Can not determine ID in chunk: " + entry);
			return null;
		}

		Molecule m = new Molecule(id, mg);
		m.setName(entry.getAsLine("NAME"));
		m.setFormula(entry.getAsLine("FORMULA"));
		double mass = -1;
		try {
			// Split line as it may contain additional string, e.g. "203.2 (PP-Dol)"
			mass = new Double(entry.getAsLine("MASS").split("\\s")[0]);
		} catch (Exception ex) {
			logger.warning("Can not get double from '" + entry.get("MASS") + "': " + ex);
		}
		m.setMass(mass);
		m.setDescription(entry.getAsString("COMMENT"));


		if (entry.hasTag("DBLINKS")) {
			for (String c : entry.get("DBLINKS")) {
				String[] token = c.split(": ");
				if (token.length == 2) {
					m.addDatabaseReference(token[0], token[1]);
				}
			}
		}

		return m;
	}
}
