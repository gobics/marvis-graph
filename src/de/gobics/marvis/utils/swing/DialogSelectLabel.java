/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.ArrayUtils;
import java.awt.BorderLayout;
import java.awt.Window;
import java.util.logging.Logger;
import javax.swing.JComboBox;

/**
 *
 * @author manuel
 */
public class DialogSelectLabel extends DialogOptions {

	private static final Logger logger = Logger.getLogger(DialogSelectLabel.class.getName());
	private final JComboBox label_select;

	public DialogSelectLabel(Window parent, Object[] labels, Object preselected) {
		super(parent);
		setTitle("Select label");

		label_select = new JComboBox(labels);
		getMainPanel().setLayout(new BorderLayout());
		getMainPanel().add(label_select, BorderLayout.CENTER);


		logger.finer("Preselected item is: "+preselected);
		if (preselected != null) {
			int idx = ArrayUtils.indexOf(labels, preselected);
			logger.finer("Setting preselected index: " + idx);
			if (idx >= 0) {
				label_select.setSelectedIndex(idx);
			}
		}

		pack();
	}

	public Object getSelectedLabel() {
		return label_select.getSelectedItem();
	}
}
