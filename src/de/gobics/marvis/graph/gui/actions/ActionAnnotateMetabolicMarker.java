/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.graph.gui.TreeModelNetworks;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 *
 * @author manuel
 */
public class ActionAnnotateMetabolicMarker extends AbstractMarvisAction {

	public ActionAnnotateMetabolicMarker(final MarvisGraphMainWindow window, final TreeModelNetworks model) {
		super(window, "Annotate metabolites");
		putValue(SHORT_DESCRIPTION, "(Re-)Annotate the metabolic markers");
		putValue(MNEMONIC_KEY, (Integer) KeyEvent.VK_A);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));

		
		new EnableActionOnNetwork(this, model).update();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().annotateMarker();
	}
}
