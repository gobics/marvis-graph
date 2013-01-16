package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Relation;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A graphical representation of the network that can be customized by the user
 * through the GUI. Users can select specific classes and/or nodes to hide.
 *
 * @author manuel
 */
public class GraphViewCustomizable extends AbstractGraph {

	private static final Logger logger = Logger.getLogger(GraphViewCustomizable.class.
			getName());
	/**
	 * A specific set of {@link GraphObject}s that should be hidden.
	 */
	private final Set<GraphObject> objects_to_hide = new TreeSet<>();
	/**
	 * Contains the classes that should be hidden in the network.
	 */
	private final TreeSet<Class<? extends GraphObject>> classes_to_hide = new TreeSet<>(new Comparator<Class<? extends GraphObject>>() {
		@Override
		public int compare(Class<? extends GraphObject> o1, Class<? extends GraphObject> o2) {
			return o1.getName().compareTo(o2.getName());
		}
	});
	/**
	 * A boolean flag allows only to draw explainable nodes.
	 */
	private boolean draw_explainable_nodes_only = false;
	/**
	 * If set to true, this view will also display nodes without an edge
	 * (usually useless).
	 */
	private boolean draw_single_nodes = true;
	/**
	 * If this value if greater than zero, only compounds will be drawn, that
	 * take part in less than {@code cofactor_limit} reactions. This will
	 * decrease the density of the visualization.
	 */
	private int cofactor_limit = -1;

	/**
	 * Create a new customizable graphical view based on the given network.
	 *
	 * @param network
	 */
	public GraphViewCustomizable(MetabolicNetwork network) {
		super(network);
	}

	/**
	 * Change the cofactor limit to the new value. If the value is less or equal
	 * zero, no threshold will be applied and all molecules will be displayed.
	 *
	 * @param new_limit
	 */
	public void setCofactorLimit(int new_limit) {
		if (cofactor_limit != new_limit) {
			cofactor_limit = new_limit;
			fireGraphChangeEvent();
		}
	}

	/**
	 * Returns the currently used cofactor limit.
	 *
	 * @return
	 */
	public int getCofactorLimit() {
		return cofactor_limit;
	}

	/**
	 * Change the hide status of the given class.
	 *
	 * @param c the class to change
	 * @param hide if true this class will be hidden
	 */
	public void hideClass(Class<? extends GraphObject> c, boolean hide) {
		if (hide) {
			if (classes_to_hide.add(c)) {
				fireGraphChangeEvent();
			}
		}
		else {
			if (classes_to_hide.remove(c)) {
				fireGraphChangeEvent();
			}
		}
	}

	/**
	 * Returns true if the given class has to be hidden.
	 *
	 * @param c the class to check
	 * @return true if {@code c} should be hidden
	 */
	public boolean drawsType(Class<? extends GraphObject> c) {
		return !classes_to_hide.contains(c);
	}

	/**
	 * Removes the specific object from the display.
	 *
	 * @param o the object to hide
	 */
	public void hideGraphobject(GraphObject o) {
		if (o == null) {
			return;
		}
		if (objects_to_hide.add(o)) {
			fireGraphChangeEvent();
		}
	}

	/**
	 * Will reset the view to contain all formerly hidden nodes again.
	 */
	public void resetHidenNodes() {
		if (!objects_to_hide.isEmpty()) {
			objects_to_hide.clear();
			fireGraphChangeEvent();
		}
	}

	/**
	 * Returns whether explainable nodes should be drawn or not.
	 *
	 * @return true if only explainable nodes should be drawn.
	 */
	public boolean drawExplainableNodesOnly() {
		return draw_explainable_nodes_only;
	}

	/**
	 * Set the status of drawing explainable nodes.
	 *
	 * @param draw_explainable_nodes_only true if only explainable nodes should
	 * be drawn.
	 */
	public void setDrawExplainableNodesOnly(boolean draw_explainable_nodes_only) {
		if (this.draw_explainable_nodes_only != draw_explainable_nodes_only) {
			this.draw_explainable_nodes_only = draw_explainable_nodes_only;
			fireGraphChangeEvent();
		}
	}

	/**
	 * Returns whether single nodes are drawn.
	 *
	 * @return true if single nodes are drawn.
	 */
	public boolean drawSingleNodes() {
		return draw_single_nodes;
	}

	/**
	 * Change whether single nodes are drawn.
	 *
	 * @param draw_single_nodes true to draw single nodes.
	 */
	public void setDrawSingleNodes(boolean draw_single_nodes) {
		if (this.draw_single_nodes != draw_single_nodes) {
			this.draw_single_nodes = draw_single_nodes;
			fireGraphChangeEvent();
		}
	}

	/**
	 * Check whether the node @code{obj} will be displayed or not.
	 *
	 * @param obj The object to check.
	 * @return true if the vertex should be display.
	 */
	private boolean acceptVertex(GraphObject obj) {
		// Check if the complete class is hidden
		try {
			if (classes_to_hide.contains(obj.getClass())) {
				return false;
			}
		}
		catch (Throwable e) {
			logger.log(Level.SEVERE, "Can not check if class {0} is to hide: {1}", new Object[]{obj.getClass(), e});
			return true;
		}

		// Check if this specific object is hidden
		if (objects_to_hide.contains(obj)) {
			return false;
		}

		//  Check if it might be excluded as cofactor
		if (obj instanceof Compound && cofactor_limit > 0) {
			if (getRootNetwork().getReactions((Compound) obj).size() >= cofactor_limit) {
				logger.log(Level.FINER, "Will not display cofactor: {0}", obj);
				return false;
			}
		}

		// Check if it needs to be excluded based on the 
		if (drawExplainableNodesOnly()) {
			return getParent().isExplainable(obj);
		}

		return true;
	}

	@Override
	public Collection<Relation> getEdges() {
		Set<Relation> rels = new TreeSet<>();
		for (GraphObject o : getParent().getAllObjects()) {
			if (acceptVertex(o)) {
				rels.addAll(getIncidentEdges(o));
			}
		}
		return rels;
	}

	@Override
	public Collection<GraphObject> getVertices() {
		if (drawSingleNodes()) {
			Set<GraphObject> vertices = new TreeSet<>();
			for (GraphObject o : getParent().getAllObjects()) {
				if (acceptVertex(o)) {
					vertices.add(o);
				}
			}
			return vertices;
		}

		Set<GraphObject> vertices = new TreeSet<>();
		for (Relation r : getEdges()) {
			vertices.add(r.getStart());
			vertices.add(r.getEnd());
		}
		return vertices;
	}

	@Override
	public Collection<Relation> getIncidentEdges(GraphObject v) {
		Set<Relation> rels = new TreeSet<>();
		for (Relation r : getParent().getRelations(v)) {
			if (acceptVertex(r.getOther(v))) {
				rels.add(r);
			}
		}
		return rels;
	}
}