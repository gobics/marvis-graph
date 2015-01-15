/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class ChooserCsvGz extends ChooserAbstract {
	private static final ChooserCsvGz global_instance = new ChooserCsvGz();
	
	public ChooserCsvGz() {
		setAcceptAllFileFilterUsed(false);
		addChoosableFileFilter(new FileFilterCsvGz());
		addChoosableFileFilter(new FileFilterCsv());
		setMultiSelectionEnabled(false);
	}
	
	public static ChooserCsvGz getInstance(){
		return global_instance;
	}
}

