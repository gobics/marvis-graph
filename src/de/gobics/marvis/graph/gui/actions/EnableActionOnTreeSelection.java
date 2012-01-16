/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import javax.swing.Action;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

/**
 * Instances of this class enable their actions if the TreeSelection they are
 * monitoring has a selection available.
 * @author manuel
 */
public class EnableActionOnTreeSelection implements TreeSelectionListener {

	private final Action action;
	private final TreeSelectionModel model;
	private int limit_min;
	private int limit_max;

	public EnableActionOnTreeSelection(final Action action, final TreeSelectionModel model) {
		this(action, model, 1);
	}

	public EnableActionOnTreeSelection(final Action action, final TreeSelectionModel model, int limit_min) {
		this(action, model, limit_min, Integer.MAX_VALUE);
	}

	public EnableActionOnTreeSelection(final Action action, final TreeSelectionModel model, int limit_min, int limit_max) {
		this.action = action;
		this.model = model;
		setLimitMin(limit_min);
		setLimitMax(limit_max);
		model.addTreeSelectionListener(this);
		update();
	}

	public int getLimitMin() {
		return limit_min;
	}

	public void setLimitMin(int new_limit) {
		limit_min = Math.abs(new_limit);
		if (limit_min > getLimitMax()) {
			setLimitMax(limit_min);
		}
	}

	public int getLimitMax() {
		return limit_max;
	}

	public void setLimitMax(int new_limit) {
		limit_max = Math.abs(new_limit);
		if (limit_max < limit_min) {
			setLimitMin(limit_max);
		}
	}

	public void update() {
		int count = model.getSelectionCount();
		action.setEnabled(count >= limit_min && count <= limit_max);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		update();
	}
}
