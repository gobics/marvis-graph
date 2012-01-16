/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.MetabolicNetwork;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author manuel
 */
public class TreeModelNetworks implements TreeModel {
	private static final Logger logger = Logger.getLogger(TreeModelNetworks.class.getName());

	private final LinkedList<TreeModelListener> treemodel_listener = new LinkedList<TreeModelListener>();
	private MetabolicNetwork root;
	private MetabolicNetwork[] subgraphs;

	public TreeModelNetworks(MetabolicNetwork root) {
		this.root = root;
		this.subgraphs = new MetabolicNetwork[0];
	}
	
	public TreeModelNetworks(MetabolicNetwork root, MetabolicNetwork[] children) {
		this.root = root;
		this.subgraphs = children;
	}

	@Override
	public MetabolicNetwork getRoot() {
		return root;
	}
	public MetabolicNetwork getRootNetwork(){
		return root;
	}

	@Override
	public MetabolicNetwork getChild(Object parent, int index) {
		return subgraphs[index];
	}

	@Override
	public int getChildCount(Object parent) {
		return subgraphs.length;
	}

	@Override
	public boolean isLeaf(Object node) {
		if (root != null && node.equals(root)) {
			return false;
		}
		return true;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// Ignore because we do not allow this
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		for (int idx = 0; idx < subgraphs.length; idx++) {
			if (subgraphs[idx].equals(child)) {
				return idx;
			}
		}
		return -1;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		if( ! treemodel_listener.contains(l))
			treemodel_listener.add(l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		treemodel_listener.remove(l);
	}

	public MetabolicNetwork[] getSubnetworks() {
		return subgraphs.clone();
	}

	void setRoot(MetabolicNetwork new_network) {
		root = new_network;
		subgraphs = new MetabolicNetwork[0];
		fireTreeModelEvent();
	}
	
	void setSubnetworks(MetabolicNetwork[] new_networks) {
		logger.finer("Setting "+new_networks.length+" new subnetworks: "+Arrays.toString(new_networks));
		subgraphs = new_networks.clone();
		fireTreeModelEvent();
	}
	
	
	private void fireTreeModelEvent(){
		TreeModelEvent evt = new TreeModelEvent(this, new MetabolicNetwork[]{getRoot()});
		for(TreeModelListener l : treemodel_listener){
			logger.finer("Fire tree model event to listener: "+l);
			l.treeStructureChanged(evt);
		}
	}
}
