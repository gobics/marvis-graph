/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

/**
 *
 * @author manuel
 */
public class DialogSpinner extends DialogAbstract {

	private final JPanel label_panel = new JPanel();
	private final JSpinner spinner = new JSpinner();

	public DialogSpinner(Frame owner, String title, ModalityType type) {
		this(owner, title, type, null);
	}

	public DialogSpinner(Frame owner, String title, boolean modal) {
		this(owner, title, modal, null);
	}

	public DialogSpinner(Frame owner, String title, boolean modal, SpinnerModel model) {
		super(owner, title, modal);
		init(model);
	}

	public DialogSpinner(Frame owner, String title, ModalityType type, SpinnerModel model) {
		super(owner, title, DEFAULT_MODALITY_TYPE);
		init(model);
	}

	private void init(SpinnerModel model) {
		if (model != null) {
			spinner.setModel(model);
		}
		setLabel("Select a value:");

		Dimension sd = spinner.getSize();
		sd.width = 300;
		sd.height += 25;

		spinner.setSize(sd);
		spinner.setPreferredSize(sd);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		panel.add(label_panel, BorderLayout.LINE_START);
		panel.add(spinner, BorderLayout.CENTER);

		addOptions(panel);

		pack();
	}

	public void setLabel(String new_label) {
		String[] lines = new_label.split("\n");
		label_panel.removeAll();
		label_panel.setLayout(new BoxLayout(label_panel, BoxLayout.PAGE_AXIS));
		
		for( String line : new_label.split("\n")){
			label_panel.add(new JLabel(line));
		}
	}

	public void setSpinnerModel(SpinnerModel model) {
		spinner.setModel(model);
	}

	public Object getValue() {
		return spinner.getModel().getValue();
	}
}
