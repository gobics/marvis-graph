package de.gobics.marvis.graph;

/**
 * An object class representing enzymes. In the term of MarVis-Graph an enzyme
 * is any type of protein that is somehow related to an reaction.
 *
 * @author Manuel Landesfeind <manuel@gobics.de>
 */
public class Enzyme extends GraphObject {

	/**
	 * A name for the enzyme.
	 */
	String name = null;
	/**
	 * A description for this enzyme.
	 */
	String description = null;
	/**
	 * The enzyme classification number of this enzyme.
	 */
	String ec = null;

	/**
	 * Create a new enzyme object with the given ID. New enzyme objects should
	 * be constructed using the factory pattern of the metabolic network:
	 * {@code network.createEnzyme(id_string)}
	 *
	 * @see de.gobics.marvis.graph.GraphObject
	 * @see de.gobics.marvis.graph.MetabolicNetwork
	 * @param id
	 */
	public Enzyme(String id) {
		super(id);
	}

	/**
	 * Set a description for this enzyme, e.g. describing the enzymatic
	 * activity.
	 *
	 * @param description new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the description
	 *
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Set the name of this enzyme.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the name if this enzyme.
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the enzyme classification number.
	 *
	 * @return the EC number
	 */
	public String getEC() {
		return ec;
	}

	/**
	 * Set the enzyme classification number for this enzymatic activity.
	 *
	 * @param ec the new EC number
	 */
	public void setEC(String ec) {
		this.ec = ec;
	}
}
