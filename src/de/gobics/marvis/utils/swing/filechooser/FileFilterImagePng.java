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
public class FileFilterImagePng extends FileFilterAbstract {

	@Override
	public String getDescriptionName() {
		return "Portable Network Graphics";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"png"};
	}
}
