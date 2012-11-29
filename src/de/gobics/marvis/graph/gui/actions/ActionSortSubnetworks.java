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
public class ActionSortSubnetworks extends AbstractMarvisAction {

	public ActionSortSubnetworks(MarvisGraphMainWindow parent, final TreeModelNetworks model){
		super(parent, "Sort networks");
		
		new EnableActionOnSubnetworks(this, model);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().sort_subnetworks();
	}
	
}
