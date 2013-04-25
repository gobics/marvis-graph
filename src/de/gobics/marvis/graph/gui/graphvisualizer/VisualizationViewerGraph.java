package de.gobics.marvis.graph.gui.graphvisualizer;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.graphview.GraphView;
import de.gobics.marvis.graph.graphview.GraphViewCustomizable;
import de.gobics.marvis.graph.graphview.GraphViewListener;
import de.gobics.marvis.graph.gui.GraphMouseListener;
import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.graph.gui.tasks.RenderGraphLayout;
import de.gobics.marvis.utils.task.TaskResultListener;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * A panel that displays the graph and its layout
 *
 * @author manuel
 */
public class VisualizationViewerGraph<E> extends VisualizationViewer<GraphObject, E> implements MouseListener {

	private static final Logger logger = Logger.getLogger(VisualizationViewerGraph.class.
			getName());
	private final LinkedList<GraphMouseListener> graph_mouse_listener = new LinkedList<>();
	private final MarvisGraphMainWindow main_window;
	private final GraphView graphview;

	public VisualizationViewerGraph(MarvisGraphMainWindow main_window, GraphView<? extends GraphObject, E> graph) {
		super(new StaticLayout(new SparseGraph<>()));
		this.graphview = graph;

		setDoubleBuffered(false);
		this.main_window = main_window;
		logger.log(Level.FINER, "Initializing graph viewer for graph: {0}", graph);
		setBackground(Color.WHITE);

		addMouseListener(this);

		logger.finer("Setting up rendering");
		getRenderContext().setVertexLabelTransformer(
				new VertexTransformerLabel());
		getRenderContext().setVertexStrokeTransformer(
				new VertexTransformerStroke(graph));
		getRenderContext().setVertexFillPaintTransformer(
				new VertexTransformerFill(graph));
		getRenderContext().setEdgeStrokeTransformer(
				new EdgeTransformerStroke(graph));
		getRenderContext().setEdgeLabelTransformer(
				new EdgeTransformerLabel(false));
		getRenderContext().setEdgeShapeTransformer(
				new EdgeShape.Line<GraphObject, E>());
		getRenderContext().setEdgeStrokeTransformer(
				new EdgeTransformerStroke(graph));


		logger.finer("Setting up mouse interaction");
		PluggableGraphMouse gm = new PluggableGraphMouse();
		gm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON3_MASK));
		gm.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0,
				1.1f, 0.9f));
		gm.add(new PickingGraphMousePlugin<GraphObject, Relation>(
				MouseEvent.BUTTON1_MASK,
				MouseEvent.MOUSE_DRAGGED));
		setGraphMouse(gm);

		graph.addGraphViewListener(new GraphViewListener() {
			@Override
			public void graphChanged(GraphView parent) {
				updateGraphLayout();
			}
		});

		logger.log(Level.FINER, "Viewer ready for: {0}", graph);

		/*SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateGraphLayout();
			}
		});*/
	}

	/**
	 * Redraws the complete layout of the graph. Also the layout will be
	 * reinitialized and recalculated. This is useful when the structure has
	 * been changed e.g. in the {@link GraphViewCustomizable}.
	 */
	public void updateGraphLayout() {
		logger.finer("Updating graph layout");

		final RenderGraphLayout process = new RenderGraphLayout(graphview);
		process.setSize(getSize());
		process.addTaskListener(new TaskResultListener<Void>() {
			@Override
			public void taskDone() {
				Layout rendered = process.getTaskResult();
				if (rendered != null) {
					VisualizationViewerGraph.this.setGraphLayout(rendered);
				}
			}
		});
		main_window.executeTask(process);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
			fireDoubleClickEvent(e);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	private void fireDoubleClickEvent(MouseEvent e) {
		logger.log(Level.FINER, "Double click event: {0}", e);
		Point p = e.getPoint();

		GraphObject o =
				getPickSupport().getVertex(getGraphLayout(), p.getX(), p.getY());
		if (o == null) {
			return;
		}

		for (GraphMouseListener l : graph_mouse_listener) {
			l.doubleClick(o);
		}
	}

	public void addGraphActionListener(GraphMouseListener graphActionListener) {
		if (!graph_mouse_listener.contains(graphActionListener)) {
			graph_mouse_listener.add(graphActionListener);
		}
	}
}