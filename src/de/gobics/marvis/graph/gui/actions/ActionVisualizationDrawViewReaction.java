/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.graphview.ReactionGraph;
import de.gobics.marvis.graph.gui.InternalFrameGraph;
import java.awt.event.ActionEvent;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

/**
 *
 * @author manuel
 */
public class ActionVisualizationDrawViewReaction extends AbstractVisualizationAction {

	public ActionVisualizationDrawViewReaction(InternalFrameGraph frame) {
		super(frame, "Show reaction view");
		putValue(SHORT_DESCRIPTION, "Draw a reaction network");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JPanel panel = new JPanel();
		SpringLayout layout = new SpringLayout();
		panel.setLayout(layout);

		JCheckBox chb_explainable = new JCheckBox("Only connect via annotated compounds");
		panel.add(chb_explainable);
		JLabel l_cofactor = new JLabel("Hub metabolite threshold: ");
		panel.add(l_cofactor);
		SpinnerNumberModel sm_cofactor = new SpinnerNumberModel(10, 0, Integer.MAX_VALUE, 10);
		JSpinner s_cofactor = new JSpinner(sm_cofactor);
		panel.add(s_cofactor);

		// Position Checkbox in to area
		layout.putConstraint(SpringLayout.WEST, chb_explainable, 5, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, chb_explainable, 5, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.EAST, panel, 5, SpringLayout.EAST, chb_explainable);
		// Position label
		layout.putConstraint(SpringLayout.WEST, l_cofactor, 5, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, l_cofactor, 5, SpringLayout.SOUTH, chb_explainable);
		// Position spinner
		layout.putConstraint(SpringLayout.WEST, s_cofactor, 5, SpringLayout.EAST, l_cofactor);
		layout.putConstraint(SpringLayout.NORTH, s_cofactor, 5, SpringLayout.SOUTH, chb_explainable);
		layout.putConstraint(SpringLayout.EAST, panel, 5, SpringLayout.EAST, s_cofactor);
		layout.putConstraint(SpringLayout.SOUTH, panel, 5, SpringLayout.SOUTH, s_cofactor);
		
		int response = JOptionPane.showConfirmDialog(null, panel, "Reaction view options", JOptionPane.OK_CANCEL_OPTION);
		if (response != JOptionPane.OK_OPTION) {
			return;
		}

		getInternalFrameGraph().drawNetwork(
				new ReactionGraph(getInternalFrameGraph().getMetabolicNetwork(), chb_explainable.isSelected(), sm_cofactor.getNumber().intValue()));
	}
}
