/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.graphview.ReactionGraph;
import de.gobics.marvis.graph.gui.InternalFrameGraph;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

/**
 *
 * @author manuel
 */
public class ActionVisualizationDrawViewReaction extends AbstractVisualizationAction {

	public ActionVisualizationDrawViewReaction(InternalFrameGraph frame) {
		super(frame, "Show reaction view");
		putValue(SHORT_DESCRIPTION, "Draw a reaction network");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int response = JOptionPane.showConfirmDialog(null, "Only connect reactions if the compound they share is explainable?");
		if (response == JOptionPane.CANCEL_OPTION) {
			return;
		}
		boolean connect_only_explainable = response == JOptionPane.YES_OPTION;
		getInternalFrameGraph().drawNetwork(new ReactionGraph(getInternalFrameGraph().
				getMetabolicNetwork(), connect_only_explainable));
	}
}
