package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.downloader.NetworkDownloaderDialog;
import de.gobics.marvis.graph.gui.actions.*;
import de.gobics.marvis.graph.gui.evaluation.TableModelResults;
import de.gobics.marvis.graph.gui.tasks.*;
import de.gobics.marvis.graph.sort.AbstractGraphScore;
import de.gobics.marvis.utils.LoggingUtils;
import de.gobics.marvis.utils.io.TabularDataReader;
import de.gobics.marvis.utils.swing.SpringUtilities;
import de.gobics.marvis.utils.swing.Statusbar;
import de.gobics.marvis.utils.swing.Statusdialog2;
import de.gobics.marvis.utils.swing.TaskWrapper;
import de.gobics.marvis.utils.swing.filechooser.ChooserExcel;
import de.gobics.marvis.utils.swing.filechooser.ChooserTabularData;
import de.gobics.marvis.utils.task.AbstractTask;
import de.gobics.marvis.utils.task.AbstractTask.State;
import de.gobics.marvis.utils.task.AbstractTaskListener;
import de.gobics.marvis.utils.task.TaskResultListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.TreePath;

public class MarvisGraphMainWindow extends JFrame {

	private static final Logger logger = Logger.getLogger(MarvisGraphMainWindow.class.
			getName());
	private final Statusbar statusbar = new Statusbar();
	private final ComboBoxGraphSort combobox_graph_sort = new ComboBoxGraphSort();
	private final TreeModelNetworks treemodel_networks = new TreeModelNetworks(null, new MetabolicNetwork[0]);
	private final JTree jtree_networks = new JTree(treemodel_networks);
	private final PanelGraphInformation panel_graph_information = new PanelGraphInformation(jtree_networks);
	private final JDesktopPane desktop = new JDesktopPane();
	private final Statusdialog2 status_dialog = new Statusdialog2(this);
	private AbstractNetworkCalculation calculate_network_task = null;

	public MarvisGraphMainWindow() {
		super("MarVis-Graph v0.2");
		logger.finer("Initializing new window");
		getContentPane().setLayout(new BorderLayout());
		JSplitPane main_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		main_split.setOneTouchExpandable(true);
		getContentPane().add(main_split, BorderLayout.CENTER);
		getContentPane().add(statusbar, BorderLayout.PAGE_END);

		combobox_graph_sort.setAction(new ActionSortSubnetworks(this, treemodel_networks));

		jtree_networks.setCellRenderer(new TreeGraphCellRenderer());

		// Inititalize the menubar
		setJMenuBar(new JMenuBar());
		JMenu menu = new JMenu("File");
		getJMenuBar().add(menu);
		menu.setMnemonic(KeyEvent.VK_F);
		menu.add(new JMenuItem(new ActionLoadNetwork(this)));
		menu.add(new JMenuItem(new ActionSaveNetwork(this, jtree_networks)));
		menu.add(new JSeparator());
		menu.add(new JMenuItem(new ActionCreateNetwork(this)));
		menu.add(new JMenuItem(new ActionReduceNetwork(this, treemodel_networks)));
		menu.add(new JSeparator());
		menu.add(new JMenuItem(new ActionCalculateNetworks(this, treemodel_networks)));
		menu.add(new JMenuItem(new ActionPermutationTest(this, treemodel_networks)));
		menu.add(new JSeparator());
		menu.add(new JMenuItem(new ActionExit(this)));

		menu = new JMenu("Data");
		menu.setMnemonic(KeyEvent.VK_D);
		getJMenuBar().add(menu);
		menu.add(new JMenuItem(new ActionImportMetabolites(this, treemodel_networks)));
		menu.add(new JMenuItem(new ActionAnnotateMetabolicMarker(this, treemodel_networks)));
		menu.add(new JSeparator());
		menu.add(new JMenuItem(new ActionImportTranscripts(this, treemodel_networks)));

		getJMenuBar().add(new MenuWindows(this));


		// Install Popup menu for JTree
		new PopupMenuNetworksTree(this, jtree_networks);


		// Display the left split pane
		JPanel left_main = new JPanel();
		GroupLayout left_layout = new GroupLayout(left_main);
		left_main.setLayout(left_layout);
		left_layout.setAutoCreateGaps(true);
		left_layout.setAutoCreateContainerGaps(true);

		JScrollPane spane_tree = new JScrollPane(jtree_networks);
		JButton button_draw = new JButton(new ActionDrawNetwork(this, jtree_networks));

		left_layout.setHorizontalGroup(
				left_layout.createParallelGroup().addComponent(combobox_graph_sort).
				addComponent(spane_tree).addComponent(button_draw, button_draw.
				getMinimumSize().width, button_draw.getPreferredSize().width, Short.MAX_VALUE).
				addComponent(panel_graph_information, panel_graph_information.
				getMinimumSize().width, panel_graph_information.getPreferredSize().width, Short.MAX_VALUE));
		left_layout.setVerticalGroup(
				left_layout.createSequentialGroup().addComponent(combobox_graph_sort, combobox_graph_sort.
				getMinimumSize().height, combobox_graph_sort.getPreferredSize().height, combobox_graph_sort.
				getPreferredSize().height).addComponent(spane_tree).addComponent(button_draw).
				addComponent(panel_graph_information, panel_graph_information.
				getMinimumSize().height, panel_graph_information.
				getPreferredSize().height, panel_graph_information.
				getPreferredSize().height));
		main_split.setLeftComponent(left_main);


		// Display a desktop on the right side
		main_split.setRightComponent(desktop);
		desktop.setPreferredSize(new Dimension(300, 300));

		pack();
		statusbar.display("MarVis-Graph started");
	}

