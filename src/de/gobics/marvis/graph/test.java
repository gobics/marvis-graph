package de.gobics.marvis.graph;

import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.graph.gui.tasks.PermutationTestResult;
import de.gobics.marvis.graph.gui.tasks.PermutationTest;
import de.gobics.marvis.graph.sort.NetworkSorterSumOfWeights;
import de.gobics.marvis.utils.LoggingUtils;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class test {

	private final static Logger logger = Logger.getLogger(test.class.getName());
	private static double restart_probability = 0.8;
	private static final int NUM_PERMUTES = 10;
	private static final int COFACTOR_THRESHOLD = 10;

	public static void main(String[] args) throws Exception {
		LoggingUtils.initLogger(Level.FINER);
		// Load the network
		logger.finer("Loading network");
		LoadNetwork loader = new LoadNetwork("testdata/wound1/wound1.complete.cut5000.intNames.xml.gz");
		final MetabolicNetwork network = loader.load();


		CalculateNetworksRWR process = new CalculateNetworksRWR(network);
		process.setCofactorThreshold(COFACTOR_THRESHOLD);
		process.setRestartProbability(restart_probability);
		MetabolicNetwork[] subs = process.calculateNetworks(network);

		PermutationTest test = new PermutationTest(network, subs, process, new NetworkSorterSumOfWeights(network));
		test.setNumberOfPermutations(NUM_PERMUTES);
		Set<PermutationTestResult> networks = test.calculateScores();

		BufferedWriter out = new BufferedWriter(new FileWriter("scores.csv"));
		out.write("Name\tnum_reactions\tscore\tfwer\nfdr\n");
		for (PermutationTestResult r : networks) {
			out.write(r.network.getName() + "\t" +r.network.getReactions().size()+"\t"+ r.score + "\t" + r.fwer + "\t" + r.fdr + "\n");
		}
		out.close();
	}
}