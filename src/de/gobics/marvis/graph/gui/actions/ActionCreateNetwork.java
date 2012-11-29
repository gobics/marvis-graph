/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/**
 *
 * @author manuel
 */
public class ActionCreateNetwork extends AbstractMarvisAction {

	public ActionCreateNetwork(final MarvisGraphMainWindow window) {
		super(window, "Create new network");
		putValue(SHORT_DESCRIPTION, "Load/create a new network from different sources");
		putValue(MNEMONIC_KEY, KeyEvent.VK_N);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().createNewNetwork();
	}
}
