/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.compoundrules;

/**
 *
 * @author manuel
 */
public class CarbonRation extends Rule {

	private static final String[] ratio_elements = new String[]{"H", "F", "Cl", "Br", "N", "O", "P", "Si"};
	private static final float[] ratio_low = new float[]{0.2f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
	private static final float[] ratio_high = new float[]{3.1f, 1.5f, 0.8f, 0.8f, 1.3f, 1.2f, 0.3f, 0.8f, 0.5f};
	private float[] current_ratio_low = new float[0];
	private float[] current_ratio_high = new float[0];
	private int idx_of_carbon = 0;

	@Override
	public String getName() {
		return "Carbon ratio (Fiehn 4+5)";
	}

	@Override
	public String getDescription() {
		return "The element-to-carbon-ratio is bounded";
	}

	@Override
	public int check(int[] formula) {
		if( idx_of_carbon < 0 || formula[idx_of_carbon] <= 0 )
			return OK;

		double r = 0.0d;
		for(int i = 0; i < current_ratio_high.length; i++ ){
			r = ((double)formula[i]) / formula[idx_of_carbon];
			if( r < current_ratio_low[i] )
				return FAIL;
			if( r > current_ratio_high[i] )
				return UPPER_BOUND;
		}

		return OK;
	}

	@Override
	public void prepare(double d, String[] els) {
		idx_of_carbon = -1;
		current_ratio_low = new float[els.length];
		current_ratio_high = new float[els.length];

		for (int i = 0; i < els.length; i++) {
			int idx = idxOf(els[i]);
			if( idx >= 0){
				current_ratio_low[i] = ratio_low[idx];
				current_ratio_high[i] = ratio_high[idx];
			}
			else {
				current_ratio_low[i] = 0;
				current_ratio_high[i] = Float.MAX_VALUE;
			}

			if( els[i].equals("C") )
				idx_of_carbon = i;
		}
		
	}

	private int idxOf(String e) {
		for (int i = 0; i < ratio_elements.length; i++) {
			if (ratio_elements[i].equals(e)) {
				return i;
			}
		}
		return -1;
	}
}
