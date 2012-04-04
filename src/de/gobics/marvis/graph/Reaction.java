package de.gobics.marvis.graph;

/**
 * A reaction is the transition of one or more compounds into one or more other.
 *
 * @author manuel
 */
public class Reaction extends GraphObject {

	/**
	 * A human readable name for the reaction.
	 */
	String name = null;
	/**
	 * The reaction equation in a short form.
	 */
	String equation = null;
	/**
	 * A description of the reaction.
	 */
	String description = null;
	/**
	 * The Enzyme classification number of the reaction (if applicable)
	 */
	String ecnumber = null;

	/**
	 * It is better to construct reactions via {@link MetabolicNetwork#createReaction(java.lang.String)
	 * }.
	 *
	 * @param id
	 */
	public Reaction(String id) {
		super(id);
	}

	/**
	 * Set the description to the new value.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Return the description
	 *
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Set the name of the raction.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Set the equation short form
	 * @param equation the new equation
	 */
	public void setEquation(String equation) {
		this.equation = equation;
	}

	/**
	 * Returns the equation
	 * @return the equation
	 */
	public String getEquation() {
		return this.equation;
	}

	/**
	 * Returns the EC number
	 * @return the EC number
	 */
	public String getEcNumber() {
		return ecnumber;
	}

	/**
	 * Set the EC number
	 * @param ecnumber the new ec number
	 */
	public void setEcNumber(String ecnumber) {
		this.ecnumber = ecnumber;
	}
}
