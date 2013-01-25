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
import de.gobics.marvis.utils.swing.AbstractTask;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
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
public class PermutationTest extends AbstractTask<Set<PermutationTestResult>, Void> {

	private static final Logger logger = Logger.getLogger(PermutationTest.class.getName());
	/**
	 * Random number generator for permutation selection.
	 */
	private static final Random rand = new Random(System.currentTimeMillis());
	/**
	 * The basic root network to use.
	 */
	private final MetabolicNetwork root_network;
	/**
	 * List of sub-networks to evaluate.
	 */
	private final MetabolicNetwork[] subnetworks;
	/**
	 * Number of permutations to perform.
	 */
	private int NUM_PERMUTES = 1000;
	/**
	 * A list of found scores.
	 */
	private final LinkedList<Collection<Comparable>> permutation_scores = new LinkedList<>();
	/**
	 * The sub-network calculator for detection of the subnetworks.
	 */
	private final AbstractNetworkCalculation calculator;
	/**
	 * The scoring mechanism to score the graphs.
	 */
	private final AbstractGraphScore scorer;

	public PermutationTest(MetabolicNetwork roo, MetabolicNetwork[] subs, AbstractNetworkCalculation calculator_process, AbstractGraphScore scorer) {
		this.root_network = roo;
		this.subnetworks = subs;
		this.calculator = calculator_process;
		this.scorer = scorer;
	}

	public void setNumberOfPermutations(int NUM_PERMUTES) {
		this.NUM_PERMUTES = NUM_PERMUTES;
	}

	@Override
	protected Set<PermutationTestResult> performTask() throws Exception {
		return calculateScores();
	}

	public Set<PermutationTestResult> calculateScores() throws InterruptedException, Exception {
		setProgressMax(NUM_PERMUTES);
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
		for (Collection<Comparable> col : permutation_scores) {
			Comparable[] array = col.toArray(new Comparable[col.size()]);
			Arrays.sort(array);
			for (Comparable value : array) {
				out.write(value + " \t");
			}
			out.write("\n");
		}
		out.close();


		logger.info("Will now calculate the scores for the found subnetworks");
		Set<PermutationTestResult> scores = new HashSet<>();
		AbstractGraphScore current_scorer = this.scorer.like(root_network);
		current_scorer.setParent(root_network);
		for (MetabolicNetwork subnet : subnetworks) {
			Comparable score = current_scorer.calculateScore(subnet);
			int fwer_errors = countFamilyWiseErrors(permutation_scores, score);
			int fdr_errors = countFalseDiscoveryErrors(permutation_scores, score);
			PermutationTestResult r = new PermutationTestResult(subnet, score, fdr_errors, fwer_errors, NUM_PERMUTES);
			scores.add(r);
		}
		return scores;
	}

	synchronized private void addPermutationScore(Collection<Comparable> scores) {
		permutation_scores.add(scores);
	}

	private int countFamilyWiseErrors(LinkedList<Collection<Comparable>> counts, Comparable score) {
		int counter = 0;
		for (Collection<Comparable> cs : counts) {
			counter += hasHigherEqual(cs, score) ? 1 : 0;
		}

		return counter;
	}

	private boolean hasHigherEqual(Collection<Comparable> scores, Comparable score) {
		for (Comparable i : scores) {
			if (i.compareTo(score) >= 0) {
				return true;
			}
		}
		return false;
	}

	private int countFalseDiscoveryErrors(LinkedList<Collection<Comparable>> counts, Comparable score) {
		int counter = 0;
		for (Collection<Comparable> cs : counts) {
			for (Comparable c : cs) {
				if (c.compareTo(score) >= 0) {
					counter++;
				}
			}
		}

		return counter;
	}

	/**
	 * A thread to calculate a single permutation and extract the networks
	 * afterwards.
	 */
	private class PermutationThread implements Runnable {

		@Override
		public void run() {
			try {
				toTask();
			}
			catch (Exception ex) {
				logger.log(Level.SEVERE, null, ex);
			}
			incrementProgress();
		}

		private void toTask() throws Exception {
			long curtime = System.currentTimeMillis();
			MetabolicNetwork permuted_network = root_network.clone();
			permuteNetwork(permuted_network);
			MetabolicNetwork[] networks = calculator.doInBackground();
			Set<Comparable> score_dist = calculateScores(root_network, networks);
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

		private Set<Comparable> calculateScores(MetabolicNetwork root, MetabolicNetwork[] networks) {
			logger.fine("Calculating scores");
			AbstractGraphScore curscorer = scorer.like(root);
			scorer.setParent(root);
			Set<Comparable> dist = new TreeSet<>();
			for (MetabolicNetwork n : networks) {
				Comparable score = curscorer.calculateScore(n);
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
}
