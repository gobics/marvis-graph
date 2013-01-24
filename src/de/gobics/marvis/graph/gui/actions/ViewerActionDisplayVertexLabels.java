/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.graphvisualizer.EdgeTransformerLabel;
import de.gobics.marvis.graph.gui.graphvisualizer.VertexTransformerLabel;
import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import java.awt.event.ActionEvent;

/**
 *
 * @author manuel
 */
public class ViewerActionDisplayVertexLabels extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;

	public ViewerActionDisplayVertexLabels(final VisualizationViewerGraph viewer) {
		super("Draw vertex label", "show_vertex_label.png");
		this.viewer = viewer;
		putValue(LONG_DESCRIPTION, "Display labels of the vertices");
		putValue(SELECTED_KEY, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		VertexTransformerLabel vt = (VertexTransformerLabel) viewer.getRenderContext().getVertexLabelTransformer();
		vt.setShowLabel(!vt.isShowLabel());
		putValue(SELECTED_KEY, vt.isShowLabel());
		viewer.updateUI();
	}
}