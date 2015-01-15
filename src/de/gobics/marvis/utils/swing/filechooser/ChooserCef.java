/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

import java.io.File;

/**
 *
 * @author manuel
 */
public class ChooserCef extends ChooserAbstract {

	private static final ChooserCef global_instance = new ChooserCef();

	public ChooserCef() {
		this(null);
	}

	public ChooserCef(File selected) {
		setAcceptAllFileFilterUsed(false);
		setMultiSelectionEnabled(false);
		addChoosableFileFilter(new FileFilterCef());
		setSelectedFile(selected);
	}

	public static ChooserCef getInstance() {
		return global_instance;
	}
}
