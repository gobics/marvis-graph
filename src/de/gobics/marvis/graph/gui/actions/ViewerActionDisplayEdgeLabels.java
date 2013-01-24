/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.graphvisualizer.EdgeTransformerLabel;
import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import java.awt.event.ActionEvent;

/**
 *
 * @author manuel
 */
public class ViewerActionDisplayEdgeLabels extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;

	public ViewerActionDisplayEdgeLabels(final VisualizationViewerGraph viewer) {
		super("Draw edge label", "show_edge_label.png");
		this.viewer = viewer;
		putValue(LONG_DESCRIPTION, "Display labels of the edges");
		putValue(SELECTED_KEY, false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		EdgeTransformerLabel et = (EdgeTransformerLabel) viewer.getRenderContext().getEdgeLabelTransformer();
		et.setShowLabel(!et.isShowLabel());
		putValue(SELECTED_KEY, et.isShowLabel());
		viewer.updateUI();
	}
}