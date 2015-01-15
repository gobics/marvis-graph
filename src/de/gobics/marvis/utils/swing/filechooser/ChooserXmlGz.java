/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class ChooserXmlGz extends ChooserAbstract {
	private static final ChooserXmlGz global_instance = new ChooserXmlGz();
	
	public ChooserXmlGz() {
		setAcceptAllFileFilterUsed(false);
		setMultiSelectionEnabled(false);
		FileFilterXmlGzip foo = new FileFilterXmlGzip();
		addChoosableFileFilter(foo);
		addChoosableFileFilter(new FileFilterXml());
		setFileFilter(foo);
	}
	
	public static ChooserXmlGz getInstance(){
		return global_instance;
	}
}

