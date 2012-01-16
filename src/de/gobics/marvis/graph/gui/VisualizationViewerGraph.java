/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.Relation;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import de.gobics.marvis.graph.graphview.GraphViewAbstract;
import de.gobics.marvis.graph.gui.tasks.RenderGraphLayout;
import de.gobics.marvis.utils.swing.Statusdialog;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class VisualizationViewerGraph<E> extends VisualizationViewer<GraphObject, E> implements MouseListener {

	private static final Logger logger = Logger.getLogger(VisualizationViewerGraph.class.
			getName());
	private final LinkedList<GraphMouseListener> graph_action_listener = new LinkedList<GraphMouseListener>();
	private final GraphViewAbstract<E> graph;

	public VisualizationViewerGraph(GraphViewAbstract<E> graph) {
		super(new StaticLayout<GraphObject, E>(graph));
		this.graph = graph;
		logger.finer("Initializing graph viewer for graph: " + graph);
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

		logger.finer("Viewer for ready for: " + graph);
	}

	public void updateGraphLayout() {
		ISOMLayout layout = new ISOMLayout(getGraphLayout().getGraph());
		layout.setSize(getSize());
		layout.initialize();
		layout.setInitializer(getGraphLayout());
		layout.reset();

		final RenderGraphLayout process = new RenderGraphLayout(layout);
		new Statusdialog(null).monitorTask(process);

		process.addPropertyChangeListener(new ProcessListener() {

			@Override
			public void processDone() {
				try {
					Layout rendered = process.get();
					if (rendered != null) {
						VisualizationViewerGraph.this.setGraphLayout(rendered);
					}
				}
				catch (Exception exeption) {
					logger.log(Level.SEVERE, "Can not render graph layout: " + exeption);
					new ErrorBox(null, "Can not get rendered graph layout", exeption).
							setVisible(true);
				}
			}

			@Override
			public void processError(Exception exeption) {
				new ErrorBox(null, "Can not render graph", exeption);
			}
		});
		process.execute();
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
		logger.finer("Double click event: " + e);
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

	void addGraphActionListener(GraphMouseListener graphActionListener) {
		if (!graph_action_listener.contains(graphActionListener)) {
			graph_action_listener.add(graphActionListener);
		}
	}
}