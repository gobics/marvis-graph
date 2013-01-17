package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.downloader.MetabolicNetworkTester;
import de.gobics.marvis.graph.downloader.NetworkDownloaderDialog;
import de.gobics.marvis.graph.gui.actions.*;
import de.gobics.marvis.graph.gui.tasks.*;
import de.gobics.marvis.graph.sort.AbstractGraphScore;
import de.gobics.marvis.utils.LoggingUtils;
import de.gobics.marvis.utils.swing.AbstractTaskListener;
import de.gobics.marvis.utils.swing.Statusbar;
import de.gobics.marvis.utils.swing.Statusdialog;
import de.gobics.marvis.utils.swing.filechooser.ChooserAbstract;
import de.gobics.marvis.utils.swing.filechooser.ChooserExcel;
import de.gobics.marvis.utils.swing.filechooser.FileFilterCef;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
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
	private final Statusdialog status_dialog = new Statusdialog(this);

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
		menu.add(new JSeparator());
		menu.add(new JMenuItem(new ActionCalculateNetworks(this, treemodel_networks)));
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

		final LoadNetwork process = new LoadNetwork(input);
		process.addPropertyChangeListener(new AbstractTaskListener() {
			@Override
			public void receiveStatusDone() {
				if (process.isCancelled()) {
					return;
				}
				try {
					setNetwork(process.get());
				}
				catch (Throwable ex) {
					logger.log(Level.SEVERE, "can not load network from '" + input.
							getAbsolutePath() + "': ", ex);
					display_error("Can not load graph", ex);
				}
			}

			@Override
			public void receiveError(String msg) {
				display_error(msg);
			}

			@Override
			public void receiveException(Exception ex) {
				display_error("Can not load network", ex);
			}
		});

		monitorTask(process);

		process.execute();
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
		monitorTask(process);
		process.execute();
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
		JInternalFrame iframe = new InternalFrameNodeInformation(getMainNetwork(), o);
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
	public static void main(String[] args) {
		LoggingUtils.initLogger(Level.FINER);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MarvisGraphMainWindow main = new MarvisGraphMainWindow();
				main.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				main.setVisible(true);
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

	/**
	 * Updates the graph information. This event will be fired when a graph is
	 * selected in the JTree
	 */
	private void updateGraphInformation() {
		MetabolicNetwork n = getSelectedNetwork();
		panel_graph_information.display(n);
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
			return;
		}

		// Create a dialog asking for the options
		DialogCalculateNetworks dialog = new DialogCalculateNetworks(this);
		dialog.setVisible(true);
		if (dialog.aborted()) {
			return;
		}

		// Create the process
		final SwingWorker<MetabolicNetwork[], Void> process = dialog.getTask(n);

		// Asynchronous fetching of the result
		process.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("state") && evt.getNewValue().
						equals(SwingWorker.StateValue.DONE)) {
					try {
						MetabolicNetwork[] networks = process.get();
						logger.log(Level.FINER, "Got results: {0}", Arrays.toString(networks));
						//sortSubnetworks(networks);
						if (networks != null) {
							treemodel_networks.setSubnetworks(networks);
						}

						sort_subnetworks();
					}
					catch (Exception ex) {
						logger.log(Level.SEVERE, "Can not get calculated networks: ", ex);
					}
				}
			}
		});

		// Initialize monitoring features
		monitorTask(process);

		// Start the process
		process.execute();

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

		final AbstractGraphScore sorter = combobox_graph_sort.getSorterFor(main);
		final SortNetworksTask process = new SortNetworksTask(sorter, getMainNetwork(), subnetworks);
		process.addPropertyChangeListener(new AbstractTaskListener() {
			@Override
			public void receiveStatusDone() {
				logger.finer("Sorted networks are ready. Will now display the results.");
				if (process.isCancelled()) {
					return;
				}

				MetabolicNetwork[] networks = null;
				try {
					networks = process.get();
				}
				catch (Exception ex) {
					logger.log(Level.SEVERE, "", ex);
				}
				if (networks == null) {
					return;
				}
				treemodel_networks.setSubnetworks(networks);

			}

			@Override
			public void receiveError(String msg) {
				MarvisGraphMainWindow.this.display_error(msg);
			}

			@Override
			public void receiveException(Exception ex) {
				MarvisGraphMainWindow.this.display_error("Exception during sub-network sort", ex);
			}
		});
		monitorTask(process);
		process.execute();
	}

	/**
	 * Main method to import metabolites. The window will display a chooser to
	 * select the file to import.
	 */
	public void importMetabolites() {
		ChooserAbstract chooser = FileChooserMetabolicMarker.getInstance();
		chooser.setMultiSelectionEnabled(true);
		chooser.showOpenDialog(this);
		File[] files = chooser.getSelectedFiles();

		if (files.length == 0) {
			return;
		}

		if (chooser.getFileFilter() instanceof FileFilterCef) {
			importMetabolitesCef(files);
		}
		else {
			importMetabolitesCSV(files);
		}
	}

	/**
	 * Import the given input file.
	 *
	 * @param input_file
	 */
	public void importMetabolitesCSV(final File input_file) {
		importMetabolitesCSV(new File[]{input_file});
	}

	/**
	 * Import the given input files.
	 *
	 * @param input_files
	 */
	public void importMetabolitesCSV(final File[] input_files) {
		if (input_files.length < 1) {
			logger.warning("Can not import from an empty file list");
			return;
		}

		for (File f : input_files) {
			if (!f.exists() || !f.canRead()) {
				logger.severe("File does not exist or is not readable: " + f.
						getAbsolutePath());
				display_error("File does not exist or is not readable:\n" + f.
						getAbsolutePath());
			}
		}

		if (input_files.length > 1) {
			JOptionPane.showMessageDialog(this, "You like to import several files at once. Please\n"
					+ "keep in mind that all files have to be formated\n"
					+ "in the same way!", "Warning", JOptionPane.WARNING_MESSAGE);
		}

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

		DialogImportMetabolicsOptions dialog = new DialogImportMetabolicsOptions(this, input_files[0]);
		dialog.setVisible(true);
		if (!dialog.closedWithOk()) {
			return;
		}

		final ImportMetabolicMarkerCSV process = dialog.getProcess(network);
		if (process == null) {
			return;
		}
		process.setInputFiles(input_files);

		monitorTask(process);

		// Asynchronous fetching of the result
		process.addPropertyChangeListener(new AbstractTaskListener() {
			@Override
			public void receiveStatusDone() {
				try {

					MetabolicNetwork network = process.get();
					if (network == null) {
						return;
					}

					if (MarvisGraphMainWindow.this.confirm("Imported " + network.
							getMarkers().size() + " metabolic marker. Accept?", "Import result")) {
						setNetwork(network);
						if (MarvisGraphMainWindow.this.confirm("Perform an annotation of the metabolic marker?", "Perform annotation")) {
							annotateMarker();
						}
					}

				}
				catch (Exception ex) {
					display_error("Can not import metabolic marker: ", ex);
					logger.log(Level.SEVERE, "Can not import metabolic marker: ", ex);
				}

			}

			@Override
			public void receiveError(String msg) {
				display_error(msg);
			}

			@Override
			public void receiveException(Exception ex) {
				display_error("Exception during import of metabolic data", ex);
			}
		});
		process.execute();
	}

	/**
	 * Import metabolic data from Agilent CEF files.
	 *
	 * @param input_files
	 */
	public void importMetabolitesCef(final File[] input_files) {
		final MetabolicNetwork network = getMainNetwork();
		if (network == null) {
			display_error("Please load a network first");
			return;
		}

		if (input_files.length < 1) {
			logger.warning("Can not import from an empty file list");
			return;
		}

		for (File f : input_files) {
			if (!f.exists() || !f.canRead()) {
				logger.severe("File does not exist or is not readable: " + f.
						getAbsolutePath());
				display_error("File does not exist or is not readable:\n" + f.
						getAbsolutePath());
			}
		}

		final ImportMetabolicMarkerCef process = new ImportMetabolicMarkerCef(network);
		process.setInputFiles(input_files);
		monitorTask(process);

		// Asynchronous fetching of the result
		process.addPropertyChangeListener(new AbstractTaskListener() {
			@Override
			public void receiveStatusDone() {
				try {

					MetabolicNetwork network = process.get();
					if (network == null) {
						return;
					}

					if (MarvisGraphMainWindow.this.confirm("Imported " + network.
							getMarkers().size() + " metabolic marker. Accept?", "Import result")) {
						setNetwork(network);
						if (MarvisGraphMainWindow.this.confirm("Perform an annotation of the metabolic marker?")) {
							annotateMarker();
						}
					}

				}
				catch (Exception ex) {
					display_error("Can not import metabolic marker: ", ex);
					logger.log(Level.SEVERE, "Can not import metabolic marker: ", ex);
				}

			}

			@Override
			public void receiveError(String msg) {
				display_error(msg);
			}

			@Override
			public void receiveException(Exception ex) {
				display_error("Can not import metabolic marker: ", ex);
			}
		});
		process.execute();
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
		JPanel dialog_panel = new JPanel();
		dialog_panel.setLayout(new BoxLayout(dialog_panel, BoxLayout.PAGE_AXIS));
		SpinnerNumberModel spinner_model = new SpinnerNumberModel(0.005, 0, Double.MAX_VALUE, 0.001);
		dialog_panel.add(new JLabel("Specify the mass range (in u):"));
		JSpinner spinner = new JSpinner(spinner_model);
		Dimension size = spinner.getSize();
		size.width = 50;
		spinner.setSize(size);
		spinner.setPreferredSize(size);
		spinner.setMaximumSize(size);
		dialog_panel.add(spinner);
		JOptionPane.showMessageDialog(this, dialog_panel, "Annotate marker", JOptionPane.QUESTION_MESSAGE);
		Number range = spinner_model.getNumber();

		final AnnotateMarker annotate_process = new AnnotateMarker(network);
		annotate_process.setMassRange(Math.abs(range.doubleValue()));

		monitorTask(annotate_process);
		// Asynchronous fetching of the result
		annotate_process.addPropertyChangeListener(new AbstractTaskListener() {
			@Override
			public void receiveStatusDone() {
				try {
					MetabolicNetwork network = annotate_process.get();
					if (network == null) {
						return;
					}
					int count_annotations = network.getRelations(RelationshipType.MARKER_ANNOTATION_COMPOUND).
							size();

					if (confirm("Found " + count_annotations + " annotations. Accept?", "Check annotations")) {
						setNetwork(network);
					}
				}
				catch (Exception ex) {
					display_error("Can not get annotate markers: ", ex);
					logger.log(Level.SEVERE, "Can not get annotate markers: ", ex);
				}
			}

			@Override
			public void receiveError(String msg) {
				display_error(msg);
			}

			@Override
			public void receiveException(Exception ex) {
				display_error("Can not annotate metabolic marker: ", ex);
			}
		});

		annotate_process.execute();
	}

	public void importTranscripts() {
		ChooserExcel chooser = ChooserExcel.getInstance();
		File input_file = chooser.doChooseFileOpen(this);
		if (input_file == null) {
			return;
		}
		importTranscripts(input_file);
	}

	private void importTranscripts(final File input_file) {
		if (!input_file.exists()) {
			display_error("Input file does not exist: " + input_file.
					getAbsolutePath());
			return;
		}
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

		DialogImportTranscriptomicsOptions dialog;
		try {
			dialog = new DialogImportTranscriptomicsOptions(this, input_file);
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Can not open dialog for import options: ", ex);
			display_error("Can not open dialog for import options", ex);
			return;
		}
		dialog.setVisible(true);
		if (!dialog.closedWithOk()) {
			return;
		}

		final ImportTranscriptomicsExcel process = dialog.getProcess(network);
		if (process == null) {
			return;
		}

		monitorTask(process);
		// Asynchronous fetching of the result
		process.addPropertyChangeListener(new AbstractTaskListener() {
			@Override
			public void receiveError(String msg) {
				MarvisGraphMainWindow.this.display_error(msg);
			}

			@Override
			public void receiveException(Exception ex) {
				MarvisGraphMainWindow.this.display_error("Exception during transcript import", ex);
			}

			@Override
			public void receiveStatusDone() {
				if (process.isCancelled()) {
					return;
				}
				MetabolicNetwork network = null;
				try {
					network = process.get();
				}
				catch (Exception ex) {
					logger.log(Level.SEVERE, "Import canceled: ", ex);
				}
				if (network == null) {
					return;
				}


				int counter_transcripts = 0;
				int counter_annotated = 0;
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
		process.execute();
	}

	public void createNewNetwork() {
		new NetworkDownloaderDialog(this).setVisible(true);
	}

	public void monitorTask(SwingWorker process) {
		statusbar.monitorTask(process);
		status_dialog.monitorTask(process);
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
		MetabolicNetworkTester tester = new MetabolicNetworkTester(network);
		String report = tester.generateReport();
		display_information(report);
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
			ComboBoxGraphobject<Pathway> cb = new ComboBoxGraphobject<Pathway>(pathways);
			JOptionPane.showMessageDialog(this, cb);
			pathway = cb.getSelectGraphObject();
		}

		displayPathwayAtKegg(network, pathway);
	}

	public void displayPathwayAtKegg(Pathway path) {
		displayPathwayAtKegg(getMainNetwork(), path);
	}

	public void displayPathwayAtKegg(MetabolicNetwork network, Pathway path) {

		StringBuilder sb = new StringBuilder("http://www.genome.jp/kegg-bin/show_pathway?").
				append(path.getId().replace("path:", ""));
		for (GraphObject c : network.getCompounds()) {
			if (network.isExplainable(c)) {
				sb.append("/").append(c.getId()).append("%09,red");
			}
		}
		for (GraphObject r : network.getReactions()) {

			if (network.isExplainable(r)) {
				sb.append("/").append(r.getId()).append("%09,red");
			}
		}
		for (GraphObject r : network.getGenes()) {
			if (network.isExplainable(r)) {
				sb.append("/").append(r.getId()).append("%09,red");
			}
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
			JTextArea area = new JTextArea(url.toASCIIString());
			area.setWrapStyleWord(false);
			JScrollPane spane = new JScrollPane(area);
			spane.setPreferredSize(new Dimension(300, 100));
			JOptionPane.showConfirmDialog(this, new JScrollPane(area), "URL", JOptionPane.OK_OPTION);
		}
		return false;
	}
}
