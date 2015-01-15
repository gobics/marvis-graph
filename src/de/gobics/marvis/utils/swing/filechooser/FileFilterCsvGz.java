/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class FileFilterCsvGz extends FileFilterAbstract {
	@Override
	public String getDescriptionName() {
		return "Comma separated values gzipped";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"csv.gz"};
	}


}