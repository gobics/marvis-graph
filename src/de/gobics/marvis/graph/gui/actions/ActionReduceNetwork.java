/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.graph.gui.TreeModelNetworks;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/**
 *
 * @author manuel
 */
public class ActionReduceNetwork extends AbstractMarvisAction {

	public ActionReduceNetwork(final MarvisGraphMainWindow window, final TreeModelNetworks tree_graphs) {
		super(window, "Clean network");
		putValue(SHORT_DESCRIPTION, "Remove objects from network without reactions");

		new EnableActionOnNetwork(this, tree_graphs).update();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().reduceNetwork();
	}
}
