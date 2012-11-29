package de.gobics.marvis.graph.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.*;

/**
 *
 * @author manuel
 */
public class ErrorDialog {
	
	public static void show(Window parent, String message){
		show(parent, message, null);
	}
	public static void show(Window parent, String message, Throwable e){
		JPanel content_pane = new JPanel();
		content_pane.setLayout(new BoxLayout(content_pane, BoxLayout.PAGE_AXIS));
		
		JPanel message_label = new JPanel();
		for (String s : message.split("\n")) {
			message_label.add(new JLabel(s));
		}
		content_pane.add(message_label);

		if (e != null) {
			JTextArea textarea = new JTextArea();
			JScrollPane spane = new JScrollPane(textarea);
			spane.setPreferredSize(new Dimension(300, 200));
			spane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			content_pane.add(spane, BorderLayout.CENTER);

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			textarea.setText(sw.toString());
		}

		JOptionPane.showMessageDialog(parent, content_pane, "Error", JOptionPane.ERROR_MESSAGE);

	}
}
