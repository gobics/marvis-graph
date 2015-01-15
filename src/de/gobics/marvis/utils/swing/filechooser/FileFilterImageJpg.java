/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.swing.filechooser;


/**
 *
 * @author manuel
 */
public class FileFilterImageJpg extends FileFilterAbstract {

	@Override
	public String getDescriptionName() {
		return "Joint Photographic Experts Group";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"jpg", "jpeg", "jpe"};
	}

}
