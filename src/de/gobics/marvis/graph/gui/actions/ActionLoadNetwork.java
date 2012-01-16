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
public class ActionLoadNetwork extends AbstractMarvisAction {

	public ActionLoadNetwork(MarvisGraphMainWindow window){
		super(window, "Load network");
		putValue(SHORT_DESCRIPTION, "Load a metabolic network");
		putValue(MNEMONIC_KEY, KeyEvent.VK_L);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().loadNetwork();
	}
	
}
