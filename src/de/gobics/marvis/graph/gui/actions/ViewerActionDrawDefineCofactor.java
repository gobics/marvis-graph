/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.graphview.GraphViewCustomizable;
import de.gobics.marvis.graph.gui.DialogSpinner;
import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author manuel
 */
public class ViewerActionDrawDefineCofactor extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionDrawDefineCofactor(final VisualizationViewerGraph viewer, GraphViewCustomizable view) {
		super("Define cofactor limit", "cofactor.png");
		this.viewer = viewer;
		this.view = view;
		putValue(LONG_DESCRIPTION, "Change the number of reactions to set a compound a cofactor");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int current_value = view.getCofactorLimit();
		if (current_value < 0) {
			current_value = 10;
		}

		SpinnerNumberModel model = new SpinnerNumberModel(current_value, 0, Integer.MAX_VALUE, 1);
		JSpinner spinner = new JSpinner(model);
		spinner.setMaximumSize(new Dimension(300, 100));
		int result = JOptionPane.showConfirmDialog(null, spinner, "Select cofactor limit", JOptionPane.OK_CANCEL_OPTION);
		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		Number n = (Number) spinner.getValue();
		if (n.intValue() == view.getCofactorLimit()) {
			return;
		}

		Logger.getLogger(ViewerActionDrawDefineCofactor.class.getName()).finer("Setting cofactor limit to: " + n.intValue());
		view.setCofactorLimit(n.intValue());
	}
}