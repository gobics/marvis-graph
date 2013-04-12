package de.gobics.marvis.graph;

import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.graph.gui.tasks.PermutationTest;
import de.gobics.marvis.graph.gui.tasks.PermutationTestResult;
import de.gobics.marvis.graph.sort.AbstractGraphScore;
import de.gobics.marvis.graph.sort.NetworkSorterDiameter;
import de.gobics.marvis.graph.sort.NetworkSorterSumOfWeights;
import de.gobics.marvis.utils.LoggingUtils;
import de.gobics.marvis.utils.task.AbstractTask.State;
import de.gobics.marvis.utils.task.TaskListener;
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
	private static AbstractGraphScore scorer;

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

		// Build scorer
		scorer.setParent(network);

		logger.log(Level.INFO, "Calculating sub-networks for restart probability of: {0}", restart_probability);

		CalculateNetworksRWR process = new CalculateNetworksRWR(network);
		process.setRestartProbability(restart_probability);
		process.setCofactorThreshold(cofactor_threshold);
		MetabolicNetwork[] subs = process.calculateNetworks();

		logger.log(Level.INFO, "Doing {1} permutations with {0} threads", new Object[]{number_of_threads, number_of_permutations});
		PermutationTest test = new PermutationTest(network, subs, process, scorer);
		test.setNumberOfPermutations(number_of_permutations);
		test.setNumberOfThreads(number_of_threads);
		test.addTaskListener(new TaskListener<Void>() {
			@Override
			public void setTaskProgress(int percentage) {
				logger.log(Level.INFO, "Progress of permutation process: {0} %", percentage);
			}

			@Override
			public void addTaskResult(Void result) {
				// ignore
			}

			@Override
			public void setTaskDescription(String new_description) {
				// ignore
			}

			@Override
			public void setTaskTitle(String new_title) {
				// ignore
			}

			@Override
			public void throwsException(Throwable t) {
				logger.log(Level.SEVERE, "Calculation resulted in exception: ", t);
			}

			@Override
			public void setTaskState(State state) {
				// ignore
			}
		});

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
		System.err.println("   Perform a permutation test on the metabolic network given by <input_file>.  \n"
				+ "\n"
				+ " Possible options: \n"
				+ "  -h | --help\n"
				+ "       display this usage information\n"
				+ "  -v | --verbose\n"
				+ "       print verbose information (default: false)\n"
				+ "  -r | --result-file <filename>\n"
				+ "       Write the result to the given file (default: result.csv)\n"
				+ "  -p | --restart-probability <floating number>\n"
				+ "       The restart probability for the random-walk-algorithm (default: 0.8)\n"
				+ "  -c | --cofactor-threshold <number>\n"
				+ "       Number of reactions before considering a metabolite to be a co-factor (default: 10)\n"
				+ "  -d | --diameter\n"
				+ "       Use diameter as score instead of sum-of-weights\n"
				+ "  -n | --number-of-permutations <number>\n"
				+ "       Number of permutations to perform (default: 1000)\n"
				+ "  -t | --number-of-threads <number>\n"
				+ "       Number of permutations to perform (default: " + Runtime.getRuntime().availableProcessors() + ")\n"
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
		CmdLineParser.Option diameter_option = parser.addBooleanOption('d', "diameter");
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
		scorer = ((Boolean) parser.getOptionValue(diameter_option, false)).booleanValue() ? new NetworkSorterDiameter() : new NetworkSorterSumOfWeights();

		return parser.getRemainingArgs();
	}
}
