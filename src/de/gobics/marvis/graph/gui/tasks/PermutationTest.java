package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.Gene;
import de.gobics.marvis.graph.Marker;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.RelationshipType;
import de.gobics.marvis.graph.Transcript;
import de.gobics.marvis.graph.sort.AbstractGraphScore;
import de.gobics.marvis.utils.task.AbstractTask;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
	public static final Random rand = new Random(System.currentTimeMillis());
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
	private long last_update;

	public PermutationTest(MetabolicNetwork roo, MetabolicNetwork[] subs, AbstractNetworkCalculation calculator_process, AbstractGraphScore scorer) {
		this.root_network = roo;
		this.subnetworks = subs;
		this.calculator = calculator_process;
		this.scorer = scorer;
		setTaskTitle("Permutation test for network scoring");
	}

	public void setNumberOfPermutations(int NUM_PERMUTES) {
		this.NUM_PERMUTES = NUM_PERMUTES;
	}

	public void setNumberOfThreads(int num_threads) {
		this.num_threads = num_threads > 0 ? num_threads : 1;
	}

	@Override
	protected Set<PermutationTestResult> doTask() throws Exception {
		return calculateScores();
	}

	public Set<PermutationTestResult> calculateScores() throws InterruptedException, Exception {
		return calculateScores(false);
	}

	public Set<PermutationTestResult> calculateScores(boolean check_for_indeterminate) throws InterruptedException, Exception {
		setProgressMax(NUM_PERMUTES);
		setTaskDescription("Permuting network structure and calculating sub networks");
		ExecutorService pool = Executors.newFixedThreadPool(num_threads);
		for (int i = 0; i < NUM_PERMUTES; i++) {
			pool.execute(new PermutationThread(i));
		}
		pool.shutdown();

		last_update = System.currentTimeMillis();
		while (!pool.isTerminated()) {
			if (isCanceled()) {
				pool.shutdownNow();
				return null;
			}

			// Check if one one or more tasks ran into indetermination (give them 5 minutes after the last update).
			if (check_for_indeterminate && (System.currentTimeMillis() - last_update) > (60 * 5000)) {
				pool.shutdownNow();
				break;
			}

			Thread.sleep(1000);
		}

		setTaskDescription(
				"Calculating Family-Wise-Error-Rate");
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

		logger.info("Will now calculate the scores for the found subnetworks with permutation scores:");

		// Calculate overall number of detected subnetworks
		int fdr_divisor = 0;
		for (Collection<Comparable> cs : permutation_scores) {
			fdr_divisor += cs.size();
		}
		Set<PermutationTestResult> scores = new HashSet<>();
		AbstractGraphScore current_scorer = this.scorer.like(root_network);

		current_scorer.setParent(root_network);
		for (MetabolicNetwork subnet : subnetworks) {
			Comparable score = current_scorer.calculateScore(subnet);
			double fwer = countFamilyWiseErrors(permutation_scores, score);
			double fdr = countFalseDiscoveryErrors(permutation_scores, score);

			PermutationTestResult r = new PermutationTestResult(subnet, score, fdr, fwer);
			scores.add(r);
		}
		return scores;
	}

	synchronized private void addPermutationScore(Collection<Comparable> scores) {
		permutation_scores.add(scores);
		this.last_update = System.currentTimeMillis();
		System.gc();
	}

	public double countFamilyWiseErrors(LinkedList<Collection<Comparable>> counts, Comparable score) {
		double counter = 0;
		for (Collection<Comparable> cs : counts) {
			counter += hasHigherEqual(cs, score) ? 1 : 0;
		}

		return counter > 0 ? counter / NUM_PERMUTES : 1d / NUM_PERMUTES;
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

	/**
	 * Calculates the false-discovery-rate as mean value over all false
	 * discoveries.
	 *
	 * @param counts
	 * @param score
	 * @return
	 */
	public double countFalseDiscoveryErrors(LinkedList<Collection<Comparable>> counts, Comparable score) {
		double mean = 0;
		for (Collection<Comparable> cs : counts) {
			int counter = 0;
			for (Comparable c : cs) {
				if (c.compareTo(score) >= 0) {
					counter++;
				}
			}
			mean += ((double) counter) / cs.size();
		}

		return mean / counts.size();
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
			boolean success = false;
			while (!success) {
				try {
					toTask();
					success = true;
				}
				catch (Throwable ex) {
					logger.log(Level.SEVERE, "Redo permutation that resulted in exception: ", ex);
				}
			}
			incrementProgress();
		}

		private void toTask() throws Throwable {
			logger.log(Level.FINE, "Thread for permutation {0} started", permutation_number);
			long curtime = System.currentTimeMillis();
			MetabolicNetwork permuted_network = permuteNetwork(permutation_number);
			MetabolicNetwork[] networks = calculator.like(permuted_network).perform();
			Collection<Comparable> score_dist = calculateScores(permuted_network, networks);
			addPermutationScore(score_dist);
			logger.log(Level.FINER, "Calculation for permutation " + permutation_number + " took {0} seconds", (System.currentTimeMillis() - curtime) / 1000);
		}

		/**
		 * Creates a permutation of the network.
		 *
		 * @param network
		 */
		private MetabolicNetwork permuteNetwork(int permutation_number) {
			MetabolicNetwork network = root_network.clone();
			network.setName("Permutation " + permutation_number + " of " + root_network.getName());
			// Remove marker and 
			List<Marker> markers = new ArrayList<>(network.countMarkers());
			for (Marker m : network.getMarkers()) {
				if (!network.getAnnotations(m).isEmpty()) {
					markers.add(m);
				}
			}
			List<Transcript> transcripts = new ArrayList<>(network.countTranscripts());
			for (Transcript t : network.getTranscripts()) {
				if (!network.getGenes(t).isEmpty()) {
					transcripts.add(t);
				}
			}
			List<Compound> compounds = new ArrayList<>(network.getCompounds());
			List<Gene> genes = new ArrayList<>(network.getGenes());
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

			
			// Permute transcripts annotations
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

			return network;
		}

		private Collection<Comparable> calculateScores(MetabolicNetwork root, MetabolicNetwork[] networks) {
			logger.fine("Calculating scores");
			AbstractGraphScore curscorer = scorer.like(root);
			scorer.setParent(root);
			Collection<Comparable> dist = new ArrayList<>(networks.length);
			for (MetabolicNetwork n : networks) {

				Comparable score = curscorer.calculateScore(n);
				//logger.finer("Score for "+n.getName()+": "+score);
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
		private <T> T random(List<T> selection) {
			int idx = rand.nextInt(selection.size());
			//System.err.println("Next int is: "+idx);
			return selection.get(idx);
		}
	}
}
