/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class ChooserExcel extends ChooserAbstract {
	private static final ChooserExcel global_instance = new ChooserExcel();

	public ChooserExcel(){
		setAcceptAllFileFilterUsed(false);
		setFileFilter(new FileFilterExcel());
	}

	public static ChooserExcel getInstance(){
		return global_instance;
	}
}
