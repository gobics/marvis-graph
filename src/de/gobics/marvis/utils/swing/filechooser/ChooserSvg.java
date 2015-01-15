/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class ChooserSvg extends ChooserAbstract {
	private static final ChooserSvg global_instance = new ChooserSvg();
	
	public ChooserSvg() {
		setAcceptAllFileFilterUsed(true);
		setFileFilter(new FileFilterSVG());
	}
	
	public static ChooserSvg getInstance(){
		return global_instance;
	}
}

