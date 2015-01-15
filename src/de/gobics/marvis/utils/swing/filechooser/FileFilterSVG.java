/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.swing.filechooser;


/**
 *
 * @author manuel
 */
public class FileFilterSVG extends FileFilterAbstract {
	@Override
	public String getDescriptionName() {
		return "Scalable Vector Graphic";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"svg"};
	}

}
