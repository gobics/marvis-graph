/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.swing.filechooser;


/**
 *
 * @author manuel
 */
public class FileFilterXml extends FileFilterAbstract {
	@Override
	public String getDescriptionName() {
		return "XML files";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"xml"};
	}

}
