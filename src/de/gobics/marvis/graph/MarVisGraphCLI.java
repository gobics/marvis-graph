package de.gobics.marvis.graph;

import de.gobics.marvis.graph.downloader.KeggCreateNetworkProcess;
import de.gobics.marvis.graph.gui.tasks.SaveNetwork;
import de.gobics.marvis.utils.LoggingUtils;
import de.gobics.marvis.utils.task.AbstractTask.State;
import de.gobics.marvis.utils.task.TaskListener;
import jargs.gnu.CmdLineParser;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command line interface to MarVis Graph. This class can be used for various
 * long-running tasks that to not need interaction, e.g.: <ul> <li>create new
 * networks via KEGG api</li> <li>...</li> </ul>
 *
 * <strong>Command line arguments</strong> <ul>
 * <li>--output-file=&lt;filename&gt;<br />output file for the results.
 * Depending on the job to perform, the output file will be of different file
 * type. <li>--create-kegg-network=&lt;organism id&gt;<br />creates a new
 * network and downloads the data via the KEGG API. The network is stores in the
 * output filename.</li> </ul>
 *
 * @author manuel
 */
public class MarVisGraphCLI {

	private static final Logger logger = Logger.getLogger(MarVisGraphCLI.class.getName());
	private static File output_file = null;
	private static String create_kegg_network = null;

	public static void main(String[] args) throws Throwable {
		getopt(args);

		if (create_kegg_network != null && output_file != null) {
			createKeggNetwork(create_kegg_network, output_file);
		}
	}

	public static String[] getopt(String[] args) throws Exception {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option verbose = parser.addBooleanOption('v', "verbose");
		CmdLineParser.Option result_file = parser.addStringOption("output-file");
		CmdLineParser.Option create_kegg = parser.addStringOption("create-kegg-network");
		parser.parse(args);

		// Where to put the result file
		Object value = parser.getOptionValue(verbose);
		if (value != null && value.equals(Boolean.TRUE)) {
			LoggingUtils.initLogger(Level.FINER);
		}
		else {
			LoggingUtils.initLogger(Level.FINE);
		}
		value = parser.getOptionValue(result_file);
		if (value != null) {
			String filename_result = value.toString();
			output_file = new File(filename_result);
		}

		value = parser.getOptionValue(create_kegg);
		if (value != null) {
			create_kegg_network = value.toString();
		}

		return parser.getRemainingArgs();
	}

	private static void createKeggNetwork(String org, File output) throws Throwable {
		logger.info("Starting download of KEGG Network for: " + org);
		KeggCreateNetworkProcess process = new KeggCreateNetworkProcess(org);
		process.addTaskListener(new TaskListener<Void>() {

			@Override
			public void setTaskProgress(int percentage) {
				System.out.println("Process at "+percentage+"%");
			}

			@Override
			public void addTaskResult(Void result) {
				// ignore
			}

			@Override
			public void setTaskDescription(String new_description) {
				System.out.println(new_description);
			}

			@Override
			public void setTaskTitle(String new_title) {
				// ignore
			}

			
			@Override
			public void setTaskState(State state) {
				//Ignore
			}

			@Override
			public void throwsException(Throwable t) {
				t.printStackTrace();
			}
		});
		MetabolicNetwork result = process.perform();
		new SaveNetwork(result, output).perform();
	}
}