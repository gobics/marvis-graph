package de.gobics.marvis.graph;

/**
 * An InputObject is an abstract subclass of the {@link GraphObject}. An input
 * object is some entity that has been measured from experimental context. All
 * input object contain an {@link IntensityProfile} and a weight.
 *
 * @author Manuel Landesfeind <manuel@gobics.de>
 */
public abstract class InputObject extends GraphObject implements HasIntensity {

	/**
	 * The weight of this input object. The weight can be any type of double
	 * value and indicates some preprocessed value. It can be e.g. a p-Value.
	 */
	private double weight = 0.0;
	/**
	 * The intensity profile for this input object.
	 */
	private IntensityProfile intensities = new IntensityProfile();
	private String annotation_id = null;

	/**
	 * A string containing the ID of this object must be given to the super
	 * class.
	 *
	 * @see GraphObject
	 * @param id the id of this object.
	 */
	public InputObject(String id) {
		super(id);
	}

	/**
	 * Set the weight of this object. Weight has to be a double value from the
	 * interval 0 to 1 (both including).
	 *
	 * @param weight the new weight
	 */
	public void setWeight(Double weight) {
		if (weight == null || weight < 0) {
			weight = 0d;
		}
		if (weight > 1) {
			weight = 1d;
		}
		this.weight = weight;
	}

	/**
	 * Returns the weight
	 *
	 * @return the weight
	 */
	public Double getWeight() {
		return this.weight;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public IntensityProfile getIntensityProfile() {
		return intensities.clone();
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void setIntensityProfile(IntensityProfile profile) {
		intensities = profile;
	}

	/**
	 * {@link IntensityProfile#setIntensity(java.lang.String[], float[])}
	 */
	public final void setIntensity(String[] desired_condition_names, float[] data) {
		intensities.setIntensity(desired_condition_names, data);
	}

	/**
	 * {@link IntensityProfile#getRawIntensityNames() }
	 */
	public final String[] getRawIntensityNames() {
		return intensities.getRawIntensityNames();
	}

	/**
	 * {@link IntensityProfile#getRawIntensities() }
	 */
	public final float[] getRawIntensities() {
		return intensities.getRawIntensities();
	}

	/**
	 * {@link IntensityProfile#getRawIntensitiesAsFloat() }
	 */
	public final Float[] getRawIntensitiesAsFloat() {
		return intensities.getRawIntensitiesAsFloat();
	}

	public String getAnnotation() {
		return annotation_id;
	}

	public void setAnnotation(String annotation_id) {
		this.annotation_id = annotation_id;
	}

	public boolean hasAnnotation() {
		return annotation_id != null;
	}
}
