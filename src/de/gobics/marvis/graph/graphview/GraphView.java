package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.GraphObject;
import edu.uci.ics.jung.graph.Graph;

/**
 * An extension of the {@link edu.uci.ics.jung.graph.Graph} as wrapper for the 
 * JUNG2 framework.
 *
 * @author manuel
 */
public interface GraphView<V extends GraphObject, E> extends Graph<V, E> {

    public boolean isExplainable(GraphObject o);
	
	public void addGraphViewListener(GraphViewListener l);
	
	public void removeGraphViewListener(GraphViewListener l);
}
