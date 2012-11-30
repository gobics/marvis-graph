package de.gobics.marvis.graph;

import java.util.*;

/**
 * An intensity profile consists of a mapping of "condition names" and their
 * corresponding abundances. These abundances may have been measured e.g. by
 * mass spectrometry.
 *
 * The data is stored in two arrays of equal length. On array contains the
 * condition names and the other the abundances. Note that a condition name may
 * occur several times if there have been several sample measured in the same
 * condition.
 *
 * @author manuel
 */
public class IntensityProfile implements Cloneable {

	/**
	 * Contains the names of the intensities.This array has a length equal to
	 * the length of {@link #intensity_profile}
	 */
	private String[] intensity_names = new String[0];
	/**
	 * Contains the corresponding intensity abundances. This array has a length
	 * equal to the length of {@link #intensity_names}
	 */
	private float[] intensity_profile = new float[0];

	/**
	 * Set the new condition names and intensities of this intensity profile.
	 * The given arrays must have equal length.
	 *
	 * @param desired_condition_names
	 * @param data
	 */
	public void setIntensity(String[] desired_condition_names, float[] data) {
		if (desired_condition_names.length != data.length) {
			throw new IllegalArgumentException("Conditions names and data array are not of equal length");
		}
		sortAndSetIntensities(desired_condition_names, data);
		//intensity_names = desired_condition_names;
		//intensity_profile = data;
	}

	/**
	 * Returns an array containing the raw intensity names. The returned data is
	 * a clone of the {@link #intensity_names}.
	 *
	 * @return clone of the {@link #intensity_names}
	 */
	public String[] getRawIntensityNames() {
		return intensity_names.clone();
	}

	/**
	 * Returns an array containing the raw intensities.
	 *
	 * @return clone of the {@link #intensity_profile}
	 */
	public float[] getRawIntensities() {
		return intensity_profile.clone();
	}

	public float[] getRawIntensities(String condition_name) {
		LinkedList<Float> values = new LinkedList<Float>();
		for (int idx = 0; idx < intensity_names.length; idx++) {
			if (intensity_names[idx].equals(condition_name)) {
				values.add(intensity_profile[idx]);
			}
		}
		float[] floats = new float[values.size()];
		for (int idx = 0; idx < floats.length; idx++) {
			floats[idx] = values.get(idx);
		}
		return floats;
	}

