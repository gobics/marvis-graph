package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import javax.swing.JComboBox;

/**
 * This combo box contains a list of available downloaded for metabolic
 * networks.
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class ComboBoxSource extends JComboBox {

	private final AbstractOptionsPanel kegg,
			biocyc;

	public ComboBoxSource(MarvisGraphMainWindow w) {
		addItem("BioCyc flat file");
		addItem("Kyoto Encyclopedia of Genes and Genomes");
		setSelectedIndex(0);
		kegg = new KeggOptionsPanel(w);
		biocyc = new BiocycOptionsPanel(w);
	}

	public AbstractOptionsPanel getOptionsPanel() {
		int idx = getSelectedIndex();
		if (idx == 0) {
			return biocyc;
		}
		else if (idx == 1) {
			return kegg;
		}
		throw new RuntimeException("Do not know the AbstractOptionsPanel of :" + getSelectedItem());
	}
}