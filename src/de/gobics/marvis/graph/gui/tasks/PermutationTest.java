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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background task to perform a permutation test on previously calculated
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
	 * Number of parallel threads to run.
	 */
	private int num_threads = Runtime.getRuntime().availableProcessors();
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

	public void setNumberOfThreads(int num_threads) {
		this.num_threads = num_threads > 0 ? num_threads : 1;
	}

	@Override
	protected Set<PermutationTestResult> performTask() throws Exception {
		return calculateScores();
	}

	public Set<PermutationTestResult> calculateScores() throws InterruptedException, Exception {
		setProgressMax(NUM_PERMUTES);
		sendDescription("Permuting network structure and calculating sub networks");
		ExecutorService pool = Executors.newFixedThreadPool(num_threads);
		for (int i = 0; i < NUM_PERMUTES; i++) {
			pool.execute(new PermutationThread(i));
		}
		pool.shutdown();
		while (!pool.isTerminated()) {
			if (isCancelled()) {
				pool.shutdownNow();
				return null;
			}
			Thread.sleep(1000);
		}

		sendDescription("Calculating Family-Wise-Error-Rate");
		//	System.out.println(permutation_scores);

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
			double fwer = ((double) fwer_errors) / NUM_PERMUTES;
			int fdr_errors = countFalseDiscoveryErrors(permutation_scores, score);
			double fdr = ((double) fdr_errors) / NUM_PERMUTES;

			PermutationTestResult r = new PermutationTestResult(subnet, score, fdr, fwer);
			scores.add(r);
		}
		return scores;
	}

	synchronized private void addPermutationScore(Collection<Comparable> scores) {
		permutation_scores.add(scores);
	}

	public int countFamilyWiseErrors(LinkedList<Collection<Comparable>> counts, Comparable score) {
		int counter = 0;
		for (Collection<Comparable> cs : counts) {
			counter += hasHigherEqual(cs, score) ? 1 : 0;
		}

		return counter;
	}

	/**
	 * Returns true if the collection contains an object that is higher or equal
	 * the given score.
	 *
	 * @param scores the scores to search
	 * @param score the comparative score
	 * @return true if such a score exists in {@code scores}
	 */
	public boolean hasHigherEqual(Collection<Comparable> scores, Comparable score) {
		for (Comparable i : scores) {
			if (i.compareTo(score) >= 0) {
				return true;
			}
		}
		return false;
	}

	public int countFalseDiscoveryErrors(LinkedList<Collection<Comparable>> counts, Comparable score) {
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

		private final int permutation_number;

		public PermutationThread(int permutation) {
			this.permutation_number = permutation;
		}

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
			logger.log(Level.FINE, "Thread for permutation {0} started", permutation_number);
			long curtime = System.currentTimeMillis();
			MetabolicNetwork permuted_network = root_network.clone();
			permuteNetwork(permuted_network);
			MetabolicNetwork[] networks = calculator.like(permuted_network).doInBackground();
			Set<Comparable> score_dist = calculateScores(root_network, networks);
			addPermutationScore(score_dist);
			logger.log(Level.FINER, "Calculation for permutation " + permutation_number + " took {0} seconds", (System.currentTimeMillis() - curtime) / 1000);
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
			int num_annotations;

			// Permute marker annotations
			num_annotations = 0;
			for (Relation r : network.getRelations(RelationshipType.MARKER_ANNOTATION_COMPOUND)) {
				network.removeRelation(r);
				num_annotations++;
			}
			for (int idx = 0; idx < num_annotations; idx++) {
				network.addRelation(new Relation(RelationshipType.MARKER_ANNOTATION_COMPOUND, random(markers), random(compounds)));
			}

			num_annotations = 0;
			for (Relation r : network.getRelations(RelationshipType.TRANSCRIPT_ISFROM_GENE)) {
				network.removeRelation(r);
				num_annotations++;
			}
			Iterator<Transcript> titer = transcripts.iterator();
			for (int idx = 0; idx < num_annotations; idx++) {
				Transcript t = titer.next(); // Transcript is always only connected to one gene, therefore we just use the next one.
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
