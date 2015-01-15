/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.swing.kegg;

import de.gobics.marvis.utils.swing.DialogOptions;
import java.awt.Window;

/**
 *
 * @author manuel
 */
public class DialogSelectOrganism extends DialogOptions{

	private final ComboBoxOrganism cb;

	public DialogSelectOrganism(Window parent, String[] organism_definitions){
		super(parent);
		cb = new ComboBoxOrganism(organism_definitions);
		getMainPanel().add(cb);

		pack();
	}

	public String getSelectedOrganismId(){
		return cb.getSelectedOrganismId();
	}

}
