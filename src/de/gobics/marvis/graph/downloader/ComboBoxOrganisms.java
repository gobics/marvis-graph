package de.gobics.marvis.graph.downloader;

import java.util.Arrays;
import java.util.Comparator;
import javax.swing.JComboBox;

/**
 * This combo box list the available organism.
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class ComboBoxOrganisms extends JComboBox {

	public ComboBoxOrganisms() {
		super();
	}

	public ComboBoxOrganisms(OrganismDescription[] organisms) {
		setOrganisms(organisms);
	}

	/**
	 * Set the available organism to the given organisms.
	 *
	 * @param organisms
	 */
	public void setOrganisms(OrganismDescription[] organisms) {
		removeAllItems();		
		if (organisms != null) {
			for (OrganismDescription od : organisms) {
				addItem(new OrganismItem(od));
			}
		}

	}

	public String getSelectedOrganismID() {
		return getSelectedOrganism().id;
	}

	private OrganismDescription getSelectedOrganism() {
		return ((OrganismItem) getSelectedItem()).organism;
	}

	/**
	 * Item to contain organism and render them in the ComboBox.
	 */
	private class OrganismItem {

		private final OrganismDescription organism;

		public OrganismItem(OrganismDescription o) {
			this.organism = o;
		}

		@Override
		public String toString() {
			return organism.name;
		}
	}

}
