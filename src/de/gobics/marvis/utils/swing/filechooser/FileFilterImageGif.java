/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.swing.filechooser;


/**
 *
 * @author manuel
 */
public class FileFilterImageGif extends FileFilterAbstract {

	@Override
	public String getDescriptionName() {
		return "Graphics Interchange Format";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"gif"};
	}

}
