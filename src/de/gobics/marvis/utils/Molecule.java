/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils;

import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author manuel
 */
public class Molecule implements Comparable<Molecule> {

	private static final Logger logger = Logger.getLogger(Molecule.class.getName());
	private static final Pattern pattern_inchi_charge = Pattern.compile("InChI=1S?/(.+)/p([-+])(\\d+)(/.*)?");
	private static final Pattern pattern_inchi_formula = Pattern.compile("InChI=1S?/([^/]+)(/.*)?");
	/**
	 * The ID of this molecule
	 */
	private final String id;
	private Metagroup metagroup;
	private String name = null;
	private String description = null;
	private String formula = null;
	private double formula_mass = -1;
	/** The SMILES {@link http://en.wikipedia.org/wiki/SMILES}
	 */
	private String smiles = null;
	/** The InChI {@link http://en.wikipedia.org/wiki/International_Chemical_Identifier}
	 */
	private String inchi = null;
	private String formula_curated = null;
	private double formula_curated_mass = -1;

	private final TreeMap<ReferenceDatabase, String> database_xref = new TreeMap<ReferenceDatabase, String>();

	public Molecule(String id, Metagroup source) {
		if (id == null) {
			throw new NullPointerException("Id can not be null");
		}
		if (source == null) {
			throw new NullPointerException("Metagroup can not be null");
		}
		this.id = id;
		this.metagroup = source;
	}

	public String getId() {
		return id;
	}

	public Metagroup getMetagroup() {
		return metagroup;
	}

	public void setMetagroup(Metagroup mg) {
		if (mg == null) {
			throw new RuntimeException("Metagroup is null");
		}
		metagroup = mg;
	}

	public String getIdFull() {
		return metagroup.getReferenceDatabase().getId() + "/" + getId();
	}

	public void setFormula(String fstring) {
		try {
			fstring = Formula.createFormulaFromString(fstring).getAsString();
		} catch (Exception e) {
			// ignore
		}
		this.formula = fstring;
	}

	public void setFormula(Formula f) {
		this.formula = f.getAsString();
	}

	public void setMass(double m) {
		this.formula_mass = m > 0 ? m : -1;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getMass() {
		return this.formula_mass;
	}

	public void setMassCurated(double m) {
		this.formula_curated_mass = m > 0 ? m : -1;
	}

	public double getMassCurated() {
		return this.formula_curated_mass;
	}

	public void setFormulaCurated(String fstring) {
		if (fstring == null || fstring.equals(formula)) {
			formula_curated = null;
			formula_curated_mass = -1;
		} else {
			formula_curated = fstring;
		}
	}

	public void setFormulaCurated(Formula f) {
		if (f == null) {
			setFormulaCurated((String) null);
		} else {
			formula_curated_mass = f.getMass();
			// Call string variant to check if the formula differs from the original
			setFormulaCurated(f.getAsString());
		}
	}

	public String getFormulaCurated() {
		return formula_curated;
	}

	public Formula getFormulaCuratedAsObject() {
		if (formula_curated == null) {
			return null;
		}
		try {
			return Formula.createFormulaFromString(formula_curated);
		} catch (Exception e) {
			logger.finer("Can not parse formula string of '" + getIdFull() + "': " + formula_curated);
		}
		return null;
	}

	public boolean isFormulaCurated() {
		return formula_curated != null && !formula_curated.isEmpty();
	}

	public String getFormula() {
		return formula;
	}

	public Formula getFormulaAsObject() {
		try {
			return Formula.createFormulaFromString(formula);
		} catch (Exception e) {
			logger.finer("Can not parse formula string of '" + this + "': " + e.getMessage());
		}
		return null;
	}

	public ReferenceDatabase getReferenceDatabase() {
		return metagroup.getReferenceDatabase();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Molecule
				&& getIdFull().equals(((Molecule) o).getIdFull());
	}

	public int compareTo(Molecule o) {
		return getIdFull().compareTo(o.getIdFull());
	}

	public String getUrl() {
		return metagroup.getReferenceDatabase().getUrlCompound().replace("$ID", getId());
	}

	public String getInchi() {
		return inchi;
	}

	public String getName() {
		return name != null ? name : getId();
	}

	public String getSmiles() {
		return smiles;
	}

	public void setInchi(String inchi) {
		this.inchi = inchi;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+"{id=" + getIdFull() + "; formula=" + getFormula() + "; curated_formula=" + getFormulaCurated() + "}";
	}

	/**
	 * Tries to determine the charge by checking the InChI string and returns it.
	 * @return
	 */
	public Integer getInchiCharge() {
		if (getInchi() == null) {
			return null;
		}

		Matcher m = pattern_inchi_charge.matcher(getInchi());
		if (!m.matches()) {
			return 0;
		}
		if (m.group(1).contains(".")) {
			logger.warning("Can not parse formula with sub-components: " + m.group(1));
			return 0;
		}
		int multiplier = m.group(2).equals("-") ? -1 : 1;
		return multiplier * Integer.parseInt(m.group(3));
	}

	/**
	 * If the molecule contains an InChI the formula defined by that InChI will
	 * be returned.
	 * @return String containing the sum formula 
	 */
	public String getInchiFormula() {
		if (getInchi() == null) {
			return null;
		}

		Matcher m = pattern_inchi_formula.matcher(getInchi());
		if (!m.matches()) {
			return null;
		}
		return m.group(1);
	}

	/**
	 * Tries to return the formula defined in the InChI as Formula object. If
	 * no InChI is given or the formula can not be parsed {@code null} will be
	 * returned.
	 * @return Formula object if the InChi can be parsed
	 */
	public Formula getInchiFormulaObject() {
		String fstring = getInchiFormula();
		if (fstring == null) {
			return null;
		}
		Formula formula = null;
		try {
			formula = Formula.createFormulaFromString(fstring);
		} catch (Exception e) {
			// Ignore
		}
		return formula;
	}

	/**
	 * Returns true if the molecule has an InChI that can be parsed and the current
	 * mass equals the mass of the formula of the InChI.
	 * @return 
	 */
	public boolean massIsInchiCurated(){
		Formula f = getInchiFormulaObject();
		if( f == null )
			return false;
		return getMass() >= f.getMass()-0.000001
				&& getMass() <= f.getMass()+0.000001;
	}

	/** Add a cross database reference for this molecule
	 * 
	 * @param The database this entry links to
	 * @param The id of the molecule in the other reference database
	 */
	public void addDatabaseReference(ReferenceDatabase rdb, String remote_id){
		if( rdb.equals(getReferenceDatabase()) && remote_id.equals(getId()))
			return;
		database_xref.put(rdb, remote_id);
	}
	/** Add a cross database reference for this molecule
	 *
	 * @param The database this entry links to
	 * @param The id of the molecule in the other reference database
	 */
	public void addDatabaseReference(String db_id, String remote_id){
		addDatabaseReference(new ReferenceDatabase(db_id), remote_id);
	}

	/** Returns the id of this molecule in the reference database {@code db_id}.
	 * @param db_id
	 * @return The remote id or null if not known
	 */
	public String getDatabaseReference(String db_id){
		return getDatabaseReference(new ReferenceDatabase(db_id));
	}

	/** Returns the id of this molecule in the reference database {@code xdb}.
	 * @param xdb
	 * @return The remote id or null if not known
	 */
	public String getDatabaseReference(ReferenceDatabase xdb) {
		return database_xref.get(xdb);
	}

	public ReferenceDatabase[] getCrossDatabaseReferenceDatabases(){
		return database_xref.keySet()
				.toArray(new ReferenceDatabase[database_xref.size()]);
	}
}
