package de.gobics.marvis.graph;

import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.graph.sort.NetworkSorterDiameter;
import de.gobics.marvis.utils.LoggingUtils;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class test {

	private static final Random rand = new Random(System.currentTimeMillis());
	private final static Logger logger = Logger.getLogger(test.class.getName());
	private static double restart_probability = 0.9;
	private static BufferedWriter out;
	private static int NUM_PERMUTES = 1000;

	public static void main(String[] args) throws Exception {
		LoggingUtils.initLogger(Level.FINER);
		// Load the network
		logger.finer("Loading network");
		LoadNetwork loader = new LoadNetwork("testdata/wound1/wound1.complete.cut5000.intNames.xml.gz");
		MetabolicNetwork network = loader.load();
		out = new BufferedWriter(new FileWriter("score_dist.csv"));

		CalculateNetworksRWR process = new CalculateNetworksRWR(network);
		process.setRestartProbability(restart_probability);
		process.setCofactorThreshold(25);

		for (int i = 0; i < NUM_PERMUTES; i++) {
			logger.log(Level.FINE, "Calculating networks in step {0}", i + 1);
			MetabolicNetwork[] networks = process.calculateNetworks();
			HashMap<Integer, Integer> score_dist = calculateScoreDistribution(network, networks);
			output(score_dist);
			permuteNetwork(network);
		}

		out.close();

	}

	/**
	 * Permutes the network in-place
	 *
	 * @param network
	 */
	private static void permuteNetwork(MetabolicNetwork network) {
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

	private static <T> T random(Collection<T> selection) {
		int idx = rand.nextInt(selection.size() - 1);
		Iterator<T> iterator = selection.iterator();
		while (idx-- >= 0) {
			iterator.next();
		}
		return iterator.next();
	}

	private static HashMap<Integer, Integer> calculateScoreDistribution(MetabolicNetwork root, MetabolicNetwork[] networks) {
		logger.fine("Calculating score distribution");
		NetworkSorterDiameter scorer = new NetworkSorterDiameter(root);
		HashMap<Integer, Integer> dist = new HashMap<>();
		for (MetabolicNetwork n : networks) {
			int score = scorer.calculateScore(n);
			if (!dist.containsKey(score)) {
				dist.put(score, 1);
			}
			else {
				dist.put(score, dist.get(score) + 1);
			}
		}
		return dist;
	}

	private static void output(HashMap<Integer, Integer> score_dist) throws Exception {
		logger.fine("Writing output");
		int num = 1;

		System.out.println(score_dist);

		int max = 0;
		for (Integer i : score_dist.keySet()) {
			max = Math.max(max, i);
		}

		// sort keys
		while (num <= max) {
			if (score_dist.containsKey(num)) {
				out.write(score_dist.get(num) + "\t");
			}
			else {
				out.write("0\t");
			}
			num++;
		}
		out.write("\n");
	}
}