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
public class ActionCalculateNetworks extends AbstractMarvisAction {

	public ActionCalculateNetworks(final MarvisGraphMainWindow window, final TreeModelNetworks tree_graphs) {
		super(window, "Calculate networks");
		putValue(SHORT_DESCRIPTION, "Calculate sub networks with experimental evidence");
		putValue(MNEMONIC_KEY, KeyEvent.VK_C);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));

		new EnableActionOnNetwork(this, tree_graphs).update();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().calculateSubnetworks();
	}
}
