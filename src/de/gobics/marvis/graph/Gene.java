package de.gobics.marvis.graph;
/**
 * A gene is a sequence on the data that emits (due to transcription and translation
 * a {@link Enzyme}.
 * @author Manuel Landesfeind <manuel@gobics.de>
 */
public class Gene extends GraphObject {
	/**
	 * A common name of this gene.
	 */
	private String name = null;
	/**
	 * A description of the gene.
	 */
	private String definition = null;
	
	/**
	 * Create a new gene object. In general it is better to use the {@link MetabolicNetwork#createGene(java.lang.String) }
	 * method.
	 * @param id 
	 */
	public Gene(String id) {
		super(id);
	}
	
	
	/**
	 * Set the name of this gene.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the name if this gene.
	 *
	 * @see GraphObject
	 * @return the name
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Set a {@link #definition} for this object.
	 * @return 
	 */
	public void setDefinition(String definition){
		this.definition = definition;
	}/**
	 * Get the {@link #definition} of this object.
	 * @param definition 
	 */
	public String getDefinition(){
		return this.definition;
	}
	
}
