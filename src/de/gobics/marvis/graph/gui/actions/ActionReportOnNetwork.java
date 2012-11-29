/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 *
 * @author manuel
 */
public class ActionReportOnNetwork extends AbstractMarvisAction {

	public ActionReportOnNetwork(MarvisGraphMainWindow window, final JTree tree_graphs) {
		super(window, "Report on network");
		putValue(SHORT_DESCRIPTION, "Displays a report of this metabolic network");
		//putValue(MNEMONIC_KEY, (Integer) KeyEvent.VK_S);
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

		new EnableActionOnTreeSelection(this, tree_graphs.getSelectionModel(), 1, 1);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().displayNetworkReport();
	}
}
