/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class ChooserTar extends ChooserAbstract {
	private static final ChooserTar global_instance = new ChooserTar();
	
	public ChooserTar() {
		setAcceptAllFileFilterUsed(false);
		addChoosableFileFilter(new FileFilterTar());
		setMultiSelectionEnabled(false);
	}
	
	public static ChooserTar getInstance(){
		return global_instance;
	}
}

