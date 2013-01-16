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

/**
 *
 * @author manuel
 */
public class ViewerActionDrawExplainableNodesOnly extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionDrawExplainableNodesOnly(final VisualizationViewerGraph graph_viewer, GraphViewCustomizable graph_view) {
		super("Draw explainable nodes only", "explainable.png");
		this.viewer = graph_viewer;
		this.view = graph_view;

		view.addGraphViewListener(new GraphViewListener() {
			@Override
			public void graphChanged(GraphView parent) {
				putValue(SELECTED_KEY, view.drawExplainableNodesOnly());
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (view.drawExplainableNodesOnly()) {
			view.setDrawExplainableNodesOnly(false);
		}
		else {
			view.setDrawExplainableNodesOnly(true);
		}
	}
}