/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Relation;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class GraphViewCustomizable extends GraphViewAbstract<Relation> {

	private static final Logger logger = Logger.getLogger(GraphViewCustomizable.class.
			getName());
	private final Graph<GraphObject, Relation> view = new SparseGraph<GraphObject, Relation>();
	private final Set<GraphObject> objects_to_hide = new TreeSet<GraphObject>();
	private final TreeSet<Class<? extends GraphObject>> classes_to_hide = new TreeSet<Class<? extends GraphObject>>(new Comparator<Class<? extends GraphObject>>() {

		@Override
		public int compare(Class<? extends GraphObject> o1, Class<? extends GraphObject> o2) {
			return o1.getName().compareTo(o2.getName());
		}
	});
	private boolean is_initialized = false;
	private boolean draw_explainable_nodes_only = false;
	private boolean draw_single_nodes = true;
	private int cofactor_limit = -1;

	public GraphViewCustomizable(MetabolicNetwork network) {
		super(network);
	}

	public void setCofactorLimit(int new_limit) {
		if (cofactor_limit != new_limit) {
			cofactor_limit = new_limit;
			reset();
		}
	}

	public int getCofactorLimit() {
		return cofactor_limit;
	}

	public void hideType(Class<? extends GraphObject> c) {
		if (classes_to_hide.add(c)) {
			reset();
		}
	}

	public void showType(Class<? extends GraphObject> c) {
		if (classes_to_hide.remove(c)) {
			reset();
		}
	}

	public boolean drawsType(Class<? extends GraphObject> c) {
		return !classes_to_hide.contains(c);
	}

	public void hideGraphobject(GraphObject o) {
		if (o == null) {
			return;
		}
		if (objects_to_hide.add(o)) {
			reset();
		}
	}

	public void resetHidenNodes() {
		objects_to_hide.clear();
		reset();
	}

	public boolean drawExplainableNodesOnly() {
		return draw_explainable_nodes_only;
	}

	public void setDrawExplainableNodesOnly(boolean draw_explainable_nodes_only) {
		this.draw_explainable_nodes_only = draw_explainable_nodes_only;
		reset();
	}

	public boolean drawSingleNodes() {
		return draw_single_nodes;
	}

	public void setDrawSingleNodes(boolean draw_single_nodes) {
		this.draw_single_nodes = draw_single_nodes;
		reset();
	}

	@Override
	public Graph<GraphObject, Relation> getView() {
		initView();
		return view;
	}

	@Override
	public Set<Relation> getEnvironment(GraphObject o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	synchronized private void initView() {
		if (is_initialized) {
			return;
		}
		// Clear the graph
		logger.finer("Initializing view");

		for (GraphObject v : view.getVertices()) {
			view.removeVertex(v);
		}
		for (Relation e : view.getEdges()) {
			view.removeEdge(e);
		}

		// Add the nodes
		if (drawSingleNodes()) {
			logger.finer("Adding all nodes");
			for (GraphObject v : getParent().getAllObjects()) {
				if (evaluateVertex(v)) {
					view.addVertex(v);
				}
			}
		}

		// Add the relations
		logger.finer("Adding all relations");
		for (Relation r : getParent().getRelations()) {
			if (evaluateVertex(r.getStart()) && evaluateVertex(r.getEnd())) {
				view.addEdge(r, r.getStart(), r.getEnd());
			}
		}
		is_initialized = true;


	}

	/**
	 * Check whether the node @code{obj} will be displayed or not.
	 *
	 * @param obj The object to check.
	 * @return true if the vertex should be display.
	 */
	private boolean evaluateVertex(GraphObject obj) {
		//logger.finer("Evaluating: " + obj);
		try { // FIXME:
			if (classes_to_hide.contains(obj.getClass())) {
				return false;
			}
		}
		catch (Throwable e) {
			logger.log(Level.SEVERE, "Can not check if class " + obj.getClass() + " is to hide: ", e);
			return true;
		}

		if (objects_to_hide.contains(obj)) {
			return false;
		}

		if (obj instanceof Compound && cofactor_limit > 0) {
			if (getFirstParentNetwork().getReactions((Compound) obj).size() >= cofactor_limit) {
				logger.finer("Will not display cofactor: " + obj);
				return false;
			}
			else {
				logger.finer(obj + " is under the cofactor treshold: " + getFirstParentNetwork().
						getReactions((Compound) obj).size() + "<" + cofactor_limit);
			}
		}

		if (drawExplainableNodesOnly()) {
			return getParent().isExplainable(obj);
		}

		return true;
	}

	private void reset() {
		is_initialized = false;
	}
}
