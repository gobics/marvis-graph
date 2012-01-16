/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph;

import java.util.*;

/**
 *
 * @author manuel
 */
public class IntensityProfile implements Cloneable {

	private String[] intensity_names = new String[0];
	private float[] intensity_profile = new float[0];

	public void setIntensity(String[] desired_condition_names, float[] data) {
		if (desired_condition_names.length != data.length) {
			throw new IllegalArgumentException("Conditions names and data array are not of equal length");
		}
		sortAndSetIntensities(desired_condition_names, data);
		//intensity_names = desired_condition_names;
		//intensity_profile = data;
	}

	public String[] getRawIntensityNames() {
		return intensity_names.clone();
	}

	public float[] getRawIntensities() {
		return intensity_profile.clone();
	}

	public Float[] getRawIntensitiesAsFloat() {
		Float[] ints = new Float[intensity_profile.length];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = (Float) intensity_profile[i];
		}
		return ints;
	}
	
	public float[] getNormalizedIntensities() {
		return normalize(intensity_profile);
	}

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

	public float[] getConditionIntensitites() {
		return getConditionIntensitites(true);
	}

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
			} else {
				profile[i] = ints.get(condition_names[i]);
			}

		}

		return profile;
	}

	public float[] getNormalizedConditionIntensities() {
		return normalize(getConditionIntensitites());
	}

	public float[] getNormalizedConditionIntensities(boolean use_mean) {
		return normalize(getConditionIntensitites(use_mean));
	}

	public float getConditionIntensity(String condition_name) {
		return getConditionIntensity(condition_name, true);
	}

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

	public int sizeRaw() {
		return intensity_names.length;
	}

	public void add(IntensityProfile other) {
		add(other, "");
	}

	public void add(IntensityProfile other, String label_extension) {
		String[] new_names = new String[intensity_names.length + other.intensity_names.length];
		float[] new_intensitites = new float[intensity_names.length + other.intensity_names.length];

		System.arraycopy(intensity_names, 0, new_names, 0, intensity_names.length);
		System.arraycopy(other.intensity_names, 0, new_names, intensity_names.length, other.intensity_names.length);

		for (int idx = intensity_names.length; idx < new_names.length; idx++) {
			new_names[idx] += label_extension;
		}

		System.arraycopy(intensity_profile, 0, new_intensitites, 0, intensity_profile.length);
		System.arraycopy(other.intensity_profile, 0, new_intensitites, intensity_profile.length, other.intensity_profile.length);

		sortAndSetIntensities(new_names, new_intensitites);
	}

	@Override
	public IntensityProfile clone() {
		IntensityProfile profile = new IntensityProfile();
		profile.intensity_names = intensity_names.clone();
		profile.intensity_profile = intensity_profile.clone();
		return profile;
	}

	public static float[] normalize(float[] intensity) {
		return normalizeSkale(intensity);
	}

	public static float[] normalizeSkale(float[] intensity_profile) {
		float[] nints = intensity_profile.clone();

		// Find max
		float max = 0;
		for (float f : intensity_profile) {
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

	public static float[] normalizeEinheitsvektor(float[] intensity_profile) {
		float[] new_ints = intensity_profile.clone();

		// Calc sum
		float sum = 0;
		for (float f : intensity_profile) {
			sum += f;
		}

		// normalize
		for (int i = 0; i < new_ints.length; i++) {
			new_ints[i] = new_ints[i] / sum;
		}

		return new_ints;
	}

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
	 * Returns the number of intensities with the given name in it
	 * @param name
	 * @return 
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
	
	public float[] getIntensitiesForName(String name){
		float[] values = new float[ countIntensities(name) ];
		int values_idx = 0;
		for( int idx = 0 ; idx < intensity_names.length ; idx ++){
			if( intensity_names[idx].equals(name) )
				values[ values_idx++ ] = intensity_profile[idx];
		}
		return values;
	}
}
