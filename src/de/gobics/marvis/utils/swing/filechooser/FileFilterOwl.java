/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

/**
 *
 * @author manuel
 */
public class FileFilterOwl extends FileFilterAbstract {

	@Override
	public String getDescriptionName() {
		return "Web Ontology Language";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"owl", "xml"};
	}
	
}
