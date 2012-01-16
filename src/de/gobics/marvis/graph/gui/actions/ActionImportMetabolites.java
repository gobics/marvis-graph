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
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 *
 * @author manuel
 */
public class ActionImportMetabolites extends AbstractMarvisAction {
	
	public ActionImportMetabolites(final MarvisGraphMainWindow window, final TreeModelNetworks model){
		super(window, "Import metabolites");
		putValue(SHORT_DESCRIPTION, "Imports metabolites from a CSV file");
		//putValue(MNEMONIC_KEY, (Integer)KeyEvent.VK_N);
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		
		new EnableActionOnNetwork(this, model).update();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().importMetabolites();
	}
	
}
