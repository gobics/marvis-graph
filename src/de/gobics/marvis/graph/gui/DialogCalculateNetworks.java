/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksPathway;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksReaction;
import de.gobics.marvis.utils.swing.SpringUtilities;
import java.awt.Frame;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

/**
 *
 * @author manuel
 */
public class DialogCalculateNetworks extends DialogAbstract {

	private final ComboboxAlgorithm cb_algorithm = new ComboboxAlgorithm();
	private final SpinnerNumberModel sm_gaps = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
	private final SpinnerNumberModel sm_cofactor = new SpinnerNumberModel(50, 0, Integer.MAX_VALUE, 1);

	public DialogCalculateNetworks(Frame window) {
		super(window, "Calculate sub networks", ModalityType.DOCUMENT_MODAL);
		
		JPanel options_panel = new JPanel(new SpringLayout());
		options_panel.add(new JLabel("Select algorithm:"));
		options_panel.add(cb_algorithm);
		options_panel.add(new JLabel("Maximum gaps:"));
		options_panel.add(new JSpinner(sm_gaps));
		options_panel.add(new JLabel("Cofactor treshold:"));
		options_panel.add(new JSpinner(sm_cofactor));
		SpringUtilities.makeCompactGrid(options_panel);

		addOptions(options_panel);
	}

	public SwingWorker<MetabolicNetwork[], Void> getTask(MetabolicNetwork n) {
		if (cb_algorithm.getSelectedIndex() == 0) {
			CalculateNetworksReaction process = new CalculateNetworksReaction(n);
			process.setMaximumGaps(sm_gaps.getNumber().intValue());
			process.setCofactorTreshold(sm_cofactor.getNumber().intValue());
			return process;
		}

		return new CalculateNetworksPathway(n);
	}

	private class ComboboxAlgorithm extends JComboBox {

		public ComboboxAlgorithm() {
			addItem("Reactions");
			addItem("Pathways");
		}
	}
}