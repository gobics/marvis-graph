/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class ChooserTarGz extends ChooserAbstract {
	private static final ChooserTarGz global_instance = new ChooserTarGz();
	
	public ChooserTarGz() {
		setAcceptAllFileFilterUsed(false);
		addChoosableFileFilter(new FileFilterTar());
		setFileFilter(new FileFilterTarGz());
		setMultiSelectionEnabled(false);
	}
	
	public static ChooserTarGz getInstance(){
		return global_instance;
	}
}