	/**
	 * Similar to the {@link #getRawIntensities()} method but returns an Float
	 * object array.
	 *
	 * @return the intensities as Float objects
	 */
	public Float[] getRawIntensitiesAsFloat() {
		Float[] ints = new Float[intensity_profile.length];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = (Float) intensity_profile[i];
		}
		return ints;
	}

	/**
	 * Returns the intensities that have been normalized by the {@link #normalize(float[])}
	 * method.
	 *
	 * @return the normalized values
	 */
	public float[] getNormalizedIntensities() {
		return normalize(intensity_profile);
	}

	/**
	 * Returns a array of distinct condition names.
	 *
	 * @return distinct condition names in this profile.
	 */
	public String[] getConditionNames() {
		LinkedList<String> names = new LinkedList<String>();
		for (String n : intensity_names) {
			if (!names.contains(n)) {
				names.add(n);
			}
		}

		String[] name_array = new String[names.size()];
		for (int idx = 0; idx < name_array.length; idx++) {
			name_array[idx] = names.get(idx);
		}

		return name_array;
	}

	/**
	 * This is a shortcut for {@code getConditionIntensitites(true)}. See {@link #getConditionIntensitites(boolean)}.
	 */
	public float[] getConditionIntensitites() {
		return getConditionIntensitites(true);
	}

	/**
	 * Returns an array of floats containing the summarized abundances per
	 * condition. If the {@code use_mean} parameter is set to false the
	 * abundances are simply summed up. If {@code use_mean} is true then the
	 * summed up abundances are divided by the number of samples in the
	 * corresponding condition.
	 *
	 * @param use_mean build the mean of the abundances per condition
	 * @return array containing the abundance per condition
	 */
	public float[] getConditionIntensitites(boolean use_mean) {
		String[] condition_names = getConditionNames();
		HashMap<String, Float> ints = new HashMap<String, Float>();
		HashMap<String, Integer> counter = new HashMap<String, Integer>();

		for (String s : condition_names) {
			ints.put(s, 0F);
			counter.put(s, 0);
		}

		for (int i = 0; i < intensity_names.length; i++) {
			ints.put(intensity_names[i], ints.get(intensity_names[i]) + intensity_profile[i]);
			counter.put(intensity_names[i], counter.get(intensity_names[i]) + 1);
		}

		float[] profile = new float[ints.size()];
		for (int i = 0; i < profile.length; i++) {
			if (use_mean) {
				profile[i] = ints.get(condition_names[i]) / counter.get(condition_names[i]);
			}
			else {
				profile[i] = ints.get(condition_names[i]);
			}

		}

		return profile;
	}

	/**
	 * Normalizes the values returned by {@link #getConditionIntensitites()}.
	 *
	 * @return normalized condition intensities
	 */
	public float[] getNormalizedConditionIntensities() {
		return normalize(getConditionIntensitites());
	}

	/**
	 * Normalizes the values returned by {@link #getConditionIntensitites(boolean)}.
	 *
	 * @param use_mean will be forwarded to {@link #getConditionIntensitites(boolean)}
	 * @return normalized condition intensities
	 */
	public float[] getNormalizedConditionIntensities(boolean use_mean) {
		return normalize(getConditionIntensitites(use_mean));
	}

	/**
	 * Short for {@code getConditionIntensity(condition_name, true)}. See {@link #getConditionIntensity(java.lang.String, boolean)
	 * }.
	 *
	 * @param condition_name the name of the condition
	 * @return the abundance of that condition
	 */
	public float getConditionIntensity(String condition_name) {
		return getConditionIntensity(condition_name, true);
	}

	/**
	 * Returns the abundance of the condition with the given name. If {@code use_mean}
	 * equals true mean over all samples in the given condition is calculated.
	 * Otherwise the abundances are just summed up.
	 *
	 * @param condition_name condition name to get the intensity from
	 * @param use_mean if true calculates the mean abundance
	 * @return the abundance for the given condition
	 */
	public float getConditionIntensity(String condition_name, boolean use_mean) {
		String[] cnames = getConditionNames();
		float[] cints = getConditionIntensitites(use_mean);
		for (int i = 0; i < cnames.length; i++) {
			if (cnames[i].equals(condition_name)) {
				return cints[i];
			}
		}
		return -1f;
	}

	/**
	 * Returns the normalized condition intensity. The normal condition
	 * intensities are calculated using {@link #getNormalizedConditionIntensities()
	 * } and the correct condition is picked.
	 *
	 * @param condition_name the name of the condition to get the abundance for
	 * @return the normalized abundance for the given condition
	 */
	public float getNormalizedConditionIntensity(String condition_name) {
		String[] cnames = getConditionNames();
		float[] cints = getNormalizedConditionIntensities();
		for (int i = 0; i < cnames.length; i++) {
			if (cnames[i].equals(condition_name)) {
				return cints[i];
			}
		}
		return 0f;
	}

	/**
	 * Returns the number of samples in this profile.
	 *
	 * @return
	 */
	public int sizeRaw() {
		return intensity_names.length;
	}

	/**
	 * Short for {@code add(other, null)}. See {@link #add(de.gobics.marvis.graph.IntensityProfile, java.lang.String)
	 * }.
	 *
	 * @param other
	 */
	public void add(IntensityProfile other) {
		add(other, null);
	}

	/**
	 * Appends the condition names and intensity abundances of the other profile
	 * to this profile. If the {@code label_extension} is not null the string is
	 * appended to every condition name in the other profile.
	 *
	 * @param other another intensity profile to fetch the conditions and
	 * intensity abundances from.
	 * @param label_extension a string that has to be appended to the condition
	 * names of the other intensity profile.
	 */
	public void add(IntensityProfile other, String label_extension) {
		String[] new_names = new String[intensity_names.length + other.intensity_names.length];
		float[] new_intensitites = new float[intensity_names.length + other.intensity_names.length];

		System.arraycopy(intensity_names, 0, new_names, 0, intensity_names.length);
		System.arraycopy(other.intensity_names, 0, new_names, intensity_names.length, other.intensity_names.length);

		if (label_extension != null) {
			for (int idx = intensity_names.length; idx < new_names.length; idx++) {
				new_names[idx] += label_extension;
			}
		}

		System.arraycopy(intensity_profile, 0, new_intensitites, 0, intensity_profile.length);
		System.arraycopy(other.intensity_profile, 0, new_intensitites, intensity_profile.length, other.intensity_profile.length);

		sortAndSetIntensities(new_names, new_intensitites);
	}

	/**
	 * Returns a clone the this intensity profile.
	 *
	 * @return a new IntensityProfile object
	 */
	@Override
	public IntensityProfile clone() {
		IntensityProfile profile = new IntensityProfile();
		profile.intensity_names = intensity_names.clone();
		profile.intensity_profile = intensity_profile.clone();
		return profile;
	}

	/**
	 * Short for {@code normalizeScale(values)}. See {@link #normalizeScale(float[])}
	 *
	 * @param values the values to normalize
	 * @return the normalized values
	 */
	public static float[] normalize(float[] values) {
		return normalizeScale(values);
	}

	/**
	 * Perform a linear normalization of the given values. All values will be
	 * replaced their fraction of the max value, that is
	 * <pre>
	 *  values[i] = values[i] / max(values);
	 * </pre>
	 *
	 * @param values the values to normalize
	 * @return the normalized values
	 */
	public static float[] normalizeScale(float[] values) {
		float[] nints = values.clone();

		// Find max
		float max = 0;
		for (float f : values) {
			if (f > max) {
				max = f;
			}
		}

		// normalize
		for (int i = 0; i < nints.length; i++) {
			nints[i] = nints[i] / max;
		}

		return nints;
	}

	/**
	 * Perform a linear normalization of the given values. All values will be
	 * replaced their fraction of the sum of all values, that is:
	 * <pre>
	 *  values[i] = values[i] / sum(values);
	 * </pre> After this the values will sum up to 1.
	 *
	 * @param values the values to normalize
	 * @return the normalized values
	 */
	public static float[] normalizeEinheitsvektor(float[] values) {
		float[] new_ints = values.clone();

		// Calc sum
		float sum = 0;
		for (float f : values) {
			sum += f;
		}

		// normalize
		for (int i = 0; i < new_ints.length; i++) {
			new_ints[i] = new_ints[i] / sum;
		}

		return new_ints;
	}

	/**
	 * The method sorts the profile according to the names.
	 *
	 * @param names the new condition names
	 * @param ints the new intensities
	 */
	private void sortAndSetIntensities(String[] names, float[] ints) {
		// Sort and set the intensity names
		intensity_names = names.clone();
		intensity_profile = new float[ints.length];
		Arrays.sort(intensity_names, new ConditionComparator());

		TreeMap<String, ArrayList<Float>> map = new TreeMap<String, ArrayList<Float>>();
		for (int i = 0; i < names.length; i++) {
			if (!map.containsKey(names[i])) {
				map.put(names[i], new ArrayList());
			}
			map.get(names[i]).add(ints[i]);
		}

		int intensity_index = 0;
		for (String c : getConditionNames()) {
			for (Float i : map.get(c)) {
				intensity_profile[intensity_index++] = i.floatValue();
			}
		}
	}

	/**
	 * Returns the number of intensities with the given name in it.
	 *
	 * @param name the name of the condition
	 * @return number of samples in the condition
	 */
	public int countIntensities(String name) {
		int sum = 0;
		for (int idx = 0; idx < intensity_names.length; idx++) {
			if (intensity_names[idx].equals(name)) {
				sum++;
			}
		}

		return sum;
	}

	/**
	 * Returns the sample intensities for a given condition name.
	 *
	 * @param name name of the condition
	 * @return the intensity abundances of the samples in the given condition
	 */
	public float[] getIntensitiesForName(String name) {
		float[] values = new float[countIntensities(name)];
		int values_idx = 0;
		for (int idx = 0; idx < intensity_names.length; idx++) {
			if (intensity_names[idx].equals(name)) {
				values[ values_idx++] = intensity_profile[idx];
			}
		}
		return values;
	}
}
