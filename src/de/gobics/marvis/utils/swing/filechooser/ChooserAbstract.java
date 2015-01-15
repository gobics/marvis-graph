/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

import java.awt.Component;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author manuel
 */
public class ChooserAbstract extends JFileChooser {

	private static final Logger logger = Logger.getLogger(ChooserAbstract.class.
			getName());

	public ChooserAbstract() {
		this(new File("."));
	}

	public ChooserAbstract(File preselected) {
		super(preselected);
	}

	public ChooserAbstract(String current_directory_path) {
		super(current_directory_path);
	}

	/**
	 * If a file has been selected it is returned containing a extension that
	 * has been specified by the used FileFilter.
	 *
	 * @return set selected file
	 */
	public File getSelectedFileChecked() {
		File selected = getSelectedFile();
		FileFilter filter = getFileFilter();

		if (selected == null) {
			return null;
		}
		if (filter == null) {
			return selected;
		}

		if (filter instanceof FileFilterAbstract) {
			return ((FileFilterAbstract) filter).validateExtension(selected);
		}
		return selected;
	}

	public File doChooseFileOpen(Component parent) {
		return doChooseFileOpen(parent, this);
	}

	public static File doChooseFileOpen(Component parent, ChooserAbstract chooser) {
		if (chooser.showOpenDialog(parent) != APPROVE_OPTION) {
			return null;
		}
		return chooser.getSelectedFile();
	}

	public File doChooseFileSave(Component parent) {
		return doChooseFileSave(parent, this);
	}

	public static File doChooseFileSave(Component parent, ChooserAbstract chooser) {
		if (chooser.showSaveDialog(parent) != APPROVE_OPTION) {
			return null;
		}
		return chooser.getSelectedFileChecked();
	}
}
