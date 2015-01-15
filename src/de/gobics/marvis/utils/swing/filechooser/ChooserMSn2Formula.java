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
public class ChooserMSn2Formula extends ChooserAbstract {

	private static final ChooserMSn2Formula global_instance = new ChooserMSn2Formula();

	public ChooserMSn2Formula() {
		setAcceptAllFileFilterUsed(false);
		addChoosableFileFilter(new FileFilterMsn2Formula());
		setMultiSelectionEnabled(false);
	}

	public ChooserMSn2Formula(File selected) {
		this();
		setSelectedFile(selected);
	}

	public static ChooserMSn2Formula getInstance() {
		return global_instance;
	}
}
