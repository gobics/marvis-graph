/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.InternalFrameGraph;
import java.awt.event.ActionEvent;

/**
 *
 * @author manuel
 */
public class ActionVisualizationDrawViewDefault extends AbstractVisualizationAction {

	public ActionVisualizationDrawViewDefault(InternalFrameGraph frame){
		super(frame, "Draw default visualization");
		putValue(SHORT_DESCRIPTION, "Updates visualization of the network");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		getInternalFrameGraph().drawNetwork();
	}
	
}
