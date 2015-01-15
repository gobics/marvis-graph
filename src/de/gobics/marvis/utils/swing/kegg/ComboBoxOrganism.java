/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.kegg;

import java.util.Arrays;
import java.util.TreeMap;
import javax.swing.*;

/**
 *
 * @author manuel
 */
public class ComboBoxOrganism extends JComboBox {

	public ComboBoxOrganism() {
		this(new String[0]);
	}

	public ComboBoxOrganism(String[] defs) {
		setOrganisms(defs);
	}

	public void setOrganisms(String[] defs) {
		Arrays.sort(defs);

		this.removeAllItems();
		this.addItem(new OrganismComboBoxEntry("map", "Reference Pathways"));
		this.addItem("-----------------------------");
		TreeMap<String, String> sorted_defs = new TreeMap<String, String>();
		for (String def : defs) {
			String[] tokens = def.split(" - ");
			sorted_defs.put(tokens[1], tokens[0]);
		}

		for(String key : sorted_defs.keySet()){
			this.addItem(new OrganismComboBoxEntry(sorted_defs.get(key), key));
		}

		this.setSelectedIndex(0);
	}

	public String getSelectedOrganismId() {
		Object o = getSelectedItem();
		if (o instanceof OrganismComboBoxEntry) {
			return ((OrganismComboBoxEntry) o).getId();
		}
		return null;
	}
}

class OrganismComboBoxEntry {

	private final String org_id;
	private final String org_name;

	public OrganismComboBoxEntry(String id, String name) {
		org_id = id;
		org_name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getName() {
		return org_name;
	}

	public String getId() {
		return org_id;
	}
}
