package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.graphview.GraphView;
import de.gobics.marvis.utils.task.AbstractTask;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.awt.Dimension;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class RenderGraphLayout extends AbstractTask<Layout, Void> {

	private final static Logger logger = Logger.getLogger(RenderGraphLayout.class.
			getName());
	private final GraphView<GraphObject, Relation> graphview;
	private Dimension size = new Dimension(400, 400);
	private Layout<GraphObject, Relation> template = null;
	private final boolean recalculate;

	public RenderGraphLayout(GraphView<GraphObject, Relation> graphview) {
		this(graphview, true);
	}

	public RenderGraphLayout(GraphView<GraphObject, Relation> graphview, boolean randomize_positions) {
		this.graphview = graphview;
		this.recalculate = randomize_positions;
		setTaskTitle("Calculating graph layout");
		setTaskDescription("Rendering graph layout");
	}

	public void setSize(Dimension size) {
		this.size = size;
	}

	public void setTemplate(Layout<GraphObject, Relation> template) {
		this.template = template;
	}

	@Override
	protected Layout doTask() throws Exception {
		logger.finer("Starting rendering");

		// Build graph
		Graph<GraphObject, Relation> graph = new SparseGraph<>();
		for (GraphObject o : graphview.getVertices()) {
			graph.addVertex(o);
		}
		if (isCanceled()) {
			return null;
		}
		for (Relation r : graphview.getEdges()) {
			graph.addEdge(r, r.getStart(), r.getEnd(), EdgeType.UNDIRECTED);
		}
		if (isCanceled()) {
			return null;
		}

		// Calculate the layout
		Layout<GraphObject, Relation> final_layout;
		if (template == null || recalculate) {
			ISOMLayout layout = new ISOMLayout(graph);
			layout.setSize(size);
			layout.setInitializer(template != null ? template : new RandomLocationTransformer(layout.getSize()));
			layout.initialize();
			layout.reset();
			int counter = 0;
			while (!layout.done()) {
				counter++;
				layout.step();
				if (isCanceled()) {
					return null;
				}
			}
			logger.finer("Layout calculation took " + counter + " steps");

			// Copy Layout to static layout
			final_layout = new StaticLayout(graph);
			for (GraphObject o : graph.getVertices()) {
				final_layout.setLocation(o, layout.transform(o));
			}
		}
		else {
			final_layout = new StaticLayout(graph);
			RandomLocationTransformer rand = new RandomLocationTransformer(size);
			for (GraphObject vertex : graphview.getVertices()) {
				if (template != null) {
					final_layout.setLocation(vertex, template.transform(vertex));
				}
				else {
					final_layout.setLocation(vertex, rand.transform(vertex));
				}
			}
		}

		if (isCanceled()) {
			return null;
		}

		logger.finer("Returning new layout");
		return final_layout;
	}
}
