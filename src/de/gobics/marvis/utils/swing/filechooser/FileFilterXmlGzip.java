/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.swing.filechooser;

import java.io.File;

/**
 *
 * @author manuel
 */
public class FileFilterXmlGzip extends FileFilterAbstract {

	@Override
	public String getDescriptionName() {
		return "GZipped XML files";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"xml.gz", "xgz"};
	}

}
