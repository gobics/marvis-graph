/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class FileFilterMsn2Formula extends FileFilterAbstract {

	@Override
	public String getDescriptionName() {
		return "MSn2Formula analysis";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"msn2formula"};
	}
}