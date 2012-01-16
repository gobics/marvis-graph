/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.graphview.GraphViewCustomizable;
import de.gobics.marvis.graph.gui.VisualizationViewerGraph;
import java.awt.event.ActionEvent;

/**
 *
 * @author manuel
 */
public class ViewerActionDrawExplainableNodesOnly extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionDrawExplainableNodesOnly(final VisualizationViewerGraph viewer, GraphViewCustomizable view) {
		super("Draw explainable nodes only", "explainable.png");
		this.viewer = viewer;
		this.view = view;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if( view.drawExplainableNodesOnly() ){
			view.setDrawExplainableNodesOnly(false);
		} else {
			view.setDrawExplainableNodesOnly(true);
		}
		viewer.updateGraphLayout();
	}
}