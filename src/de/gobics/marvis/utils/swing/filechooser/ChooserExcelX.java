/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class ChooserExcelX extends ChooserAbstract {
	private static final ChooserExcelX global_instance = new ChooserExcelX();

	public ChooserExcelX(){
		setAcceptAllFileFilterUsed(false);
		setFileFilter(new FileFilterExcelX());
	}

	public static ChooserExcelX getInstance(){
		return global_instance;
	}
}
