package de.gobics.marvis.graph;
/**
 * A pathway is a set of (usually connected) reactions.
 * 
 * @see Reaction
 * @author manuel
 */
public class Pathway extends GraphObject {
	/**
	 * A human readable name of the pathway.
	 */
	String name = null;
	/**
	 * A general description of the pathway.
	 */
	String description = null;
	
	/**
	 * Construction of a new pathway is better done via {@link MetabolicNetwork#createPathway(java.lang.String)}
	 * @param id 
	 */
	public Pathway( String id) {
		super(id);
	}
	
	/**
	 * {@inheritDoc }
	 */
	@Override
	public String getName() {
	return this.name;
	}
	/**
	 * Set the name of the pathway.
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/** 
	 * Set the description of the pathway.
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Get the description of the pathways
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}
	
}
