/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksPathway;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksReaction;
import de.gobics.marvis.utils.swing.SpringUtilities;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
    private final SpinnerNumberModel sm_rwr = new SpinnerNumberModel(0.8, 0, 1, 0.1);
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
                } else if (cb_algorithm.getSelectedIndex() == 1) {
                    options_panel.add(options_reaction, BorderLayout.CENTER);
                }
                pack();
            }
        });
    }

    public SwingWorker<MetabolicNetwork[], Void> getTask(MetabolicNetwork n) {
        if (cb_algorithm.getSelectedIndex() == 0) {
            CalculateNetworksRWR process = new CalculateNetworksRWR(n);
            process.setRestartProbability(sm_rwr.getNumber().doubleValue());
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

    private class ComboboxAlgorithm extends JComboBox {

        public ComboboxAlgorithm() {
            addItem("Random walk based");
            addItem("Reactions");
            addItem("Pathways");
        }
    }

    private class OptionsReaction extends JPanel {

        public OptionsReaction() {
            super(new SpringLayout());
            add(new JLabel("Maximum gaps:"));
            add(new JSpinner(sm_gaps));
            add(new JLabel("Cofactor treshold:"));
            add(new JSpinner(sm_cofactor));
            SpringUtilities.makeCompactGrid(this);
        }
    }

    private class OptionsRWR extends JPanel {

        public OptionsRWR() {
            add(new JLabel("Restart probability:"));
            add(new JSpinner(sm_rwr));
        }
    }
}