package de.gobics.marvis.graph;

public abstract class InputObject extends GraphObject implements HasIntensity {

	private static final long serialVersionUID = 1L;
	private double weight = 0.0;
	private IntensityProfile intensities = new IntensityProfile();

	public InputObject(String id) {
		super(id);
	}

	public void setWeight(Double weight) {
		if (weight == null || weight < 0) {
			weight = 0d;
		}
		if ( weight > 1 ) {
			weight = 1d;
		}
		this.weight = weight;
	}

	public Double getWeight() {
		return this.weight;
	}

	@Override
	public IntensityProfile getIntensityProfile(){
		return intensities.clone();
	}
	
	@Override
	public void setIntensityProfile(IntensityProfile profile){
		intensities = profile;
	}

	public final void setIntensity(String[] desired_condition_names, float[] data) {
		intensities.setIntensity(desired_condition_names, data);
	}

	public final String[] getRawIntensityNames() {
		return intensities.getRawIntensityNames();
	}

	public final float[] getRawIntensities() {
		return intensities.getRawIntensities();
	}

	public final Float[] getRawIntensitiesAsFloat() {
		return intensities.getRawIntensitiesAsFloat();
	}
}
