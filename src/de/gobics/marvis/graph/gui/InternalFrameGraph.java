package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.graphview.*;
import de.gobics.marvis.graph.gui.actions.*;
import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import de.gobics.marvis.utils.swing.*;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import java.awt.*;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.jfree.chart.ChartPanel;

public final class InternalFrameGraph extends JInternalFrame {

	protected static final Logger logger = Logger.getLogger(InternalFrameGraph.class.
			getName());
	// MetabolicNetwork visualizing stuff
	private final MarvisGraphMainWindow main_window;
	protected final MetabolicNetwork network;
	//protected VisualizationViewerGraph graphViewer;
	protected JPanel toolbarPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
	//protected final InternalFrameGraphToolbarView toolbarView;
	private final Statusbar statusbar = new Statusbar();
	private final JTabbedPane notebook = new JTabbedPane();

	/**
	 * Create a new MetabolicNetworkViewer InternalFrame
	 *
	 * @param parent The main GUI instance
	 * @param view The view to display
	 */
	public InternalFrameGraph(MarvisGraphMainWindow main_window, MetabolicNetwork network) {
		super(network.getName(), true, true, true, true);
		if (main_window == null) {
			throw new NullPointerException("Given main window is null");
		}
		if (network == null) {
			throw new NullPointerException("Given network is null");
		}
		this.main_window = main_window;
		logger.log(Level.FINER, "Creating internal frame for: {0}", network);
		this.network = network;
		if (network == null) {
			throw new NullPointerException("Given network is NULL");
		}

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());

		// Initialize menu
		setJMenuBar(new JMenuBar());
		JMenu menu = new JMenu("Visualization");
		getJMenuBar().add(menu);

		menu.add(new JMenuItem(new ActionVisualizationHeatmapMarker(this)));
		menu.add(new JMenuItem(new ActionVisualizationHeatmapTranscript(this)));
		menu.add(new JSeparator());
		menu.add(new JMenuItem(new ActionVisualizationClose(this)));

		menu = new JMenu("View");
		getJMenuBar().add(menu);
		menu.add(new JMenuItem(new ActionVisualizationDrawViewDefault(this)));
		menu.add(new JMenuItem(new ActionVisualizationDrawViewReaction(this)));

		// Add notebook
		add(notebook, BorderLayout.CENTER);
		notebook.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);


		// init heatmap
		logger.finer("Initializing heatmaps");
		drawHeatmapMarker();
		drawHeatmapTranscript();

		// ObjectList
		//logger.finer("Creating list of graph objects");
		//MetabolicNetworkobjectList list = new MetabolicNetworkobjectList(this.view);
		//list.addMetabolicNetworkobjectListListener(this);
		//notebook.add("List of all objects", new JScrollPane(list));

		add(statusbar, BorderLayout.PAGE_END);
		setPreferredSize(new Dimension(600, 600));
		pack();

		drawNetwork();
		logger.log(Level.FINER, "{0} is ready for graph: {1}", new Object[]{getClass().getSimpleName(), this.network});
	}

	/**
	 * Get the view instance
	 *
	 * @return the view which is actually be displayed
	 */
	public MetabolicNetwork getMetabolicNetwork() {
		return this.network;
	}

	/**
	 * Select a specified vertex within the view.
	 *
	 * @param obj
	 */
