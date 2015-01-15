/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.swing.filechooser.ChooserAbstract;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author manuel
 */
public class FilechooserTextField extends JPanel implements ActionListener, DocumentListener {

	public static final long serialVersionUID = 2412582753256l;
	private final JTextField field = new JTextField();
	private final Color field_background_color = field.getBackground();
	private final JButton button = new JButton("...");
	private final boolean select_save;
	private final ChooserAbstract chooser;

	public FilechooserTextField() {
		this(new ChooserAbstract());
	}

	public FilechooserTextField(File preselect) {
		this(new ChooserAbstract(), preselect);
	}

	public FilechooserTextField(ChooserAbstract chooser) {
		this(chooser, false, chooser.getSelectedFile());
	}

	public FilechooserTextField(ChooserAbstract chooser, File selected) {
		this(chooser, false, selected);
	}

	public FilechooserTextField(ChooserAbstract chooser, boolean select_save) {
		this(chooser, select_save, null);
	}

	public FilechooserTextField(ChooserAbstract chooser, boolean select_save, File selected) {
		setLayout(new BorderLayout());
		this.chooser = chooser;
		this.select_save = select_save;

		add(field, BorderLayout.CENTER);
		add(button, BorderLayout.LINE_END);
		button.addActionListener(this);

		Dimension d = field.getPreferredSize();
		d.width += button.getPreferredSize().width;
		this.setPreferredSize(d);

		field.getDocument().addDocumentListener(this);

		if (selected != null) {
			field.setText(selected.getAbsolutePath());
			chooser.setSelectedFile(selected);
		}
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(button)) {
			showChooserDialog();
		}
		else {
			throw new RuntimeException("Can not handle event: " + event);
		}

	}

	public void showChooserDialog() {
		field.setText("");

		int result = JFileChooser.CANCEL_OPTION;
		if (select_save) {
			result = chooser.showSaveDialog(this);
		}
		else {
			result = chooser.showOpenDialog(this);
		}

		if (result == JFileChooser.APPROVE_OPTION) {
			String new_file = chooser.getSelectedFileChecked().getAbsolutePath();
			field.setText(new_file);
			firePropertyChange("file.selected", field.getText(), new_file);
		}

		checkFileExists();
	}

	public File getSelectedFile() {
		if (field.getText() == null || field.getText().isEmpty()) {
			return null;
		}
		return new File(field.getText());
	}

	public File getSelectedFileWithShow() {
		File f = getSelectedFile();
		if (f == null) {
			showChooserDialog();
			return getSelectedFile();
		}
		return f;
	}

	public void setSelectedFile(File filename) {
		field.setText(filename.getAbsolutePath());
		chooser.setSelectedFile(filename);

	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = button.getPreferredSize();
		d.width += 200;
		return d;
	}

	private void checkFileExists() {
		File f = getSelectedFile();
		if (f == null) {
			field.setBackground(field_background_color);
		}
		else if (!select_save) {
			if (!f.exists()) {
				field.setBackground(Color.RED);
			}
			else {
				field.setBackground(field_background_color);
			}
		}
		firePropertyChange("file.selected", null, f != null ? f.getAbsolutePath() : null);
		repaint();
	}

	public void insertUpdate(DocumentEvent e) {
		if (e.getDocument().equals(field.getDocument())) {
			checkFileExists();
		}
		else {
			throw new RuntimeException("Can not handle event: " + e);
		}
	}

	public void removeUpdate(DocumentEvent e) {
		if (e.getDocument().equals(field.getDocument())) {
			checkFileExists();
		}
		else {
			throw new RuntimeException("Can not handle event: " + e);
		}
	}

	public void changedUpdate(DocumentEvent e) {
		if (e.getDocument().equals(field.getDocument())) {
			checkFileExists();
		}
		else {
			throw new RuntimeException("Can not handle event: " + e);
		}
	}
}
