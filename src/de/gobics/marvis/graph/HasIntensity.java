package de.gobics.marvis.graph;
/**
 * An interface that indicates that this implementing class has an intensity profile.
 * @author manuel
 */
public interface HasIntensity {
	/**
	 * Set the intensity profile to the given one.
	 * @see IntensityProfile
	 * @param ip the new intensity profile
	 */
	public void setIntensityProfile(IntensityProfile ip);
	/**
	 * Get the intensity profile.
	 * @see IntensityProfile
	 * @return the intensity profile
	 */
	public IntensityProfile getIntensityProfile();
}
