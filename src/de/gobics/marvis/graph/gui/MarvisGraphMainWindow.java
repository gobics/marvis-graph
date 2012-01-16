/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.Relation.RelationshipType;
import de.gobics.marvis.graph.gui.actions.*;
import de.gobics.marvis.graph.gui.tasks.*;
import de.gobics.marvis.graph.sort.AbstractGraphSort;
import de.gobics.marvis.utils.LoggingUtils;
import de.gobics.marvis.utils.swing.Statusbar;
import de.gobics.marvis.utils.swing.Statusdialog;
import de.gobics.marvis.utils.swing.filechooser.ChooserAbstract;
import de.gobics.marvis.utils.swing.filechooser.ChooserCsv;
import de.gobics.marvis.utils.swing.filechooser.ChooserExcel;
import de.gobics.marvis.utils.swing.filechooser.FileFilterCef;
import de.gobics.marvis.utils.swing.filechooser.FileFilterCsv;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import sun.net.NetworkClient;

public class MarvisGraphMainWindow extends JFrame {

	private static final Logger logger = Logger.getLogger(MarvisGraphMainWindow.class.
			getName());
	private final Statusbar statusbar = new Statusbar();
	private final JcbGraphSort combobox_graph_sort = new JcbGraphSort();
	private final TreeModelNetworks treemodel_networks = new TreeModelNetworks(null, new MetabolicNetwork[0]);
	private final JTree jtree_networks = new JTree(treemodel_networks);
	private final PanelGraphInformation panel_graph_information = new PanelGraphInformation(jtree_networks);
	private final JDesktopPane desktop = new JDesktopPane();

