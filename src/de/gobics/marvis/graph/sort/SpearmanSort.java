
package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.TreeSet;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 *
 * @author manuel
 */
public class SpearmanSort extends AbstractGraphScore {

	private static final SpearmansCorrelation correlation = new SpearmansCorrelation();

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
				complete_corr += calculateCorrelation(m.getIntensityProfile(), t.getIntensityProfile());
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
				logger.finer("Calculating correlation between " + m + " and " + gene);
				IntensityProfile one = generateGenIntensityProfile(graph, gene);
				IntensityProfile two = m.getIntensityProfile();

				complete_corr += calculateCorrelation(one, two);
				counter++;
			}
		}
		if (complete_corr == 0) {
			return 0D;
		}
		return new Double(complete_corr / counter);
	}

	private IntensityProfile generateGenIntensityProfile(MetabolicNetwork graph, Gene gene) {
		IntensityProfile result = new IntensityProfile();

		for (Transcript transcript : graph.getTranscripts(gene)) {
			result.add(transcript.getIntensityProfile());
		}

		return result;
	}

	private double calculateCorrelation(IntensityProfile ip_one, IntensityProfile ip_two) {
		TreeSet<String> names = new TreeSet<String>();
		names.addAll(Arrays.asList(ip_one.getConditionNames()));
		names.addAll(Arrays.asList(ip_two.getConditionNames()));
		LinkedList<String> names_list = new LinkedList<String>(names);

		double[] values_gene = new double[names.size()];
		double[] values_marker = new double[names.size()];

		for (int idx = 0; idx < names.size(); idx++) {
			values_gene[idx] = ip_one.getConditionIntensity(names_list.get(idx));
			values_marker[idx] = ip_two.getConditionIntensity(names_list.get(idx));
		}

		// Use only absolute value because a negative correlation is
		//  also interesting (and positive and negative correlation will
		//  inhibit each other otherwise)
		double corr = correlation.correlation(values_gene, values_marker);
		return Math.abs(corr);
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
