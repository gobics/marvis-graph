/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;

/**
 *
 * @author manuel
 */
public class ViewerActionRedraw extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;

	public ViewerActionRedraw(final VisualizationViewerGraph viewer) {
		super("Redraw", "refresh.png");
		this.viewer = viewer;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F5"));
		putValue(LONG_DESCRIPTION, "Recalculate the network layout (F5)");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		viewer.updateGraphLayout();
	}
}