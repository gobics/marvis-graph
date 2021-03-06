package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.gui.tasks.AbstractNetworkCalculation;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksPathway;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksReaction;
import de.gobics.marvis.utils.swing.SpringUtilities;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

/**
 *
 * @author manuel
 */
public class DialogCalculateNetworks extends DialogAbstract {

	private static final ComboboxAlgorithm cb_algorithm = new ComboboxAlgorithm();
	private static final SpinnerNumberModel sm_gaps = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
	private static final SpinnerNumberModel sm_cofactor = new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1);
	private static final SpinnerNumberModel sm_rwr = new SpinnerNumberModel(0.8, 0, 1, 0.1);
	private static final SpinnerNumberModel sm_rwr_threshold = new SpinnerNumberModel(0.2, 0, 1, 0.1);
	private final JPanel options_panel = new JPanel(new BorderLayout());
	private final JPanel options_rwr = new OptionsRWR();
	private final JPanel options_reaction = new OptionsReaction();

	public DialogCalculateNetworks(Frame window) {
		super(window, "Calculate sub networks", ModalityType.DOCUMENT_MODAL);

		final JPanel algorithm_panel = new JPanel(new SpringLayout());
		algorithm_panel.add(new JLabel("Select algorithm:"));
		algorithm_panel.add(cb_algorithm);
		SpringUtilities.makeCompactGrid(algorithm_panel);

		options_panel.add(algorithm_panel, BorderLayout.PAGE_START);
		options_panel.add(options_rwr, BorderLayout.CENTER);

		addOptions(options_panel);

		cb_algorithm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				options_panel.removeAll();
				options_panel.add(algorithm_panel, BorderLayout.PAGE_START);

				if (cb_algorithm.getSelectedIndex() == 0) {
					options_panel.add(options_rwr, BorderLayout.CENTER);
				}
				else {
					if (cb_algorithm.getSelectedIndex() == 1) {
						options_panel.add(options_reaction, BorderLayout.CENTER);
					}
				}
				pack();
			}
		});
	}

	public AbstractNetworkCalculation getTask(MetabolicNetwork n) {
		if (cb_algorithm.getSelectedIndex() == 0) {
			CalculateNetworksRWR process = new CalculateNetworksRWR(n);
			process.setRestartProbability(sm_rwr.getNumber().doubleValue());
			process.setThreshold(sm_rwr_threshold.getNumber().doubleValue());
			process.setCofactorThreshold(sm_cofactor.getNumber().intValue());
			return process;
		}
		if (cb_algorithm.getSelectedIndex() == 1) {
			CalculateNetworksReaction process = new CalculateNetworksReaction(n);
			process.setMaximumGaps(sm_gaps.getNumber().intValue());
			process.setCofactorTreshold(sm_cofactor.getNumber().intValue());
			return process;
		}

		return new CalculateNetworksPathway(n);
	}

	private static class ComboboxAlgorithm extends JComboBox {

		public ComboboxAlgorithm() {
			addItem("Random walk based");
			addItem("Reactions");
			addItem("Pathways");
		}
	}

	private static class OptionsReaction extends JPanel {

		public OptionsReaction() {
			super(new SpringLayout());
			add(new JLabel("Maximum gaps:"));
			add(new JSpinner(sm_gaps));
			add(new JLabel("Hub metabolite treshold:"));
			add(new JSpinner(sm_cofactor));
			SpringUtilities.makeCompactGrid(this);
		}
	}

	private static class OptionsRWR extends JPanel {

		public OptionsRWR() {
			super(new SpringLayout());
			add(new JLabel("Restart probability:"));
			add(new JSpinner(sm_rwr));
			add(new JLabel("Score threshold:"));
			add(new JSpinner(sm_rwr_threshold));
			add(new JLabel("Hub metabolite treshold:"));
			add(new JSpinner(sm_cofactor));
			SpringUtilities.makeCompactGrid(this);
		}
	}
}