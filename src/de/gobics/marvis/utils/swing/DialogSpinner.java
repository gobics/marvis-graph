/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author manuel
 */
public class DialogSpinner extends DialogOptions {

	private final JLabel description = new JLabel("Please select value");
	private final JSpinner spinner = new JSpinner();

	public DialogSpinner(Window parent, double initial, double min_value, double max_value, double step) {
		this(parent, new SpinnerNumberModel(initial, min_value, max_value, step));
	}

	public DialogSpinner(Window parent, int initial, int min_value, int max_value, int step) {
		this(parent, new SpinnerNumberModel(initial, min_value, max_value, step));
	}

	public DialogSpinner(Window parent, SpinnerModel model) {
		super(parent);
		if (model == null) {
			throw new RuntimeException("Model can not be null");
		}

		spinner.setModel(model);

		JPanel main = getMainPanel();
		main.setLayout(new BorderLayout());
		main.add(description, BorderLayout.PAGE_START);
		main.add(spinner, BorderLayout.CENTER);
		spinner.setPreferredSize(new Dimension(100, 20));

		pack();
	}

	public void setDescription(String new_descri) {
		description.setText(new_descri);

		if (isVisible()) {
			pack();
			repaint();
		}
	}

	public Object getValue() {
		return spinner.getModel().getValue();
	}
}
