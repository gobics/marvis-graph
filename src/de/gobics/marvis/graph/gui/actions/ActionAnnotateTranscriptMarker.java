/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.graph.gui.TreeModelNetworks;
import java.awt.event.ActionEvent;

/**
 *
 * @author manuel
 */
public class ActionAnnotateTranscriptMarker extends AbstractMarvisAction {

	public ActionAnnotateTranscriptMarker(final MarvisGraphMainWindow window, final TreeModelNetworks model) {
		super(window, "Annotate transcript marker");
		putValue(SHORT_DESCRIPTION, "(Re-)Annotate the transcript marker");
		//putValue(MNEMONIC_KEY, (Integer) KeyEvent.VK_A);
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		
		new EnableActionOnNetwork(this, model).update();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().annotateTranscriptMarker();
	}
}
