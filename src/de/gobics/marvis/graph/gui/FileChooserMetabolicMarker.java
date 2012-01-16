/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.utils.swing.filechooser.ChooserAbstract;
import de.gobics.marvis.utils.swing.filechooser.FileFilterCef;
import de.gobics.marvis.utils.swing.filechooser.FileFilterCsv;
import java.io.File;

/**
 *
 * @author manuel
 */
public class FileChooserMetabolicMarker extends ChooserAbstract {

	private static FileChooserMetabolicMarker global_instance;

	public FileChooserMetabolicMarker() {
		this(null);
	}

	public FileChooserMetabolicMarker(File selected) {
		addChoosableFileFilter(new FileFilterCef());
		addChoosableFileFilter(new FileFilterCsv());
		setAcceptAllFileFilterUsed(false);
		setMultiSelectionEnabled(true);
		setSelectedFile(selected);
	}

	public static FileChooserMetabolicMarker getInstance() {
		if (global_instance == null) {
			global_instance = new FileChooserMetabolicMarker();
		}
		return global_instance;
	}
}
