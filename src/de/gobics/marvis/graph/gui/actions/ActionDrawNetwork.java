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
import javax.swing.plaf.basic.BasicTreeUI.TreeSelectionHandler;

/**
 *
 * @author manuel
 */
public class ActionDrawNetwork extends AbstractMarvisAction {
	
	private final JTree tree_graphs;
	
	public ActionDrawNetwork(final MarvisGraphMainWindow window, final JTree tree_graphs) {
		super(window, "Draw network");
		this.tree_graphs = tree_graphs;
		putValue(SHORT_DESCRIPTION, "Draws the selected network");
		putValue(MNEMONIC_KEY, KeyEvent.VK_D);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK));

		new EnableActionOnTreeSelection(this, tree_graphs.getSelectionModel(), 1, 1);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().drawNetwork();
	}
}
