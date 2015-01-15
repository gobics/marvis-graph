/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author manuel
 */
public class ChooserDirectory extends ChooserAbstract {

	private static final ChooserDirectory global_instance = new ChooserDirectory();

	public ChooserDirectory() {
		setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		setAcceptAllFileFilterUsed(true);

	}

	public ChooserDirectory(File selected) {
		addChoosableFileFilter(new FileFilterCsv());
		setSelectedFile(selected);
	}

	public static ChooserDirectory getInstance() {
		return global_instance;
	}
}
