/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.graphview.GraphViewCustomizable;
import de.gobics.marvis.graph.gui.InternalFrameGraph;
import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;

/**
 *
 * @author manuel
 */
public class ViewerActionChangeDisplaySettings extends AbstractViewerAction {

	private final InternalFrameGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionChangeDisplaySettings(final InternalFrameGraph viewer, final GraphViewCustomizable view) {
		super("Display type", "explainable.png");
		this.viewer = viewer;
		this.view = view;
		putValue(LONG_DESCRIPTION, "Change the display settings");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		viewer.changeDisplayTypeSettings(view);
	}
}