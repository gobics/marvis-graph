package de.gobics.marvis.graph;

import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.graph.gui.tasks.PermutationTest;
import de.gobics.marvis.graph.gui.tasks.PermutationTestResult;
import de.gobics.marvis.graph.sort.NetworkSorterSumOfWeights;
import de.gobics.marvis.utils.LoggingUtils;
import jargs.gnu.CmdLineParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class PermutationTestCLI {

	private final static Logger logger = Logger.getLogger(PermutationTestCLI.class.getName());
	private static File file_result;
	private static Double restart_probability;
	private static Integer number_of_permutations;
	private static Integer number_of_threads;
	private static Integer cofactor_threshold;

	public static void main(String[] args) throws Exception {
		LoggingUtils.initLogger(Level.FINER);
		args = parseOptions(args);

		if (args.length < 1) {
			usage("No metabolic network file given");
		}

		File file = new File(args[0]);
		if (!file.exists()) {
			usage("Metabolic network file does not exist");
		}
		if (!file.canRead()) {
			usage("Metabolic network file is not readable");
		}

		// Load the network
		LoadNetwork loader = new LoadNetwork(file);
		final MetabolicNetwork network = loader.load();

		logger.log(Level.INFO, "Permutating for restart probability of: {0}", restart_probability);
		CalculateNetworksRWR process = new CalculateNetworksRWR(network);
		process.setCofactorThreshold(cofactor_threshold);
		process.setRestartProbability(restart_probability);
		MetabolicNetwork[] subs = process.calculateNetworks(network);

		PermutationTest test = new PermutationTest(network, subs, process, new NetworkSorterSumOfWeights(network));
		test.setNumberOfPermutations(number_of_permutations);
		test.setNumberOfThreads(number_of_threads);

		Set<PermutationTestResult> networks = test.calculateScores(true);

		logger.log(Level.INFO, "Writing result to: {0}", file_result);
		BufferedWriter out = new BufferedWriter(new FileWriter(file_result));
		out.write("Name\tnum_reactions\tscore\tfwer\tfdr\n");
		for (PermutationTestResult r : networks) {
			out.write(r.network.getName() + "\t" + r.network.getReactions().size() + "\t" + r.score + "\t" + r.fwer + "\t" + r.fdr + "\n");
		}
		out.close();

		logger.log(Level.INFO, "Calculations for restart probability {0} is ready", restart_probability);
	}

	private static void usage(String error) {
		if (error != null && !error.isEmpty()) {
			System.err.println(error);
		}

		System.err.println("Usage: java " + PermutationTestCLI.class.getSimpleName() + " [OPTIONS] <input_file>");
		System.err.println("");
		System.err.println("   Perform a permutation test on the metabolic network given by <input_file>.  "
				+ ""
				+ " Possible options: "
				+ "  -h | --help"
				+ "       display this usage information"
				+ "  -v | --verbose"
				+ "       print verbose information (default: false)"
				+ "  -r | --result-file <filename>"
				+ "       Write the result to the given file (default: result.csv)"
				+ "  -p | --restart-probability <floating number>"
				+ "       The restart probability for the random-walk-algorithm (default: 0.8)"
				+ "  -c | --cofactor-threshold <number>"
				+ "       Number of reactions before considering a metabolite to be a co-factor (default: 10)"
				+ "  -n | --number-of-permutations <number>"
				+ "       Number of permutations to perform (default: 1000)"
				+ "  -t | --number-of-threads <number>"
				+ "       Number of permutations to perform (default: " + Runtime.getRuntime().availableProcessors() + ")"
				+ "");

		System.exit(error == null ? 0 : 1);
	}

	private static String[] parseOptions(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option help_option = parser.addBooleanOption('h', "help");
		CmdLineParser.Option verbose_option = parser.addBooleanOption('v', "verbose");
		CmdLineParser.Option result_file_option = parser.addStringOption('r', "result-file");
		CmdLineParser.Option restart_probability_option = parser.addDoubleOption('p', "restart-probability");
		CmdLineParser.Option cofactor_threshold_option = parser.addIntegerOption('c', "cofactor-threshold");
		CmdLineParser.Option num_permutations_option = parser.addIntegerOption('n', "number-of-permutations");
		CmdLineParser.Option num_threads_option = parser.addIntegerOption('t', "number-of-threads");

		try {
			parser.parse(args);
		}
		catch (CmdLineParser.OptionException e) {
			usage(e.getMessage());
		}

		if ((Boolean) parser.getOptionValue(help_option, Boolean.FALSE)) {
			usage(null);
		}

		// DEBUG or VERBOSE
		if ((Boolean) parser.getOptionValue(verbose_option, (Boolean) false)) {
			LoggingUtils.initLogger(Level.FINER);
		}
		else {
			LoggingUtils.initLogger(Level.INFO);
		}

		// Where to put the result file
		String filename_result = parser.getOptionValue(result_file_option, "result.csv").toString();
		file_result = new File(filename_result);
		restart_probability = (Double) parser.getOptionValue(restart_probability_option, 0.8D);
		cofactor_threshold = (Integer) parser.getOptionValue(cofactor_threshold_option, (Integer) 10);
		number_of_permutations = (Integer) parser.getOptionValue(num_permutations_option, (Integer) 1000);
		number_of_threads = (Integer) parser.getOptionValue(num_threads_option, (Integer) Runtime.getRuntime().availableProcessors());

		return parser.getRemainingArgs();
	}
}
