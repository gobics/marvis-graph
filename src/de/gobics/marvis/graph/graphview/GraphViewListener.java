package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.graphview.GraphView;

/**
 * A listener to graphical representations.
 *
 * @author manuel
 */
public interface GraphViewListener {

	/**
	 * This method will be fired when the graphical view has somehow be changed.
	 * @param parent 
	 */
	public void graphChanged(GraphView parent);
}
