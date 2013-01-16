/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.GraphObject;
import edu.uci.ics.jung.graph.Graph;

/**
 *
 * @author manuel
 */
public interface GraphView<V extends GraphObject, E> extends Graph<V, E> {

    public boolean isExplainable(GraphObject o);
}