	public MarvisGraphMainWindow() {
		super("MarVis-Graph v0.2");
		final MarvisGraphMainWindow main_frame = this;
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
		statusbar.monitorTask(process);
		process.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("state") && evt.getNewValue().
						equals(SwingWorker.StateValue.DONE)) {
					try {
						setNetwork(process.get());
					}
					catch (Throwable ex) {
						logger.log(Level.SEVERE, "can not load network from '" + input.
								getAbsolutePath() + "': ", ex);
						showErrorBox("Can not load graph", ex);
					}
				}
			}
		});

		new Statusdialog(this).monitorTask(process);

		process.execute();
	}

	public void saveNetwork() {
		MetabolicNetwork network = getMainNetwork();
		if (network == null) {
			showErrorBox("Please select a network first.");
		}
		saveNetwork(network);
	}

	public void saveNetwork(final MetabolicNetwork n) {
		final File output_file = ChooserArchive.getInstance().doChooseFileSave(this);
		if (output_file == null) {
			return;
		}
		final SaveNetwork process = new SaveNetwork(n, output_file);
		statusbar.monitorTask(process);
		new Statusdialog(this).monitorTask(process);
		process.execute();
	}

	/**
	 * Create a JInternalFrame to display the graph. The internal frame will
	 * be added to the JDesktopPane.
	 * 
	 * Note: This is a shortcut for @{code createNetworkVisualization(getSelectedNetwork())} 
	 * including error checking
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
			this.showErrorBox("The object in the tree is not of type MetabolicNetwork but: " + o.
					getClass().getName());
			return null;
		}
		return (MetabolicNetwork) o;
	}

	/**
	 * Set the main network to the given one.
	 * @param new_network 
	 */
	public void setNetwork(MetabolicNetwork new_network) {
		for (JInternalFrame f : desktop.getAllFrames()) {
			f.dispose();
		}
		treemodel_networks.setRoot(new_network);
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
	 * model. The statusbar and a statusdialog will monitor the applications status
	 * and present it to the user.
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
						logger.finer("Got results: " + Arrays.toString(networks));
						//sortSubnetworks(networks);
						if (networks != null) {
							treemodel_networks.setSubnetworks(networks);
						}

						sortSubnetworks();
					}
					catch (Exception ex) {
						logger.log(Level.SEVERE, "Can not get calculated networks: ", ex);
					}
				}
			}
		});

		// Initialize monitoring features
		statusbar.monitorTask(process);
		new Statusdialog(this).monitorTask(process);

		// Start the process
		process.execute();

	}

	public void showErrorBox(String message) {
		showErrorBox(message, null);
	}

	public void showErrorBox(String message, Throwable e) {
		new ErrorBox(this, message, e).setVisible(true);
	}

	public void sortSubnetworks() {
		MetabolicNetwork[] subnetworks = getSubnetworks();
		logger.finer("Got " + subnetworks.length + " sub networks");
		sortSubnetworks(subnetworks);
	}

	public void sortSubnetworks(final MetabolicNetwork[] subnetworks) {
		if (subnetworks == null || subnetworks.length == 0) {
			return;
		}
		MetabolicNetwork main = getMainNetwork();
		if (main == null) {
			return;
		}

		final AbstractGraphSort sorter = combobox_graph_sort.getSorterFor(main);
		final SortNetworksTask process = new SortNetworksTask(sorter, subnetworks);
		statusbar.monitorTask(process);
		process.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("state")
						&& evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
					try {
						logger.finer("Sorted networks are ready. Will now display the results.");
						if (process.isCancelled()) {
							logger.warning("Sort process is canceled.");
						}
						else {
							treemodel_networks.setSubnetworks(process.get());
						}
					}
					catch (Exception ex) {
						logger.log(Level.SEVERE, "Can not get result of process", ex);
						showErrorBox("can not get results of sorting", ex);
					}
				}
			}
		});
		new Statusdialog(this).monitorTask(process);
		process.execute();
	}

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

	public void importMetabolitesCSV(final File input_file) {
		importMetabolitesCSV(new File[]{input_file});
	}

	public void importMetabolitesCSV(final File[] input_files) {
		if (input_files.length < 1) {
			logger.warning("Can not import from an empty file list");
			return;
		}

		for (File f : input_files) {
			if (!f.exists() || !f.canRead()) {
				logger.severe("File does not exist or is not readable: " + f.
						getAbsolutePath());
				showErrorBox("File does not exist or is not readable:\n" + f.
						getAbsolutePath());
			}
		}

		if (input_files.length > 1) {
			JOptionPane.showMessageDialog(this, "You like to import several files at once. Please\nkeep in mind that all files have to be formated\nin the same way!", "Warning", JOptionPane.WARNING_MESSAGE);
		}

		MetabolicNetwork network = getMainNetwork();
		if (network == null) {
			showErrorBox("Please load a metabolic network first");
		}

		// If there are markers in the network it is better to remove them!
		if (!network.getMarkers().isEmpty()) {
			if (JOptionPane.showConfirmDialog(this, "Network already contains marker. Remove them?\n\nWarning: If you import markers with a different number of intensities\n this can lead to serious problems.", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
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

		statusbar.monitorTask(process);
		new Statusdialog(this).monitorTask(process);

		// Asynchronous fetching of the result
		process.addPropertyChangeListener(new ProcessListener() {

			@Override
			public void processDone() {
				try {

					MetabolicNetwork network = process.get();
					if (network == null) {
						return;
					}

					if (JOptionPane.showConfirmDialog(MarvisGraphMainWindow.this, "Imported " + network.
							getMarkers().size() + " metabolic marker. Accept?", "Import result", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						setNetwork(network);
						if (JOptionPane.showConfirmDialog(MarvisGraphMainWindow.this, "Perform an annotation of the metabolic marker?", "Perform annotation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							annotateMarker();
						}
					}

				}
				catch (Exception ex) {
					showErrorBox("Can not import metabolic marker: ", ex);
					logger.log(Level.SEVERE, "Can not import metabolic marker: ", ex);
				}

			}

			@Override
			public void processError(Exception exeption) {
				showErrorBox("Can not import metabolic marker: ", exeption);
			}
		});
		process.execute();
	}

	public void importMetabolitesCef(final File[] input_files) {
		final MetabolicNetwork network = getMainNetwork();
		if (network == null) {
			showErrorBox("Please load a network first");
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
				showErrorBox("File does not exist or is not readable:\n" + f.
						getAbsolutePath());
			}
		}

		final ImportMetabolicMarkerCef process = new ImportMetabolicMarkerCef(network);

		process.setInputFiles(input_files);

		statusbar.monitorTask(process);
		new Statusdialog(this).monitorTask(process);

		// Asynchronous fetching of the result
		process.addPropertyChangeListener(new ProcessListener() {

			@Override
			public void processDone() {
				try {

					MetabolicNetwork network = process.get();
					if (network == null) {
						return;
					}

					if (JOptionPane.showConfirmDialog(MarvisGraphMainWindow.this, "Imported " + network.
							getMarkers().size() + " metabolic marker. Accept?", "Import result", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						setNetwork(network);
						if (JOptionPane.showConfirmDialog(MarvisGraphMainWindow.this, "Perform an annotation of the metabolic marker?", "Perform annotation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							annotateMarker();
						}
					}

				}
				catch (Exception ex) {
					showErrorBox("Can not import metabolic marker: ", ex);
					logger.log(Level.SEVERE, "Can not import metabolic marker: ", ex);
				}

			}

			@Override
			public void processError(Exception exeption) {
				showErrorBox("Can not import metabolic marker: ", exeption);
			}
		});
		process.execute();
	}

	public void annotateMarker() {
		MetabolicNetwork network = getMainNetwork();
		if (network == null) {
			showErrorBox("Please load a graph first");
			return;
		}

		// Remove existing annotations
		if (!network.getRelations(RelationshipType.MARKER_ANNOTATION_COMPOUND).
				isEmpty()) {
			if (JOptionPane.showConfirmDialog(this, "There are already markers annotated. This\nannotations will be removed. Accept?", "Remove existing annotation?", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				return;
			}
			for (Relation r : network.getRelations(RelationshipType.MARKER_ANNOTATION_COMPOUND)) {
				network.removeRelation(r);
			}
		}

		// Ask for range accuracy
		DialogSpinner dialog = new DialogSpinner(this, "Mass range", true, new SpinnerNumberModel(0.005, 0, Double.MAX_VALUE, 0.001));
		dialog.setVisible(true);
		if (dialog.aborted()) {
			return;
		}

		Number range = (Number) dialog.getValue();

		final AnnotateMarker annotate_process = new AnnotateMarker(network);
		annotate_process.setMassRange(Math.abs(range.doubleValue()));

		statusbar.monitorTask(annotate_process);
		new Statusdialog(this).monitorTask(annotate_process);
		// Asynchronous fetching of the result
		annotate_process.addPropertyChangeListener(new ProcessListener() {

			@Override
			public void processDone() {
				try {
					MetabolicNetwork network = annotate_process.get();
					if (network == null) {
						return;
					}
					int count_annotations = network.getRelations(RelationshipType.MARKER_ANNOTATION_COMPOUND).
							size();

					if (JOptionPane.showConfirmDialog(MarvisGraphMainWindow.this, "Found " + count_annotations + " annotations. Accept?", "Check annotations", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						setNetwork(network);
					}
				}
				catch (Exception ex) {
					showErrorBox("Can not get annotate markers: ", ex);
					logger.log(Level.SEVERE, "Can not get annotate markers: ", ex);
				}
			}

			@Override
			public void processError(Exception exeption) {
				showErrorBox("Can not annotate metabolic marker: ", exeption);
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
			showErrorBox("Input file does not exist: " + input_file.
					getAbsolutePath());
			return;
		}
		MetabolicNetwork network = getMainNetwork();
		if (network == null) {
			showErrorBox("Please load a metabolic network first");
		}

		// If there are markers in the network it is better to remove them!
		if (!network.getTranscripts().isEmpty()) {
			if (JOptionPane.showConfirmDialog(this, "Network already contains transcripts. Remove them?\n\nWarning: If you import transcripts with a different number of intensities\n this can lead to serious problems.", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
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
			showErrorBox("Can not open dialog for import options", ex);
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

		statusbar.monitorTask(process);
		new Statusdialog(this).monitorTask(process);
		// Asynchronous fetching of the result
		process.addPropertyChangeListener(new ProcessListener() {

			@Override
			public void processDone() {
				try {
					MetabolicNetwork network = process.get();

					int count_transcripts = network.getTranscripts().size();
					int count_mappings = network.getRelations(RelationshipType.TRANSCRIPT_ISFROM_GENE).
							size();

					if (JOptionPane.showConfirmDialog(MarvisGraphMainWindow.this, "Imported " + count_transcripts + " transcripts with " + count_mappings + " gene associations. Accept?", "Import result", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						setNetwork(network);
					}
				}
				catch (Exception ex) {
					showErrorBox("Can not import transcripts: ", ex);
					logger.log(Level.SEVERE, "Can not import transcripts: ", ex);
				}

			}

			@Override
			public void processError(Exception exeption) {
				showErrorBox("Can not import transcriptomics:", exeption);
			}
		});
		process.execute();
	}
}
