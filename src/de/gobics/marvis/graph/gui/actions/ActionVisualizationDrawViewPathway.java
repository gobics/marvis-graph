/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.graphview.GraphViewPathways;
import de.gobics.marvis.graph.gui.InternalFrameGraph;
import java.awt.event.ActionEvent;

/**
 *
 * @author manuel
 */
public class ActionVisualizationDrawViewPathway extends AbstractVisualizationAction {

	public ActionVisualizationDrawViewPathway(InternalFrameGraph frame) {
		super(frame, "Show pathway view");
		putValue(SHORT_DESCRIPTION, "Draw a pathway network");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getInternalFrameGraph().drawNetwork(new GraphViewPathways(getInternalFrameGraph().getMetabolicNetwork()));
	}
}
