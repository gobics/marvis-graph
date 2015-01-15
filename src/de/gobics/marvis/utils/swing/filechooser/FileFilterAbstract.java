/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

import de.gobics.marvis.utils.swing.SpringUtilities;
import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author manuel
 */
public abstract class FileFilterAbstract extends FileFilter {

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		}

		for (String ext : getDefaultExtensions()) {
			if (file.getName().toLowerCase().endsWith("." + ext)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		for (String ext : getDefaultExtensions()) {
			sb.append(", ").append("*.").append(ext);
		}

		return getDescriptionName() + " (" + sb.toString().substring(2) + ")";
	}

	public abstract String getDescriptionName();

	public abstract String[] getDefaultExtensions();

	public File validateExtension(File input_file) {
		String[] ext = getDefaultExtensions();

		for (String e : ext) {
			if (input_file.getName().endsWith("." + e)) {
				return input_file;
			}
		}
		return new File(input_file.getPath() + "." + ext[0]);
	}
}
