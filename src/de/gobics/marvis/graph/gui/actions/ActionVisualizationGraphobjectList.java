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
public class ActionVisualizationGraphobjectList extends AbstractVisualizationAction {

	public ActionVisualizationGraphobjectList(final InternalFrameGraph frame) {
		super(frame, "Show object list");
		putValue(SHORT_DESCRIPTION, "Creates a new tab containing a list of all objects in this metabolic network");

		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
}
