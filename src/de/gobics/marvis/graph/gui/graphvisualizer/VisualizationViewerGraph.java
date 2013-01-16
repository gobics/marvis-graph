/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.graphvisualizer;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.graphview.GraphView;
import de.gobics.marvis.graph.graphview.GraphViewListener;
import de.gobics.marvis.graph.gui.ErrorDialog;
import de.gobics.marvis.graph.gui.GraphMouseListener;
import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.graph.gui.ProcessListener;
import de.gobics.marvis.graph.gui.tasks.RenderGraphLayout;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class VisualizationViewerGraph<E> extends VisualizationViewer<GraphObject, E> implements MouseListener {

	private static final Logger logger = Logger.getLogger(VisualizationViewerGraph.class.
			getName());
	private final LinkedList<GraphMouseListener> graph_action_listener = new LinkedList<>();
	private final GraphView<? extends GraphObject, E> graph;
	private final MarvisGraphMainWindow main_window;

	public VisualizationViewerGraph(MarvisGraphMainWindow main_window, GraphView<? extends GraphObject, E> graph) {
		super(new StaticLayout(graph));
		this.graph = graph;
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
				new EdgeTransformerLabel());
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
	}

	public void updateGraphLayout() {
		ISOMLayout layout = new ISOMLayout(getGraphLayout().getGraph());
		layout.setSize(getSize());
		layout.initialize();
		layout.setInitializer(getGraphLayout());
		layout.reset();

		long time_start = System.currentTimeMillis();
		while (System.currentTimeMillis() - time_start < 1000 && !layout.done()) {
			layout.step();
		}

		if (layout.done()) {
			drawLayout(layout);
			return;
		}
		
		logger.finer("Rendering takes to much time. Starting background Process");


		final RenderGraphLayout process = new RenderGraphLayout(layout);
		main_window.monitorTask(process);
		process.addPropertyChangeListener(new ProcessListener() {
			@Override
			public void processDone() {
				try {
					Layout rendered = process.get();
					if (rendered != null) {
						drawLayout(rendered);
					}
				}
				catch (Exception exeption) {
					logger.log(Level.SEVERE, "Can not render graph layout: {0}", exeption);
					ErrorDialog.show(null, "Can not get rendered graph layout", exeption);
				}
			}

			@Override
			public void processError(Exception exeption) {
				ErrorDialog.show(null, "Can not render graph", exeption);
			}
		});
		process.execute();
	}

	private void drawLayout(Layout layout) {
		// Copy the information to the new layout
		StaticLayout rendered = new StaticLayout(layout.getGraph(), layout.getSize());
		for (Object o : layout.getGraph().getVertices()) {
			rendered.setLocation(o, (Point2D) layout.transform(o));
		}
		VisualizationViewerGraph.this.setGraphLayout(rendered);
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

		for (GraphMouseListener l : graph_action_listener) {
			l.doubleClick(o);
		}
	}

	public void addGraphActionListener(GraphMouseListener graphActionListener) {
		if (!graph_action_listener.contains(graphActionListener)) {
			graph_action_listener.add(graphActionListener);
		}
	}
}