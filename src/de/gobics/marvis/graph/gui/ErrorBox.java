package de.gobics.marvis.graph.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import sun.awt.VerticalBagLayout;

/**
 *
 * @author manuel
 */
public class ErrorBox extends JDialog {

	public ErrorBox(Window parent, String message) {
		this(parent, message, null);
	}

	public ErrorBox(Window parent, String message, Throwable e) {
		super(parent, "Error", ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(new BorderLayout());

		JPanel message_label = new JPanel(new VerticalBagLayout());
		for (String s : message.split("\\n")) {
			message_label.add(new JLabel(s));
		}

		if (e != null) {
			JPanel error_panel = new JPanel(new BorderLayout());
			getContentPane().add(error_panel, BorderLayout.CENTER);

			error_panel.add(message_label, BorderLayout.PAGE_START);
			JTextArea textarea = new JTextArea();
			error_panel.add(new JScrollPane(textarea), BorderLayout.CENTER);

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			textarea.setText(sw.toString());
		}
		else {
			getContentPane().add(message_label, BorderLayout.CENTER);
		}

		JButton button_close = new JButton(new AbstractAction("Close") {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		button_close.setMaximumSize(button_close.getSize());
		getContentPane().add(button_close, BorderLayout.PAGE_END);
		pack();
		setLocationRelativeTo(parent);
	}
}
