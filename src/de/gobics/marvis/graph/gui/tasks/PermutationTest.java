/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.Gene;
import de.gobics.marvis.graph.Marker;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.RelationshipType;
import de.gobics.marvis.graph.Transcript;
import de.gobics.marvis.graph.sort.AbstractGraphScore;
import de.gobics.marvis.graph.sort.NetworkSorterDiameter;
import de.gobics.marvis.graph.sort.NetworkSorterSumOfWeights;
import de.gobics.marvis.graph.test;
import de.gobics.marvis.utils.swing.AbstractTask;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class PermutationTest extends AbstractTask<Map<MetabolicNetwork, Double>, Void> {

	private static final Random rand = new Random(System.currentTimeMillis());
	private static final Logger logger = Logger.getLogger(PermutationTest.class.getName());
	private final MetabolicNetwork root_network;
	private final MetabolicNetwork[] subnetworks;
	private double restart_probability = 0.8;
	private int NUM_PERMUTES = 1000;
	private int COFACTOR_THRESHOLD = 25;
	private final LinkedList<Collection<Double>> permutation_scores = new LinkedList<>();

	public PermutationTest(MetabolicNetwork roo, MetabolicNetwork[] subs) {
		this.root_network = roo;
		this.subnetworks = subs;
	}

	public void setRestartProbability(double restart_probability) {
		this.restart_probability = restart_probability;
	}

	public void setNumberOfPermutations(int NUM_PERMUTES) {
		this.NUM_PERMUTES = NUM_PERMUTES;
	}

	public void setCofactorThreshold(int COFACTOR_THRESHOLD) {
		this.COFACTOR_THRESHOLD = COFACTOR_THRESHOLD;
	}

	@Override
	protected Map<MetabolicNetwork, Double> performTask() throws Exception {
		HashMap<MetabolicNetwork, Double> mapped = new HashMap<>();
		for (Result r : calculateScores()) {
			mapped.put(r.network, r.fwer);
		}
		return mapped;
	}

	public Set<Result> calculateScores() throws InterruptedException, Exception {
		sendDescription("Permuting network structure and calculating sub networks");
		ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (int i = 0; i < NUM_PERMUTES; i++) {
			pool.execute(new PermutationThread());
		}
		pool.shutdown();
		pool.awaitTermination(NUM_PERMUTES, TimeUnit.DAYS); // wait for a more-or-less infinite time

		sendDescription("Calculating Family-Wise-Error-Rate");
		System.out.println(permutation_scores);

		BufferedWriter out = new BufferedWriter(new FileWriter("fwer_scores.csv"));
		for (Collection<Double> col : permutation_scores) {
			Double[] array = col.toArray(new Double[col.size()]);
			Arrays.sort(array);
			for (Double value : array) {
				out.write(value + " \t");
			}
			out.write("\n");
		}
		out.close();


		logger.info("Will now calculate the scores for the found subnetworks");
		Set<Result> scores = new HashSet<>();
		NetworkSorterSumOfWeights scorer = new NetworkSorterSumOfWeights(root_network);
		scorer.setParent(root_network);
		for (MetabolicNetwork subnet : subnetworks) {
			double score = scorer.calculateScore(subnet);
			int errors = countErrors(permutation_scores, score);
			Result r = new Result(subnet, score, errors);
			scores.add(r);
		}
		return scores;
	}

	synchronized private void addPermutationScore(Collection<Double> scores) {
		permutation_scores.add(scores);
	}

	private int countErrors(LinkedList<Collection<Double>> counts, double score) {
		int counter = 0;
		for (Collection<Double> cs : counts) {
			counter += hasHigherEqual(cs, score) ? 1 : 0;
		}

		return counter;
	}

	private boolean hasHigherEqual(Collection<Double> scores, Comparable score) {
		for (Comparable i : scores) {
			if (i.compareTo(score) >= 0) {
				return true;
			}
		}
		return false;
	}

	private class PermutationThread implements Runnable {

		@Override
		public void run() {
			try {
				toTask();
			}
			catch (Exception ex) {
				logger.log(Level.SEVERE, null, ex);
			}
		}

		private void toTask() throws Exception {
			long curtime = System.currentTimeMillis();
			MetabolicNetwork permuted_network = root_network.clone();
			permuteNetwork(permuted_network);
			CalculateNetworksRWR process = new CalculateNetworksRWR(permuted_network);
			process.setRestartProbability(restart_probability);
			process.setCofactorThreshold(COFACTOR_THRESHOLD);
			MetabolicNetwork[] networks = process.calculateNetworks();
			Set<Double> score_dist = calculateScores(root_network, networks);
			addPermutationScore(score_dist);
			incrementProgress();
			logger.log(Level.FINER, "Calculation of one permutation took {0} seconds", (System.currentTimeMillis() - curtime) / 1000);
		}

		/**
		 * Permutes the network in-place
		 *
		 * @param network
		 */
		private void permuteNetwork(MetabolicNetwork network) {
			Collection<Marker> markers = network.getMarkers();
			Collection<Transcript> transcripts = network.getTranscripts();
			Collection<Compound> compounds = network.getCompounds();
			Collection<Gene> genes = network.getGenes();

			// Remove relations
			int num_annotations = network.getRelations(RelationshipType.MARKER_ANNOTATION_COMPOUND).size();
			for (Relation r : network.getRelations(RelationshipType.MARKER_ANNOTATION_COMPOUND)) {
				network.removeRelation(r);
			}
			for (Relation r : network.getRelations(RelationshipType.TRANSCRIPT_ISFROM_GENE)) {
				network.removeRelation(r);
			}

			// Add new relations
			for (int idx = 0; idx < num_annotations; idx++) {
				network.addRelation(new Relation(RelationshipType.MARKER_ANNOTATION_COMPOUND, random(markers), random(compounds)));
			}
			for (Transcript t : transcripts) {
				network.addRelation(new Relation(RelationshipType.TRANSCRIPT_ISFROM_GENE, t, random(genes)));
			}
		}

		private Set<Double> calculateScores(MetabolicNetwork root, MetabolicNetwork[] networks) {
			logger.fine("Calculating scores");
			NetworkSorterSumOfWeights scorer = new NetworkSorterSumOfWeights(root);
			scorer.setParent(root);
			Set<Double> dist = new TreeSet<>();
			for (MetabolicNetwork n : networks) {
				double score = scorer.calculateScore(n);
				dist.add(score);
			}
			return dist;
		}

		/**
		 * Randomly select one object out of the given collection.
		 *
		 * @param <T>
		 * @param selection
		 * @return one object out of {@code selection}
		 */
		private <T> T random(Collection<T> selection) {
			int idx = rand.nextInt(selection.size() - 1);
			Iterator<T> iterator = selection.iterator();
			while (idx-- >= 0) {
				iterator.next();
			}
			return iterator.next();
		}
	}

	public class Result {

		public final MetabolicNetwork network;
		public final int errors;
		public final double fwer;
		public final double score;

		public Result(MetabolicNetwork n, double score, int errors) {
			this.network = n;
			this.score = score;
			this.errors = errors;
			this.fwer = ((double) errors) / ((double) NUM_PERMUTES);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Result other = (Result) obj;
			if (!Objects.equals(this.network, other.network)) {
				return false;
			}
			if (this.errors != other.errors) {
				return false;
			}
			if (this.fwer != other.fwer) {
				return false;
			}
			if (Double.doubleToLongBits(this.score) != Double.doubleToLongBits(other.score)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 97 * hash + Objects.hashCode(this.network);
			hash = 97 * hash + this.errors;
			hash = 97 * hash + (int) (Double.doubleToLongBits(this.fwer) ^ (Double.doubleToLongBits(this.fwer) >>> 32));
			hash = 97 * hash + (int) (Double.doubleToLongBits(this.score) ^ (Double.doubleToLongBits(this.score) >>> 32));
			return hash;
		}
	}
}
