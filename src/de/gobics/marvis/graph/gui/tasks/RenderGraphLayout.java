/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.graphview.GraphView;
import de.gobics.marvis.utils.task.AbstractTask;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author manuel
 */
public class RenderGraphLayout extends AbstractTask<Layout, Void> {

	private final static Logger logger = Logger.getLogger(RenderGraphLayout.class.
			getName());
	private final GraphView graphview;
	private Dimension size = new Dimension(400, 400);
	private Layout template = null;

	public RenderGraphLayout(GraphView graphview) {
		this.graphview = graphview;
		setTaskTitle("Calculating graph layout");
		setTaskDescription("Rendering graph layout");
	}

	public void setSize(Dimension size) {
		this.size = size;
	}

	public void setTemplate(Layout template) {
		this.template = template;
	}

	@Override
	protected Layout doTask() throws Exception {
		logger.finer("Starting rendering");

		// Build graph
		Graph<GraphObject, Relation> graph = new SparseGraph<>();
		for (Object o : graphview.getVertices()) {
			graph.addVertex((GraphObject) o);
		}
		for (Object ro : graphview.getEdges()) {
			Relation r = (Relation) ro;
			graph.addEdge(r, r.getStart(), r.getEnd(), EdgeType.UNDIRECTED);
		}

		// Calculate the layout
		ISOMLayout layout = new ISOMLayout(graph);
		layout.setSize(size);
		layout.setInitializer(template != null ? template : new RandomLocationTransformer(layout.getSize()));
		layout.initialize();
		layout.reset();
		int counter = 0;
		while (!layout.done()) {
			counter++;
			layout.step();
		}
		logger.finer("Layout calculation took "+counter+" steps");

		// Copy Layout to static layout
		Layout final_layout = new StaticLayout(graph);
		for (Object o : graph.getVertices()) {
			final_layout.setLocation(o, layout.transform(o));
		}

		logger.finer("Returning new layout");
		return final_layout;
	}
}
