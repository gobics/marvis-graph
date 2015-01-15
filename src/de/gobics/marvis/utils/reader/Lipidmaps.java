/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.reader;

import de.gobics.marvis.utils.Metagroup;
import de.gobics.marvis.utils.Molecule;
import de.gobics.marvis.utils.StringUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class Lipidmaps extends BasicReader {

	private static final Logger logger = Logger.getLogger(Lipidmaps.class.getName());

	public Lipidmaps(File input_file) throws FileNotFoundException, IOException {
		super(input_file);
		setSeparator("\n$$$$\n");
	}

	public LipidmapsEntry nextEntry() throws IOException {
		String chunk = nextChunk();
		if (chunk == null) {
			return null;
		}
		return new LipidmapsEntry(chunk);

	}

	public Molecule nextMolecule(Metagroup metagroup) throws IOException {
		return createMolecule(nextEntry(), metagroup);
	}

	public Molecule createMolecule(LipidmapsEntry entry, Metagroup metagroup) {
		if (entry == null) {
			return null;
		}
		logger.finer("ID of entry is: "+entry.getId());
		if (entry.getId() == null || entry.getId().isEmpty()) {
			return null;
		}

		logger.finer("Parsing entry: " + entry.getId());

		// Chemical information
		Molecule m = new Molecule(entry.getId(), metagroup);
		m.setFormula(entry.getFirst("FORMULA"));
		if (entry.hasTag("EXACT_MASS")) {
			m.setMass(new Double(entry.getAsLine("EXACT_MASS")));
		}

		// Names
		String[] names = entry.get(new String[]{"COMMON_NAME", "SYNONYMS", "SYSTEMATIC_NAME"});
		if (names != null && names.length > 0) {
			m.setName(StringUtils.join("; ", names));
		}

		// Cross Database links
		if (entry.hasTag("PUBCHEM_SID")) {
			m.addDatabaseReference("pubchem", entry.getFirst("PUBCHEM_SID"));
		}
		if (entry.hasTag("CHEBI_ID")) {
			m.addDatabaseReference("chebi", entry.getFirst("CHEBI_ID"));
		}
		if (entry.hasTag("KEGG_ID")) {
			m.addDatabaseReference("kegg", entry.getFirst("KEGG_ID"));
		}
		if (entry.hasTag("HMDBID")) {
			m.addDatabaseReference("hmdb", entry.getFirst("HMDBID"));
		}
		if (entry.hasTag("LIPIDAT_ID")) {
			m.addDatabaseReference("lipidat", entry.getFirst("LIPIDAT_ID"));
		}
		if (entry.hasTag("LIPIDBANK_ID")) {
			m.addDatabaseReference("lipidbank", entry.getFirst("LIPIDBANK_ID"));
		}

		return m;
	}
}
