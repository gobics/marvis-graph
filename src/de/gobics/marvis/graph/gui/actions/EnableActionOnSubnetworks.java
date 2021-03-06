/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.TreeModelNetworks;
import javax.swing.Action;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 * Instances of this class enable actions if there is a main network in the 
 * treemodel and one or more subnetworks
 * @author manuel
 */
public class EnableActionOnSubnetworks implements TreeModelListener {

	private final Action action;
	private final TreeModelNetworks model;

	public EnableActionOnSubnetworks(final Action action, final TreeModelNetworks model) {
		this.action = action;
		this.model = model;
		model.addTreeModelListener(this);
		
		update();
	}

	public void update() {
		action.setEnabled(model.getRootNetwork() != null && model.getChildCount(model.getRoot()) > 0);
	}

	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		update();
	}

	@Override
	public void treeNodesInserted(TreeModelEvent e) {
		update();
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e) {
		update();
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {
		update();
	}
}
