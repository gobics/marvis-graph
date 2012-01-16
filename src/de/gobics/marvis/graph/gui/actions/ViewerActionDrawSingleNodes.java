/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.graphview.GraphViewCustomizable;
import de.gobics.marvis.graph.gui.VisualizationViewerGraph;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class ViewerActionDrawSingleNodes extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionDrawSingleNodes(final VisualizationViewerGraph viewer, GraphViewCustomizable view) {
		super("Show single nodes", "single_nodes.png");
		this.viewer = viewer;
		this.view = view;

		putValue(SELECTED_KEY, Boolean.TRUE);
		putValue(LONG_DESCRIPTION, "Select this to display nodes without relations");

		view.setDrawSingleNodes((Boolean) getValue(SELECTED_KEY));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Logger.getLogger(ViewerActionDrawSingleNodes.class.getName()).finer(e.
				toString());

		if (view.drawSingleNodes() != ((Boolean) getValue(SELECTED_KEY)).
				booleanValue()) {
			view.setDrawSingleNodes((Boolean) getValue(SELECTED_KEY));
			viewer.updateGraphLayout();
		}
	}
}