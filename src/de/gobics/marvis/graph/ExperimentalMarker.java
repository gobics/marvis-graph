package de.gobics.marvis.graph;

/**
 * An {@link ExperimentalMarker} is an abstract subclass of the
 * {@link GraphObject}. An experimental marker is some entity that has been
 * measured in experimental context. All marker contain an
 * {@link IntensityProfile} and a score.
 *
 * @author Manuel Landesfeind <manuel@gobics.de>
 */
public abstract class ExperimentalMarker extends GraphObject implements HasIntensity {

	/**
	 * The score of this input object. The score can be any type of double value
	 * and indicates some preprocessed value. It can be e.g.
	 * <code>1 - p-Value</code>.
	 */
	private double score = 1d;
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
	public ExperimentalMarker(String id) {
		super(id);
	}

	/**
	 * Set the score of this object. Score has to be a double value greater than
	 * 0. The higher the score is the more qualitative or important this
	 * experimental marker is.
	 *
	 * @param new_score the new score
	 */
	public void setScore(Double new_score) {
		if (new_score == null || new_score < 0) {
			new_score = 0d;
		}
		this.score = new_score;
	}

	/**
	 * Returns the score
	 *
	 * @return the score
	 */
	public Double getScore() {
		return this.score;
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