	public JDesktopPane getDesktop() {
		return desktop;
	}

	public void loadNetwork() {
		final File input = ChooserArchive.getInstance().doChooseFileOpen(this);
		if (input == null) {
			return;
		}
		loadNetwork(input);
	}

	public void loadNetwork(File input) {
		final LoadNetwork process = new LoadNetwork(input);
		process.addTaskListener(new AbstractTaskListener<Void>() {
			@Override
			public void setTaskState(State state) {
				if (process.isDone()) {
					setNetwork(process.getTaskResult());
				}
			}
		});

		executeTask(process);
	}

	public void saveNetwork() {
		MetabolicNetwork network = getMainNetwork();
		if (network == null) {
			display_error("Please select a network first.");
		}
		saveNetwork(network);
	}

	public void saveNetwork(final MetabolicNetwork n) {
		final File output_file = ChooserArchive.getInstance().doChooseFileSave(this);
		if (output_file == null) {
			return;
		}
		final SaveNetwork process = new SaveNetwork(n, output_file);
		executeTask(process);
	}

	/**
	 * Create a JInternalFrame to display the graph. The internal frame will be
	 * added to the JDesktopPane.
	 *
	 * Note: This is a shortcut for
	 *
	 * @{code createNetworkVisualization(getSelectedNetwork())} including error
	 * checking
	 */
	public void drawNetwork() {
		MetabolicNetwork n = getSelectedNetwork();
		if (n != null) {
			createNetworkVisualization(n);
		}
	}

	public void createNetworkVisualization(MetabolicNetwork n) {
		JInternalFrame iframe = new InternalFrameGraph(this, n);
		desktop.add(iframe);
		desktop.moveToFront(iframe);
		iframe.setVisible(true);
	}

	public void createGraphobjectVisualization(GraphObject o) {
		if (o == null) {
			return;
		}
		JInternalFrame iframe = new InternalFrameNodeInformation(this, getMainNetwork(), o);
		desktop.add(iframe);
		desktop.moveToFront(iframe);
		iframe.setVisible(true);
	}

