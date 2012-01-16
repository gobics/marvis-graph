/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.MetabolicNetwork;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author manuel
 */
public class TreeModelGraphobjects implements TreeModel {

	private final MetabolicNetwork network;
	private final Class[] classes;

	public TreeModelGraphobjects(MetabolicNetwork n) {
		if (n == null) {
			throw new NullPointerException("Given network is null");
		}
		network = n;
		classes = n.getAllObjectClasses();

	}

	@Override
	public Object getRoot() {
		return network;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent.equals(network)) {
			return classes[index];
		}
		if (parent instanceof Class) {
			return network.getAllObjects((Class) parent).get(index);
		}
		throw new RuntimeException("This may be a leaf: " + parent);
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent.equals(network)) {
			return classes.length;
		}
		if (parent instanceof Class) {
			return network.getAllObjects((Class) parent).size();
		}
		return 0;
	}

	@Override
	public boolean isLeaf(Object node) {
		return node instanceof GraphObject;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// Ignore this
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent.equals(network)) {
			for (int idx = 0; idx < classes.length; idx++) {
				if (classes[idx].equals(child)) {
					return idx;
				}
			}
			return -1;
		}
		if (parent instanceof Class) {
			return network.getAllObjects((Class) parent).indexOf(child);
		}
		throw new RuntimeException("This may be a leaf: " + parent);
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		// TreeModel is not editable - no changes will happen
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		// TreeModel is not editable - no changes will happen
	}
}
