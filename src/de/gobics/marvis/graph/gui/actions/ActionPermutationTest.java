/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.graph.gui.TreeModelNetworks;
import java.awt.event.ActionEvent;

/**
 *
 * @author manuel
 */
public class ActionPermutationTest extends AbstractMarvisAction {
	
	public ActionPermutationTest(final MarvisGraphMainWindow window, final TreeModelNetworks tree_graphs) {
		super(window, "Permutation Test");
		putValue(SHORT_DESCRIPTION, "Performs a permutation test on the found sub-networks");
		
		new EnableActionOnSubnetworks(this, tree_graphs);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().performPermutationTest();
	}
}
