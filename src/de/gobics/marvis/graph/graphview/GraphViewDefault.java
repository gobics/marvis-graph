/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class GraphViewDefault extends GraphViewAbstract<Relation> {

	private static final Logger logger = Logger.getLogger(GraphViewDefault.class.getName());
	private final Graph<GraphObject, Relation> view = new SparseGraph<GraphObject, Relation>();

	public GraphViewDefault(MetabolicNetwork basis_network) {
		super(basis_network);
	}

	private void initView() {
		logger.finer("Initializing the view");
		for (GraphObject o : getParent().getAllObjects()) {
			view.addVertex(o);
		}
		for (Relation r : getParent().getAllRelations()) {
			view.addEdge(r, r.getStart(), r.getEnd());
		}
	}

	@Override
	public Graph<GraphObject, Relation> getView() {
		if (view.getVertexCount() == 0) {
			initView();
		}
		return view;
	}

	@Override
	public Set<Relation> getEnvironment(GraphObject o) {
		return new TreeSet<Relation>();
	}
}
