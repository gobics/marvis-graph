package de.gobics.marvis.graph;

import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.graph.gui.tasks.PermutationResultFwer;
import de.gobics.marvis.graph.gui.tasks.PermutationTest;
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
	private static final int NUM_PERMUTES = 3000;
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
		MetabolicNetwork[] subs = process.calculateNetworks();

		PermutationTest test = new PermutationTest(network, subs);
		test.setNumberOfPermutations(NUM_PERMUTES);
		test.setCofactorThreshold(COFACTOR_THRESHOLD);
		Set<PermutationResultFwer> networks = test.calculateScores();

		BufferedWriter out = new BufferedWriter(new FileWriter("scores.csv"));
		out.write("Name\tnum_reactions\tscore\terrors\tfwer\n");
		for (PermutationResultFwer r : networks) {
			out.write(r.network.getName() + "\t" +r.network.getReactions().size()+"\t"+ r.score + "\t" + r.errors + "\t" + r.fwer + "\n");
		}
		out.close();
	}
}