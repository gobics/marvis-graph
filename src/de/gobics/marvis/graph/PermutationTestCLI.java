package de.gobics.marvis.graph;

import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.graph.gui.tasks.PermutationTest;
import de.gobics.marvis.graph.gui.tasks.PermutationTestResult;
import de.gobics.marvis.graph.sort.NetworkSorterSumOfWeights;
import de.gobics.marvis.utils.HumanReadable;
import de.gobics.marvis.utils.LoggingUtils;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jblas.DoubleMatrix;

/**
 *
 * @author manuel
 */
public class PermutationTestCLI {

	private final static Logger logger = Logger.getLogger(PermutationTestCLI.class.getName());
	private static final int NUM_PERMUTES = 1000;
	private static final int NUM_THREADS = 4;
	private static final int COFACTOR_THRESHOLD = 10;

	public static void main(String[] args) throws Exception {
		LoggingUtils.initLogger(Level.FINER);

		double rp = new Double(args[0]).doubleValue();
		String file = args[1];

		// Load the network
		LoadNetwork loader = new LoadNetwork(file);
		final MetabolicNetwork network = loader.load();

		logger.log(Level.INFO, "Permutating for restart probability of: {0}", rp);
		CalculateNetworksRWR process = new CalculateNetworksRWR(network);
		process.setCofactorThreshold(COFACTOR_THRESHOLD);
		process.setRestartProbability(rp);
		MetabolicNetwork[] subs = process.calculateNetworks(network);

		PermutationTest test = new PermutationTest(network, subs, process, new NetworkSorterSumOfWeights(network));
		test.setNumberOfPermutations(NUM_PERMUTES);
		test.setNumberOfThreads(NUM_THREADS);

		Set<PermutationTestResult> networks = test.calculateScores(true);

		String resultfilename = file + ".scores-" + rp + ".csv";
		logger.log(Level.INFO, "Writing result to: {0}", resultfilename);
		BufferedWriter out = new BufferedWriter(new FileWriter(resultfilename));
		out.write("Name\tnum_reactions\tscore\tfwer\tfdr\n");
		for (PermutationTestResult r : networks) {
			out.write(r.network.getName() + "\t" + r.network.getReactions().size() + "\t" + r.score + "\t" + r.fwer + "\t" + r.fdr + "\n");
		}
		out.close();

		logger.log(Level.INFO, "Calculations for restart probability {0} is ready", rp);
	}


	
}
