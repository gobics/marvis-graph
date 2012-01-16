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
public class ActionExit extends AbstractMarvisAction {

	public ActionExit(MarvisGraphMainWindow window) {
		super(window, "Exit");
		putValue(MNEMONIC_KEY, KeyEvent.VK_X);
		putValue(SHORT_DESCRIPTION, "Exit this window");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
				
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().exit();
	}
	
}
