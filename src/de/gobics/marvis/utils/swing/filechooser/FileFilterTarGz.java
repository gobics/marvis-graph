/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class FileFilterTarGz extends FileFilterAbstract {
	@Override
	public String getDescriptionName() {
		return "Tar archive (gzipped)";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"tar.gz", "tgz"};
	}


}