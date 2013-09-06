package de.gobics.marvis.graph;

/**
 * A compound is a molecule. These are used as substrates and products in
 * reactions.
 *
 * @author Manuel Landesfeind <manuel@gobics.de>
 */
public class Compound extends GraphObject {

	/**
	 * A name for this compound.
	 */
	String name = null;
	/**
	 * A description for this compound.
	 */
	String description = null;
	/**
	 * The chemical sum formula of this compound.
	 */
	String formula = null;
	/**
	 * The mass/monoisotopic weight of this compound. Defaults to zero.
	 */
	float mass = 0f;
	/**
	 * The INternational CHemincal Identifier of this compound {@link http://en.wikipedia.org/wiki/International_Chemical_Identifier}.
	 */
	String inchi = null;

	public Compound(String id) {
		super(id);
	}

	/**
	 * Set the name of this compound.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the common {@link #name} of this compound.
	 *
	 * @see GraphObject
	 * @return
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Set a text that describes this molecule. The text may contain any type of
	 * information.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the description of this molecule.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Set the monoisotopic weight of this molecule. If the given float is lower
	 * than zero the mass will be set to zero.
	 *
	 * @param mass the new mass
	 */
	public void setMass(float mass) {
		if (mass < 0) {
			mass = 0f;
		}
		this.mass = mass;
	}

	/**
	 * Returns the mass
	 *
	 * @return the mass
	 */
	public float getMass() {
		return this.mass;
	}

	/**
	 * Set the chemical formula of this molecule as a string.
	 *
	 * @param formula the new formula
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}

	/**
	 * Returns the chemical formula of this molecule.
	 *
	 * @return the chemical formula
	 */
	public String getFormula() {
		return this.formula;
	}

	/**
	 * Set the International Chemical Identifier (InChI) of this molecule. See
	 * {@link http://en.wikipedia.org/wiki/International_Chemical_Identifier}
	 *
	 * @param inchi the new InChI
	 */
	public void setInchi(String inchi) {
		this.inchi = inchi;
	}

	/**
	 * Returns the 
	 * <a href="http://en.wikipedia.org/wiki/International_Chemical_Identifier">InChI</a>.
	 *
	 * @return
	 */
	public String getInchi() {
		return inchi;
	}

	/**
	 * Returns true iff this molecule has an InChI set.
	 *
	 * @return true if this molecule has an InChI, false otherwise
	 */
	public boolean hasInchi() {
		return inchi != null && !inchi.isEmpty();
	}
}
