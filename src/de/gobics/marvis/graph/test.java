/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph;

import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.graph.gui.tasks.SortNetworksTask;
import de.gobics.marvis.graph.sort.NetworkSorterSumOfWeights;
import de.gobics.marvis.utils.LoggingUtils;
import java.util.Arrays;
import java.util.logging.Level;

/**
 *
 * @author manuel
 */
public class test {
	private final static Logger logger = Logger.getLogger(test.class.getName());
	private static final int NUM_PERMUTES = 1000;
	private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors() - 1;
	private static final int COFACTOR_THRESHOLD = 10;

	public static void main(String[] args) throws Exception, Throwable {
		LoggingUtils.initLogger(Level.FINER);

		double rp = new Double(args[0]).doubleValue();
		LoadNetwork loader = new LoadNetwork(args[1]);
		final MetabolicNetwork network = loader.load();

		logger.info("Permutating for restart probability of: " + rp);
		CalculateNetworksRWR process = new CalculateNetworksRWR(network);
		process.setCofactorThreshold(COFACTOR_THRESHOLD);
		process.setRestartProbability(rp);
		MetabolicNetwork[] subs = process.calculateNetworks(network);

		PermutationTest test = new PermutationTest(network, subs, process, new NetworkSorterSumOfWeights(network));
		test.setNumberOfPermutations(NUM_PERMUTES);
		test.setNumberOfThreads(NUM_THREADS);

		Set<PermutationTestResult> networks = test.calculateScores(true);

		String resultfilename = args[1] + ".scores-" + rp + ".csv";
		logger.log(Level.INFO, "Writing result to: {0}", resultfilename);
		BufferedWriter out = new BufferedWriter(new FileWriter(resultfilename));
		out.write("Name\tnum_reactions\tscore\tfwer\tfdr\n");
		for (PermutationTestResult r : networks) {
			out.write(r.network.getName() + "\t" + r.network.getReactions().size() + "\t" + r.score + "\t" + r.fwer + "\t" + r.fdr + "\n");
		}
		out.close();

		logger.info("Calculations for restart probability " + rp + " is ready");
		System.exit(0);
	}

	private static void testMatrixSize() {
		Runtime rt = Runtime.getRuntime();
		for (int i = 100; true; i += 100) {
			DoubleMatrix m = new DoubleMatrix(i, i);
			System.out.println(i + " => " + HumanReadable.bytes(rt.totalMemory() - rt.freeMemory()));
			m = null;
			System.gc();
		}

	}

	private static void timeWalker() throws Throwable {
		MetabolicNetwork network = new LoadNetwork("testdata/wound1/wound1.complete.cut5000.intNames.xml.gz").perform();
		Runtime rt = Runtime.getRuntime();

		CalculateNetworksRWR process = new CalculateNetworksRWR(network, true);
		process.setRestartProbability(0.8);
		process.setCofactorThreshold(COFACTOR_THRESHOLD);
		long t0 = System.currentTimeMillis();
		process.perform();
		System.out.println("Sparse took " + (System.currentTimeMillis() - t0));
		System.out.println("Memory: " + (rt.totalMemory() - rt.freeMemory()));

		System.gc();

		process = new CalculateNetworksRWR(network, false);
		process.setRestartProbability(0.8);
		process.setCofactorThreshold(COFACTOR_THRESHOLD);
		t0 = System.currentTimeMillis();
		process.perform();
		System.out.println("Dense took " + (System.currentTimeMillis() - t0));
		System.out.println("Memory: " + (rt.totalMemory() - rt.freeMemory()));

		System.exit(0);
	}
}
