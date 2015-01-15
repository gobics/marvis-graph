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
public class ChooserCsv extends ChooserAbstract {
	private static final ChooserCsv global_instance = new ChooserCsv();
	public ChooserCsv() {
		setAcceptAllFileFilterUsed(false);
		addChoosableFileFilter(new FileFilterCsv());
		setMultiSelectionEnabled(false);
	}
	
	public ChooserCsv(File selected) {
		addChoosableFileFilter(new FileFilterCsv());
		setAcceptAllFileFilterUsed(false);
		setMultiSelectionEnabled(false);
		setSelectedFile(selected);
	}
	
	public static ChooserCsv getInstance(){
		return global_instance;
	}
}

