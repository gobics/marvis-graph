/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author manuel
 */
public class ButtonBrowseUri extends JButton implements ActionListener {

	private static final Logger logger = Logger.getLogger(ButtonBrowseUri.class.getName());
	private final URI uri;

	public ButtonBrowseUri(URI uri) {
		super("Browse");
		this.uri = uri;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		logger.finer("Got callback: " + e);
		if (!e.getSource().equals(this)) {
			return;
		}

		if (Desktop.isDesktopSupported()) {
			try {
				logger.fine("Try to browse: " + uri);
				Desktop.getDesktop().browse(uri);
			}
			catch (Exception ex) {
				logger.log(Level.SEVERE, "Can not execute browser: ", ex);
				JTextArea area = new JTextArea(uri.toASCIIString());
				area.setLineWrap(true);
				area.setWrapStyleWord(false);
				JScrollPane spane = new JScrollPane(area);
				spane.setPreferredSize(new Dimension(300, 100));
				JOptionPane.showMessageDialog(this, spane, "URL", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else {
			logger.warning("Desktop is not supported");
		}
	}
}
