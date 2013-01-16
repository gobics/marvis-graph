/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.graphview.GraphView;
import de.gobics.marvis.graph.graphview.GraphViewCustomizable;
import de.gobics.marvis.graph.graphview.GraphViewListener;
import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class ViewerActionDrawSingleNodes extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionDrawSingleNodes(final VisualizationViewerGraph viewer, GraphViewCustomizable gview) {
		super("Show single nodes", "single_nodes.png");
		this.viewer = viewer;
		this.view = gview;

		putValue(SELECTED_KEY, Boolean.TRUE);
		putValue(LONG_DESCRIPTION, "Select this to display nodes without relations");

		gview.setDrawSingleNodes((Boolean) getValue(SELECTED_KEY));

		view.addGraphViewListener(new GraphViewListener() {
			@Override
			public void graphChanged(GraphView parent) {
				putValue(SELECTED_KEY, view.drawSingleNodes());
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		view.setDrawSingleNodes(!view.drawSingleNodes());
	}
}