	public void exit() {
		int ret = JOptionPane.showConfirmDialog(this, "Are your shure to exit?", "Exit", JOptionPane.YES_NO_OPTION);
		if (ret != JOptionPane.OK_OPTION) {
			return;
		}
		logger.finer("Will now dispose");
		this.dispose();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(final String[] args) {
		LoggingUtils.initLogger(Level.FINER);
		final MarvisGraphMainWindow main = new MarvisGraphMainWindow();
		main.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				main.setVisible(true);
				if (args.length > 0) {
					main.loadNetwork(new File(args[0]));
				}
			}
		});
	}

	public MetabolicNetwork getMainNetwork() {
		return treemodel_networks.getRootNetwork();
	}

	private MetabolicNetwork[] getSubnetworks() {
		return treemodel_networks.getSubnetworks();
	}

	private MetabolicNetwork getSelectedNetwork() {
		if (jtree_networks.getSelectionCount() < 1) {
			return null;
		}

		Object o = jtree_networks.getSelectionPath().getLastPathComponent();
		if (!(o instanceof MetabolicNetwork)) {
			this.display_error("The object in the tree is not of type MetabolicNetwork but: " + o.
					getClass().getName());
			return null;
		}
		return (MetabolicNetwork) o;
	}

	/**
	 * Set the main network to the given one.
	 *
	 * @param new_network
	 */
	public void setNetwork(MetabolicNetwork new_network) {
		for (JInternalFrame f : desktop.getAllFrames()) {
			f.dispose();
		}
		treemodel_networks.setRoot(new_network);
		jtree_networks.getSelectionModel().setSelectionPath(new TreePath(new_network));
	}

	public void reduceNetwork() {
		// Fetch the main network 
		MetabolicNetwork n = getMainNetwork();
		if (n == null) {
			display_error("Please load or create a metabolic network first.");
			return;
		}

		final ReduceNetwork process = new ReduceNetwork(n);
		process.addTaskListener(new TaskResultListener<Void>() {
			@Override
			public void taskDone() {
				if (!process.isCanceled()) {
					setNetwork(process.getTaskResult());
				}
			}
		});

		executeTask(process);
	}

	/**
	 * Initialises the process of subnetwork calculation. The user will be asked
	 * for required options. A swingworker performing the calculation will be
	 * created. The result will be fetched asynchronous and stored in the tree
	 * model. The statusbar and a statusdialog will monitor the applications
	 * status and present it to the user.
	 */
	public void calculateSubnetworks() {
		// Fetch the main network 
		MetabolicNetwork n = getMainNetwork();
		if (n == null) {
			display_error("Please load or create a metabolic network first.");
			return;
		}

		// Create a dialog asking for the options
		DialogCalculateNetworks dialog = new DialogCalculateNetworks(this);
		dialog.setVisible(true);
		if (dialog.aborted()) {
			return;
		}

		// Create the process
		final AbstractNetworkCalculation task = dialog.getTask(n);
		calculate_network_task = task;

		task.addTaskListener(new TaskResultListener<Void>() {
			@Override
			public void taskDone() {
				MetabolicNetwork[] networks = task.getTaskResult();
				logger.log(Level.FINER, "Got results: {0}", Arrays.toString(networks));
				//sortSubnetworks(networks);
				if (networks != null) {
					treemodel_networks.setSubnetworks(networks);
				}

				sort_subnetworks();
			}
		});
		task.addTaskListener(new AbstractTaskListener<Void>() {
			@Override
			public void throwsException(Throwable t) {
				if (t instanceof java.lang.OutOfMemoryError) {
					display_error("Not enough memory to calculate sub-networks.");
				}
				else {
					display_error("Severe error while calculating networks: ", t);
				}
			}
		});

		executeTask(task);
	}

	/**
	 * Display the error message in a modal dialog.
	 *
	 * @param message
	 */
	public void display_error(String message) {
		display_error(message, null);
	}

	/**
	 * Display the given message and exception in an error dialog.
	 *
	 * @param message the message to display
	 * @param e the exception to display
	 */
	public void display_error(String message, Throwable e) {
		ErrorDialog.show(this, message, e);
	}

	/**
	 * Sort the sub-networks available.
	 */
	public void sort_subnetworks() {
		MetabolicNetwork[] subnetworks = getSubnetworks();
		logger.finer("Got " + subnetworks.length + " sub networks");
		sortSubnetworks(subnetworks);
	}

	/**
	 * Sort the given sub-networks. The sorting method depends on the selected
	 * sorting algorithm.
	 *
	 * @param subnetworks
	 */
	public void sortSubnetworks(final MetabolicNetwork[] subnetworks) {
		if (subnetworks == null || subnetworks.length == 0) {
			return;
		}
		MetabolicNetwork main = getMainNetwork();
		if (main == null) {
			return;
		}

		final AbstractGraphScore sorter = combobox_graph_sort.getSorterFor(main, calculate_network_task);
		final SortNetworksTask task = new SortNetworksTask(sorter, getMainNetwork(), subnetworks);
		task.addTaskListener(new TaskResultListener<Void>() {
			@Override
			public void taskDone() {
				MetabolicNetwork[] networks = task.getTaskResult();
				treemodel_networks.setSubnetworks(networks);
			}
		});

		executeTask(task);
	}

	/**
	 * Main method to import metabolites. The window will display a chooser to
	 * select the file to import.
	 */
	public void importMetabolites() {
		ChooserTabularData chooser = ChooserTabularData.getInstance();
		chooser.setMultiSelectionEnabled(false);
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		try {
			TabularDataReader reader = chooser.getDataReader();
			importMetabolites(reader);
		}
		catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
			display_error("Can not open file", ex);
		}
	}

	/**
	 * Import the given input files.
	 *
	 * @param input_files
	 */
	public void importMetabolites(TabularDataReader reader) {
		MetabolicNetwork network = getMainNetwork();
		if (network == null) {
			display_error("Please load a metabolic network first");
		}

		// If there are markers in the network it is better to remove them!
		if (!network.getMarkers().isEmpty()) {
			if (confirm("Network already contains marker. Remove them?\n\nWarning: If you import markers with a different number of intensities\n this can lead to serious problems.", "Warning")) {
				for (GraphObject m : network.getMarkers()) {
					network.remove(m);
				}
			}
		}

		DialogImportMetabolicsOptions dialog = new DialogImportMetabolicsOptions(this, reader);
		if (!dialog.showDialog()) {
			return;
		}

		final ImportAbstract process = dialog.createProcess(network);
		if (process == null) {
			return;
		}

		process.addTaskListener(new TaskResultListener<Void>() {
			@Override
			public void taskDone() {
				MetabolicNetwork network = process.getTaskResult();
				if (network == null) {
					return;
				}

				if (confirm("Imported " + network.
						getMarkers().size() + " metabolic marker. Accept?", "Import result")) {
					setNetwork(network);
					if (confirm("Perform an annotation of the metabolic marker?", "Perform annotation")) {
						annotateMarker();
					}
				}
			}
		});

		executeTask(process);
	}

	/**
	 * Annotate the given marker.
	 */
	public void annotateMarker() {
		MetabolicNetwork network = getMainNetwork();
		if (network == null) {
			display_error("Please load a graph first");
			return;
		}

		// Remove existing annotations
		if (!network.getRelations(RelationshipType.MARKER_ANNOTATION_COMPOUND).
				isEmpty()) {
			if (!confirm("There are already markers annotated. This\n"
					+ "annotations will be removed. Accept?", "Remove existing annotation?")) {
				return;
			}
			for (Relation r : network.getRelations(RelationshipType.MARKER_ANNOTATION_COMPOUND)) {
				network.removeRelation(r);
			}
		}

		// Ask for range accuracy
		JPanel dialog_panel = new JPanel(new BorderLayout());
		dialog_panel.add(new JLabel("Specify the mass range (in u):"), BorderLayout.PAGE_START);
		SpinnerNumberModel spinner_model = new SpinnerNumberModel(0.005, 0, Double.MAX_VALUE, 0.001);
		JSpinner spinner = new JSpinner(spinner_model);
		spinner.setPreferredSize(new Dimension(200, 50));
		dialog_panel.add(spinner, BorderLayout.CENTER);
		int res = JOptionPane.showConfirmDialog(this, dialog_panel, "Annotate marker", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (res != JOptionPane.OK_OPTION) {
			return;
		}
		double range = Math.abs(spinner_model.getNumber().doubleValue());

		final AnnotateMarker annotate_process = new AnnotateMarker(network);
		annotate_process.setMassRange(range);
		annotate_process.addTaskListener(new TaskResultListener<Void>() {
			@Override
			public void taskDone() {
				MetabolicNetwork network = annotate_process.getTaskResult();
				if (network == null) {
					return;
				}
				int count_annotations = network.getRelations(RelationshipType.MARKER_ANNOTATION_COMPOUND).
						size();

				if (confirm("Found " + count_annotations + " annotations. Accept?", "Check annotations")) {
					setNetwork(network);
				}
			}
		});
		executeTask(annotate_process);
	}

	public void importTranscripts() {
		ChooserTabularData chooser = ChooserTabularData.getInstance();
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		try {
			importTranscripts(chooser.getDataReader());
		}
		catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
			display_error("Can not open file for read", ex);
		}
	}

	private void importTranscripts(final TabularDataReader reader) {
		MetabolicNetwork network = getMainNetwork();
		if (network == null) {
			display_error("Please load a metabolic network first");
		}

		// If there are markers in the network it is better to remove them!
		if (!network.getTranscripts().isEmpty()) {
			if (confirm("Network already contains transcripts. Remove them?\n\n"
					+ "Warning: If you import transcripts with a different number of intensities\n"
					+ "this can lead to serious problems.", "Warning")) {
				for (GraphObject m : network.getTranscripts()) {
					network.remove(m);
				}
			}
		}

		DialogImportTranscriptomicsOptions dialog = new DialogImportTranscriptomicsOptions(this, reader);
		if (!dialog.showDialog()) {
			return;
		}

		final ImportAbstract process = dialog.getProcess(network);
		if (process == null) {
			return;
		}
		process.addTaskListener(new TaskResultListener<Void>() {
			@Override
			public void taskDone() {
				MetabolicNetwork network = process.getTaskResult();
				if (network == null) {
					return;
				}

				int counter_transcripts = 0;
				TreeSet<Reaction> reactions = new TreeSet<Reaction>();
				for (Transcript t : network.getTranscripts()) {
					counter_transcripts++;
					for (Gene g : network.getGenes(t)) {
						for (Enzyme e : network.getEncodedEnzymes(g)) {
							reactions.addAll(network.getReactions(e));
						}
					}
				}

				String message = "New network will contain " + counter_transcripts + " transcripts\n(" + reactions.
						size() + " with associated reaction). Accept?";
				if (MarvisGraphMainWindow.this.confirm(message, "Accept import")) {
					MarvisGraphMainWindow.this.setNetwork(network);
				}
			}
		});

		executeTask(process);
	}

	public void createNewNetwork() {
		new NetworkDownloaderDialog(this).setVisible(true);
	}

	/**
	 * Ensures, that the task is monitored and the run through a SwingWorker for
	 * background calculation. <strong>Note:</strong> it is not necessary to
	 * call
	 * <code>monitorTask()</code> separately, because this method will do this
	 * automatically.
	 *
	 * @param task
	 */
	public void executeTask(AbstractTask task) {
		statusbar.monitorTask(task);
		status_dialog.monitorTask(task);

		new TaskWrapper<>(task).execute();
	}

	public boolean confirm(String question) {
		return confirm(question, "Question");
	}

	/**
	 * Displays a message dialog showing the given question. It will return true
	 * if the user pressed the "yes" button
	 *
	 * @param question
	 * @param title
	 * @return
	 */
	public boolean confirm(String question, String title) {
		return JOptionPane.showConfirmDialog(this, question, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	public void display_information(String report) {
		Component message = new JLabel(report);
		if (report.length() > 50 || report.contains("\n")) {
			message = new JScrollPane(new JTextArea(report));
		}
		JOptionPane.showMessageDialog(this, message);
	}

	public void displayNetworkReport() {
		displayNetworkReport(getSelectedNetwork());
	}

	public void displayNetworkReport(MetabolicNetwork network) {
		final MetabolicNetworkReport tester = new MetabolicNetworkReport(network);
		tester.addTaskListener(new TaskResultListener<Void>() {
			@Override
			public void taskDone() {
				if (tester.isDone()) {
					display_information(tester.getTaskResult());
				}
			}
		});

		executeTask(tester);
	}

	public void displayPathwayAtKegg() {
		MetabolicNetwork network = getSelectedNetwork();
		if (network == null) {
			display_error("Please select a network first");
			return;
		}
		Collection<Pathway> pathways = network.getPathways();
		if (pathways.isEmpty()) {
			display_error("No pathway object exists in selected network");
			return;
		}

		Pathway pathway = pathways.iterator().next();
		if (pathways.size() > 1) {
			ComboBoxGraphobject<Pathway> cb = new ComboBoxGraphobject<>(pathways);
			if (JOptionPane.showConfirmDialog(this, cb, "Select pathway", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
				return;
			}
			pathway = cb.getSelectGraphObject();
		}

		displayPathwayAtKegg(network, pathway);
	}

	public void displayPathwayAtKegg(Pathway path) {
		displayPathwayAtKegg(getMainNetwork(), path);
	}

	public void displayPathwayAtKegg(MetabolicNetwork network, Pathway path) {

		StringBuilder sb = new StringBuilder("http://www.genome.jp/kegg-bin/show_pathway?").append(path.getId().replace("path:", ""));
		for (GraphObject c : network.getCompounds()) {
			sb.append("/").append(c.getId()).append("%09,").append(network.isExplainable(c) ? "red" : "%23cdb06e");
		}
		for (GraphObject r : network.getReactions()) {
			sb.append("/").append(r.getId()).append("%09,").append(network.isExplainable(r) ? "red" : "%23cdb06e");;

		}
		for (GraphObject r : network.getGenes()) {
			sb.append("/").append(r.getId()).append("%09,").append(network.isExplainable(r) ? "red" : "%23cdb06e");;

		}

		openBrowser(sb.toString());
	}

	/**
	 * Tries to open a browser for the given URL.
	 *
	 * @param url
	 */
	public boolean openBrowser(String url) {
		try {
			return openBrowser(new URI(url));
		}
		catch (URISyntaxException ex) {
			display_error("Can not parse url '" + url + "': ", ex);
		}
		return false;
	}

	/**
	 * Tries to open a browser for the given URL.
	 *
	 * @param url
	 */
	public boolean openBrowser(URI url) {
		try {
			logger.log(Level.FINE, "Try to browse: {0}", url);
			Desktop.getDesktop().browse(url);
			return true;
		}
		catch (Exception ex) {
			logger.log(Level.WARNING, "Can not open URL: ", ex);
			JTextArea area = new JTextArea(url.toASCIIString());
			area.setLineWrap(true);
			area.setWrapStyleWord(false);
			JScrollPane spane = new JScrollPane(area);
			spane.setPreferredSize(new Dimension(300, 100));
			JOptionPane.showMessageDialog(this, spane, "URL", JOptionPane.INFORMATION_MESSAGE);
		}
		return false;
	}

	public void performPermutationTest() {
		MetabolicNetwork main = getMainNetwork();
		if (main == null) {
			display_error("Please load a network first");
			return;
		}
		MetabolicNetwork[] subs = getSubnetworks();
		if (subs == null || subs.length == 0) {
			display_error("Please calculate subnetworks first");
			return;
		}

		JPanel options_panel = new JPanel(new SpringLayout());
		SpinnerNumberModel sm_permutations = new SpinnerNumberModel(1000, 1, Integer.MAX_VALUE, 1000);
		SpinnerNumberModel sm_threads = new SpinnerNumberModel(Runtime.getRuntime().availableProcessors(), 1, Integer.MAX_VALUE, 1);
		options_panel.add(new JLabel("No. of permutations:"));
		options_panel.add(new JSpinner(sm_permutations));
		options_panel.add(new JLabel("No. of CPUs to use:"));
		options_panel.add(new JSpinner(sm_threads));
		SpringUtilities.makeCompactGrid(options_panel);

		if (JOptionPane.showConfirmDialog(this, options_panel, "Set permutation options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) {
			return;
		}
		performPermutationTest(main, subs, sm_permutations.getNumber().intValue(), sm_threads.getNumber().intValue());
	}

	/**
	 * Perform an permutation test on the given networks with the given set of
	 * parameter.
	 *
	 * @param main the main/root network
	 * @param subs the identified sub-networks
	 * @param restart_probability restart-probability for the
	 * @param cofactor_threshold
	 * @param permutations
	 */
	private void performPermutationTest(MetabolicNetwork main, MetabolicNetwork[] subs, int number_of_permutations, int num_threads) {
		if (calculate_network_task == null) {
			display_error("Have no calculator stored. ");
			return;
		}

		final PermutationTest process = new PermutationTest(main, subs, calculate_network_task, combobox_graph_sort.getSorterFor(main, calculate_network_task));
		process.setNumberOfPermutations(number_of_permutations);
		process.setNumberOfThreads(num_threads);
		process.addTaskListener(new TaskResultListener<Void>() {
			@Override
			public void taskDone() {
				Set<PermutationTestResult> results = process.getTaskResult();
				logger.log(Level.FINER, "Permutation test returned {0} results", results.size());
				displayPermutationResults(results);

			}
		});

		executeTask(process);
	}

	/**
	 * Displays the results of a permutation test in a new
	 * {@link JInternalFrame}.
	 *
	 * @param results
	 */
	public void displayPermutationResults(Set<PermutationTestResult> results) {
		logger.log(Level.FINE, "Will now display {0} permutation results", results.size());
		JTable table = new JTable(new TableModelResults(results));
		table.setAutoCreateRowSorter(true);
		JScrollPane spane = new JScrollPane(table);
		spane.setPreferredSize(new Dimension(500, 400));
		JOptionPane.showMessageDialog(this, spane, "Results from permutation test", JOptionPane.INFORMATION_MESSAGE);
	}
}