//	public void selectVertex(GraphObject obj) {
//		graphViewer.getPickedVertexState().pick(obj, true);
//		graphViewer.repaint();
//	}
	public void drawHeatmapMarker() {
		logger.finer("Creating marker heatmap");
		if (!network.hasMarkers()) {
			return;
		}
		Collection<Marker> marker_collection = network.getMarkers();
		if (marker_collection.isEmpty()) {
			return;
		}

		Marker[] markers = marker_collection.toArray(new Marker[marker_collection.
				size()]);

		ChartPanel heatmap_marker = IntensityProfileHistogram.createHeatmap(markers);

//		heatmap_marker.addHeatmapListener(new HeatmapListener() {
//
//			@Override
//			public void heatmapClicked(HeatmapEvent event) {
//			}
//
//			@Override
//			public void heatmapSelectionChanged(HeatmapEvent event) {
//			}
//
//			@Override
//			public void heatmapDoubleClicked(HeatmapEvent event) {
//				Object o = event.getLabelX();
//				if (o instanceof GraphObject) {
//					main_window.createGraphobjectVisualization((GraphObject) o);
//				}
//			}
//		});


		addTab("Marker heatmap", heatmap_marker);
	}

	public void drawHeatmapTranscript() {
		logger.finer("Creating transcript heatmap");
		if (!network.hasTranscripts()) {
			return;
		}
		Collection<Transcript> transcript_collection = network.getTranscripts();
		if (transcript_collection.isEmpty()) {
			return;
		}
		Transcript[] markers = transcript_collection.toArray(new Transcript[transcript_collection.
				size()]);

		ChartPanel heatmap_transcripts = IntensityProfileHistogram.createHeatmap(markers);

		/*
		 * heatmap_transcripts.addHeatmapListener(new HeatmapListener() {
		 *
		 * @Override public void heatmapClicked(HeatmapEvent event) { }
		 *
		 * @Override public void heatmapSelectionChanged(HeatmapEvent event) { }
		 *
		 * @Override public void heatmapDoubleClicked(HeatmapEvent event) {
		 * Object o = event.getLabelX(); if (o instanceof GraphObject) {
		 * main_window.createGraphobjectVisualization((GraphObject) o); } }
		});
		 */
		addTab("Transcript heatmap", heatmap_transcripts);
	}

	public void drawNetwork() {
		drawNetwork(new GraphViewCustomizable(network));
	}

	@SuppressWarnings("unchecked")
	public void drawNetwork(GraphView graph_view) {
		logger.log(Level.FINER, "Creating graph viewer for view: {0}", graph_view);
		JPanel panel = new JPanel(new BorderLayout());

		VisualizationViewerGraph viewer = new VisualizationViewerGraph(main_window, graph_view);
		GraphZoomScrollPane graphZoom = new GraphZoomScrollPane(viewer);
		graphZoom.setPreferredSize(new Dimension(400, 400));

		viewer.addGraphActionListener(new GraphMouseListener() {

			@Override
			public void doubleClick(GraphObject o) {
				main_window.createGraphobjectVisualization(o);
			}
		});

		panel.add(graphZoom, BorderLayout.CENTER);

		if (graph_view instanceof GraphViewReactions) {
			addTab("Reaction network", panel);
			panel.add(new ToolbarView(this, viewer), BorderLayout.PAGE_START);
		}
		else if (graph_view instanceof GraphViewCustomizable) {
			addTab("Metabolic network", panel);
			panel.add(new ToolbarViewDefault(this, viewer, (GraphViewCustomizable) graph_view), BorderLayout.PAGE_START);
		}
		else {
			addTab("Metabolic network", panel);
		}

		new PopupMenuNetworkViewer(main_window, viewer, graph_view);
		viewer.updateGraphLayout();
	}

	private void addTab(final String title, final Component content) {
		notebook.addTab(title, content);
		notebook.setTabComponentAt(notebook.getTabCount() - 1, new TabComponent(notebook));
		notebook.getModel().setSelectedIndex(notebook.getTabCount() - 1);
	}
}

class ToolbarView extends JToolBar {

	public ToolbarView(final InternalFrameGraph parent, final VisualizationViewerGraph viewer) {
		super("Viewer toolbar");
		JButton button = new JButton(new ViewerActionRedraw(viewer));
		add(button);
		button.setHideActionText(true);
	}
}

class ToolbarViewDefault extends ToolbarView {

	public ToolbarViewDefault(final InternalFrameGraph parent, final VisualizationViewerGraph viewer, final GraphViewCustomizable view) {
		super(parent, viewer);

		JToggleButton button = new JToggleButton(new ViewerActionDrawSingleNodes(viewer, view));
		button.setHideActionText(true);
		add(button);

		button = new JToggleButton(new ViewerActionDrawExplainableNodesOnly(viewer, view));
		button.setHideActionText(true);
		add(button);


		button = new JToggleButton(new ViewerActionDrawTypeMarker(viewer, view));
		button.setHideActionText(true);
		add(button);

		button = new JToggleButton(new ViewerActionDrawTypeMolecule(viewer, view));
		button.setHideActionText(true);
		add(button);

		button = new JToggleButton(new ViewerActionDrawTypeReaction(viewer, view));
		button.setHideActionText(true);
		add(button);

		button = new JToggleButton(new ViewerActionDrawTypeEnzyme(viewer, view));
		button.setHideActionText(true);
		add(button);

		button = new JToggleButton(new ViewerActionDrawTypeGene(viewer, view));
		button.setHideActionText(true);
		add(button);

		button = new JToggleButton(new ViewerActionDrawTypeTranscript(viewer, view));
		button.setHideActionText(true);
		add(button);

		button = new JToggleButton(new ViewerActionDrawTypePathway(viewer, view));
		button.setHideActionText(true);
		add(button);

		JButton button2 = new JButton(new ViewerActionDrawDefineCofactor(viewer, view));
		add(button2);
	}
}
