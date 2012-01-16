/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.*;
import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * @author manuel
 */
public class SpearmanSort extends AbstractGraphSort {

	public SpearmanSort(MetabolicNetwork p) {
		super(p);
	}

	public SpearmanSort() {
		super();
	}

	@Override
	public Comparable calculateScore(MetabolicNetwork graph) {
		return calculateScoreGene(graph);
	}

	private Comparable calculateScoreTranscript(MetabolicNetwork graph) {
		double complete_corr = 0;
		int counter = 0;
		for (Marker m : graph.getMarkers()) {
			for (Transcript t : graph.getTranscripts()) {
				logger.finer("Calculating correlation between " + m + " and " + t);
				double corr = getCorrelation(m.getIntensityProfile(), t.getIntensityProfile());
				if (corr < 0) // Build abs value
				{
					corr *= -1;
				}
				complete_corr += corr;
				counter++;
			}
		}
		if (complete_corr == 0) {
			return 0D;
		}
		return new Double(complete_corr / counter);
	}

	private Comparable calculateScoreGene(MetabolicNetwork graph) {
		double complete_corr = 0;
		int counter = 0;
		for (Marker m : graph.getMarkers()) {
			for (Gene gene : graph.getGenes()) {
				LinkedList<Transcript> transcripts = graph.getTranscripts(gene);
				if (transcripts.size() > 0) {
					String[] names = transcripts.get(0).getIntensityProfile().getConditionNames();
					float[] intensities = transcripts.get(0).getIntensityProfile().getConditionIntensitites(true);

					for(int idx = 0; idx < transcripts.size(); idx++){
						IntensityProfile ip = transcripts.get(idx).getIntensityProfile();
						for(int i = 0; i < names.length ; i++ ){
							double it = ip.getConditionIntensity(names[i]);
							if( it >= 0)
								intensities[i]+=it;
						}
					}

					
					IntensityProfile ip = new IntensityProfile();
					ip.setIntensity(names, intensities);
					logger.finer("Calculating correlation between " + m + " and " + gene);
					double corr = getCorrelation(m.getIntensityProfile(), ip);
					if (corr < 0) // Build abs value
					{
						corr *= -1;
					}
					complete_corr += corr;
					counter++;
				}
			}
		}
		if (complete_corr == 0) {
			return 0D;
		}
		return new Double(complete_corr / counter);
	}

	protected double getCorrelation(IntensityProfile i1, IntensityProfile i2) {
		float[] intensities1 = i1.getConditionIntensitites();
		String[] names = i1.getConditionNames();
		float[] intensities2 = new float[intensities1.length];

		for (int i = 0; i < intensities1.length;) {
			intensities2[i] = i2.getConditionIntensity(names[i], false);
			logger.finer("Intensity for '" + names[i] + "' is: " + intensities2[i]);
			if (intensities2[i] < 0) {
				names = removeIndex(names, i);
				intensities1 = removeIndex(intensities1, i);
				intensities2 = removeIndex(intensities2, i);
			}
			else {
				i++;
			}
		}
		if (names.length < 2) {
			return 0;
		}

		logger.finer("Inte-Array 1: " + Arrays.toString(intensities1));
		logger.finer("Rank-Array 1: " + Arrays.toString(buildRankArray(intensities1)));
		logger.finer("Inte-Array 2: " + Arrays.toString(intensities2));
		logger.finer("Rank-Array 2: " + Arrays.toString(buildRankArray(intensities2)));


		return getPearsonCorrelation(buildRankArray(intensities1), buildRankArray(intensities2));
	}

	public static int[] buildRankArray(float[] input) {
		int[] ranks = new int[input.length];
		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < input.length; j++) {
				if (input[j] <= input[i]) {
					ranks[i]++;
				}
			}
		}
		return ranks;
	}

	private static float[] removeIndex(float[] array, int idx) {
		float[] old_array = array.clone();
		array = new float[old_array.length - 1];
		System.arraycopy(old_array, 0, array, 0, idx);
		System.arraycopy(old_array, idx + 1, array, idx, array.length - idx);
		return array;
	}

	private static String[] removeIndex(String[] array, int idx) {
		String[] old_array = array.clone();
		array = new String[old_array.length - 1];
		System.arraycopy(old_array, 0, array, 0, idx);
		System.arraycopy(old_array, idx + 1, array, idx, array.length - idx);
		logger.finer("Spliced " + Arrays.toString(old_array) + " to " + Arrays.toString(array));
		return array;
	}

	private double getPearsonCorrelation(int[] scores1, int[] scores2) {
		double result = 0;
		double sum_sq_x = 0;
		double sum_sq_y = 0;
		double sum_coproduct = 0;
		double mean_x = scores1[0];
		double mean_y = scores2[0];
		for (int i = 2; i < scores1.length + 1; i += 1) {
			double sweep = Double.valueOf(i - 1) / i;
			double delta_x = scores1[i - 1] - mean_x;
			double delta_y = scores2[i - 1] - mean_y;
			sum_sq_x += delta_x * delta_x * sweep;
			sum_sq_y += delta_y * delta_y * sweep;
			sum_coproduct += delta_x * delta_y * sweep;
			mean_x += delta_x / i;
			mean_y += delta_y / i;
		}
		double pop_sd_x = (double) Math.sqrt(sum_sq_x / scores1.length);
		double pop_sd_y = (double) Math.sqrt(sum_sq_y / scores1.length);
		double cov_x_y = sum_coproduct / scores1.length;
		return cov_x_y / (pop_sd_x * pop_sd_y);
	}

	@Override
	public String getName() {
		return "Spearman correlation";
	}

	@Override
	public String getDescription() {
		return "Calculate Spearman rank correlations between metabolic markers and transcripts";
				
	}
}
