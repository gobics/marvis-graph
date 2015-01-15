/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.pwtools;

import de.gobics.marvis.utils.swing.DialogOptions;
import java.awt.Window;
import javax.swing.JComboBox;

/**
 *
 * @author manuel
 */
public class DialogSelectPGDB extends DialogOptions {

	private final JComboBox cb;
	private final String[] ids;

	public DialogSelectPGDB(Window parent, String[] ids, String[] definitions) {
		super(parent);
		this.ids = ids;

		cb = new JComboBox(definitions);
		getMainPanel().add(cb);

		pack();
	}

	public String getSelectedPgdbId() {
		return ids[ cb.getSelectedIndex()];
	}
}
