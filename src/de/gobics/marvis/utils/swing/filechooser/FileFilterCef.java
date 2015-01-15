/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class FileFilterCef extends FileFilterAbstract {
	
	@Override
	public String getDescriptionName() {
		return "Agilent compound exchange format";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"cef"};
	}


}