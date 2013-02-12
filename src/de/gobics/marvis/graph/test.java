package de.gobics.marvis.graph;

import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.graph.gui.tasks.PermutationTest;
import de.gobics.marvis.graph.gui.tasks.PermutationTestResult;
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
	private static final int NUM_PERMUTES = 1000;
	private static final int COFACTOR_THRESHOLD = 10;

	public static void main(String[] args) throws Exception {
		LoggingUtils.initLogger(Level.FINER);
		// Load the network
		logger.finer("Loading network");
		for (String file : args) {
			LoadNetwork loader = new LoadNetwork(file);
			final MetabolicNetwork network = loader.load();

			for (double rp = 0.9d; rp >= 0; rp -= 0.1) {
				logger.info("Permutating for restart probability of: "+rp);
				CalculateNetworksRWR process = new CalculateNetworksRWR(network);
				process.setCofactorThreshold(COFACTOR_THRESHOLD);
				process.setRestartProbability(rp);
				MetabolicNetwork[] subs = process.calculateNetworks(network);

				PermutationTest test = new PermutationTest(network, subs, process, new NetworkSorterSumOfWeights(network));
				test.setNumberOfPermutations(NUM_PERMUTES);

				Set<PermutationTestResult> networks = test.calculateScores(true);

				String resultfilename = file + ".scores-"+rp+".csv";
				logger.info("Writing result to: "+resultfilename);
				BufferedWriter out = new BufferedWriter(new FileWriter(resultfilename));
				out.write("Name\tnum_reactions\tscore\tfwer\tfdr\n");
				for (PermutationTestResult r : networks) {
					out.write(r.network.getName() + "\t" + r.network.getReactions().size() + "\t" + r.score + "\t" + r.fwer + "\t" + r.fdr + "\n");
				}
				out.close();
				
				logger.info("Calculations for restart probability "+rp+" is ready");
			}
		}
	}
}
