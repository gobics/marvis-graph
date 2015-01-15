/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author manuel
 */
public class DialogOptions extends DialogCentered implements ActionListener {

	private static final Logger logger = Logger.getLogger(DialogOptions.class.getName());
	private JPanel options_panel = new JPanel();
	private final JButton button_ok = new JButton("Ok");
	private final JButton button_cancel = new JButton("Cancel");
	private boolean ok_button_clicked = false;

	public DialogOptions(Window parent) {
		super(parent);
		if (getParent() == null) {
			setModalityType(ModalityType.APPLICATION_MODAL);
		} else {
			setModalityType(ModalityType.DOCUMENT_MODAL);
		}

		JPanel main = new JPanel(new BorderLayout());
		add(main);

		main.add(options_panel, BorderLayout.CENTER);
		options_panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel button_panel = new JPanel();
		main.add(button_panel, BorderLayout.PAGE_END);
		button_panel.add(Box.createHorizontalGlue());
		button_panel.add(button_cancel);
		button_panel.add(Box.createHorizontalStrut(25));
		button_panel.add(button_ok);
		button_ok.addActionListener(this);
		button_cancel.addActionListener(this);

		pack();
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(button_ok)) {
			logger.finer("Dialog " + getClass().getName() + " closed with ok");
			ok_button_clicked = true;
			dispose();
		} else if (event.getSource().equals(button_cancel)) {
			logger.finer("Dialog " + getClass().getName() + " closed with cancel");
			ok_button_clicked = false;
			dispose();
		} else {
			logger.warning("Can not handle action event from: " + event.getSource());
		}
	}

	public boolean closedWithOkButton() {
		return ok_button_clicked;
	}

	public JPanel getMainPanel() {
		return options_panel;
	}

	public void setMainPanelText(String text) {
		options_panel.setBorder(BorderFactory.createTitledBorder(text));
		pack();
	}

	/**
	 * Displays the dialog modal and returns true if the dialog has been closed
	 * with the OK button
	 * @return
	 */
	public boolean showDialog() {
		if (isVisible()) {
			return false;
		}

		if (getParent() == null) {
			setModalityType(ModalityType.APPLICATION_MODAL);
		}
		setVisible(true);
		return closedWithOkButton();
	}
}
