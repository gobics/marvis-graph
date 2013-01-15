/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.Marker;
import de.gobics.marvis.graph.graphview.GraphViewCustomizable;
import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import java.awt.event.ActionEvent;

/**
 *
 * @author manuel
 */
public class ViewerActionDrawTypeMarker extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionDrawTypeMarker(final VisualizationViewerGraph viewer, GraphViewCustomizable view) {
		super("Draw marker", "marker.png");
		this.viewer = viewer;
		this.view = view;
		putValue(LONG_DESCRIPTION, "Select this to display m/z marker nodes");
		if (!view.parentContains(Marker.class)) {
			putValue(SELECTED_KEY, false);
			view.hideType(Marker.class);
		}
		else {
			putValue(SELECTED_KEY, true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (getValue(SELECTED_KEY) != null && ((Boolean) getValue(SELECTED_KEY)).
				booleanValue()) {
			view.showType(Marker.class);
		}
		else {
			view.hideType(Marker.class);
		}
		viewer.updateGraphLayout();
	}
}