/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class ChooserOwl extends ChooserAbstract {
	private static ChooserOwl instance = null;
	
	public ChooserOwl(){
		setAcceptAllFileFilterUsed(false);
		setFileFilter(new FileFilterOwl());
	}
	
	
	public static ChooserOwl getInstance(){
		if( instance == null )
			instance = new ChooserOwl();
		return instance;
	}
	
}
