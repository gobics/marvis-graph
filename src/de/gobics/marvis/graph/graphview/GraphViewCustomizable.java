package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Relation;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * A graphical representation of the network that can be customized by the user
 * through the GUI. Users can select specific classes and/or nodes to hide.
 *
 * @author manuel
 */
public class GraphViewCustomizable extends AbstractGraph {

	public enum DisplayType {

		All, WithMarker, None
	}
	private static final Logger logger = Logger.getLogger(GraphViewCustomizable.class.
			getName());
	/**
	 * A specific set of {@link GraphObject}s that should be hidden.
	 */
	private final Set<GraphObject> objects_to_hide = new TreeSet<>();
	/**
	 * Contains the display type for classes
	 */
	private final TreeMap<Class<? extends GraphObject>, DisplayType> classdisplay = new TreeMap<>(new Comparator<Class<? extends GraphObject>>() {
		@Override
		public int compare(Class<? extends GraphObject> o1, Class<? extends GraphObject> o2) {
			return o1.getName().compareTo(o2.getName());
		}
	});
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
	 * A cache containing all vertices. This is for speeding the system up.
	 */
	protected final TreeSet<GraphObject> cache_vertices = new TreeSet<>();
	/**
	 * A cache mapping {@link GraphObject}s to the {@link Relation}s, they occur
	 * in. This is to speed the system up.
	 */
	protected final TreeMap<GraphObject, Collection<Relation>> cache_relations = new TreeMap<>();
	protected final TreeSet<Relation> cache_all_relations = new TreeSet<>();
	private boolean update_listener = true;

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
	 * Sets whether update listener should be notified or not.
	 *
	 * @param b true to notify listener on changes
	 */
	public void setUpdateListener(boolean b) {
		this.update_listener = b;
		if (b) {
			fireGraphChangeEvent();
		}
	}

	/**
	 * Change the hide status of the given class.
	 *
	 * @param c the class to change
	 * @param hide if true this class will be hidden
	 */
	public void hideClass(Class<? extends GraphObject> c, boolean hide) {
		setDisplayType(c, hide ? DisplayType.None : DisplayType.All);
	}

	public void setDisplayType(Class<? extends GraphObject> c, DisplayType type) {
		logger.finer("Change display type for class " + c + " from " + classdisplay.get(c) + " to: " + type);
		if (!classdisplay.containsKey(c)) {
			classdisplay.put(c, type);
			fireGraphChangeEvent();
		}
		else if (!type.equals(classdisplay.get(c))) {
			classdisplay.put(c, type);
			fireGraphChangeEvent();
		}
		else {
			logger.warning("Ignore change of display for class " + c + " from " + classdisplay.get(c) + " to: " + type);
		}
	}

	public DisplayType getDisplayType(Class<? extends GraphObject> c) {
		return classdisplay.containsKey(c) ? classdisplay.get(c) : DisplayType.All;
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
	protected boolean acceptVertex(GraphObject obj) {
		// Check if the complete class is hidden
		DisplayType type = getDisplayType(obj.getClass());
		if (type.equals(DisplayType.None)) {
			return false;
		}
		if (type.equals(DisplayType.WithMarker) && !getRootNetwork().isExplainable(obj)) {
			return false;
		}

		// Check if this specific object is hidden
		if (objects_to_hide.contains(obj)) {
			return false;
		}

		//  Check if it might be excluded as cofactor
		if (obj instanceof Compound && cofactor_limit > 0) {
			if (getRootNetwork().getReactions((Compound) obj).size() >= cofactor_limit) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected void fireGraphChangeEvent() {
		if (update_listener) {
			cache_relations.clear();
			cache_vertices.clear();
			cache_all_relations.clear();
			logger.finer("Fire graph change event");
			super.fireGraphChangeEvent();
		}
	}

	@Override
	public Collection<Relation> getEdges() {
		if (cache_all_relations.isEmpty()) {
			for (GraphObject o : getMetabolicNetwork().getAllObjects()) {
				if (acceptVertex(o)) {
					cache_all_relations.addAll(getIncidentEdges(o));
				}
			}
		}
		return cache_all_relations;
	}

	@Override
	public Collection<GraphObject> getVertices() {
		//logger.log(Level.FINE, "Require list of vertices: ", new Exception());
		if (cache_vertices.isEmpty()) {
			if (drawSingleNodes()) {
				for (GraphObject o : getMetabolicNetwork().getAllObjects()) {
					if (acceptVertex(o)) {
						cache_vertices.add(o);
					}
				}

			}
			else {
				for (Relation r : getEdges()) {
					cache_vertices.add(r.getStart());
					cache_vertices.add(r.getEnd());
				}
			}
		}
		return cache_vertices;
	}

	@Override
	public Collection<Relation> getIncidentEdges(GraphObject v) {
		//logger.log(Level.FINE, "Require list of edges to " + v + ": ", new Exception());
		if (cache_relations.containsKey(v)) {
			return cache_relations.get(v);
		}
		Set<Relation> rels = new TreeSet<>();
		for (Relation r : getMetabolicNetwork().getRelations(v)) {
			if (acceptVertex(r.getOther(v))) {
				rels.add(r);
			}
		}
		cache_relations.put(v, rels);
		return rels;
	}
}