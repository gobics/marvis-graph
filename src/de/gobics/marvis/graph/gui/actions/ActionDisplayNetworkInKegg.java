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
public class ActionDisplayNetworkInKegg extends AbstractMarvisAction {
	
	private final JTree tree_graphs;
	
	public ActionDisplayNetworkInKegg(final MarvisGraphMainWindow window, final JTree tree_graphs) {
		super(window, "Display at KEGG");
		this.tree_graphs = tree_graphs;
		putValue(SHORT_DESCRIPTION, "Draws the selected network");
		new EnableActionOnTreeSelection(this, tree_graphs.getSelectionModel(), 1, 1);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().displayPathwayAtKegg();
	}
}
