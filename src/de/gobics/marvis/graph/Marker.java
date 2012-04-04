package de.gobics.marvis.graph;

/**
 * A marker is a mass spectrometry measurement consisting of a retention time,
 * a mass and an intensity profile.
 * @author Manuel Landesfeind <manuel@gobics.de>
 */

public class Marker extends InputObject {
	
	/**
	 * The mass/weight obtained from mass spectrometry analysis
	 */
	Double mass = 0.0;
	/**
	 * The retention time obtained from chromatography
	 */
	float rt = -1;
	/**
	 * Possible additional data from the input file.
	 */
	String[] additional_data = null;
	
	/**
	 * Construct a new marker object. It is preferred to use the {@link MetabolicNetwork}
	 * to create markers:
	 * <pre>
	 *  Marker m = metabolic_network.createMarker(id_string);
	 * </pre>
	 * @param id the id of this marker object.
	 */
	public Marker(String id) {
		super(id);
	}
	
	/**
	 * Set the mass of this marker to the given on. The mass has to be a non negative
	 * value.
	 * @param mass the new mass
	 */
	public void setMass(Double mass) {
		if (mass != null && mass < 0)
			mass = 0D;
		this.mass = mass;
	}

	/**
	 * Return the mass.
	 * @return the mass
	 */
	public Double getMass() {
		return this.mass;
	}
	
	/**
	 * Set the retention time. If the a value lower than zero is supplied it is
	 * assumed that there is no retention time measurement and the internal variable
	 * is set to -1 instead.
	 * @param new_rt the new retention time
	 */
	public void setRetentionTime(float new_rt){
		if( new_rt < 0 )
			new_rt = -1;
		this.rt = new_rt;
	}

	/**
	 * Returns the retention time
	 * @return the retention time
	 */
	public float getRetentionTime(){
		return rt;
	}

	/**
	 * Set additional data
	 * @param ad 
	 */
	public void setAdditionalData(String[] ad){
		this.additional_data = ad;
	}
	/**
	 * Returns the additional data
	 * @return 
	 */
	public String[] getAdditionalData(){
		return additional_data;
	}
}
