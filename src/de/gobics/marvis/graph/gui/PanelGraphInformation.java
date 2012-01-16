/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.MetabolicNetwork;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/**
 *
 * @author manuel
 */
class PanelGraphInformation extends JPanel {

	private final static Logger logger = Logger.getLogger(PanelGraphInformation.class.
			getName());
	private final JLabel label_molecules = new JLabel("Molecules"),
			label_reactions = new JLabel("Reactions"),
			label_enzymes = new JLabel("Enzymes"),
			label_genes = new JLabel("Genes"),
			label_pathways = new JLabel("Pathways"),
			label_marker = new JLabel("Marker"),
			label_transcripts = new JLabel("Transcripts");
	private final JLabel count_max_molecules = new JLabel("0"),
			count_max_reactions = new JLabel("0"),
			count_max_enzymes = new JLabel("0"),
			count_max_genes = new JLabel("0"),
			count_max_pathways = new JLabel("0"),
			count_max_marker = new JLabel("0"),
			count_max_transcripts = new JLabel("0");
	private final JTree tree_graphs;

	public PanelGraphInformation(final JTree tree_graphs) {
		this.tree_graphs = tree_graphs;

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(
				layout.createParallelGroup().addGroup(layout.
				createSequentialGroup().addComponent(label_molecules, 0, 200, Short.MAX_VALUE).
				addComponent(count_max_molecules)).addGroup(layout.
				createSequentialGroup().addComponent(label_reactions, 0, 200, Short.MAX_VALUE).
				addComponent(count_max_reactions)).addGroup(layout.
				createSequentialGroup().addComponent(label_enzymes, 0, 200, Short.MAX_VALUE).
				addComponent(count_max_enzymes)).addGroup(layout.
				createSequentialGroup().addComponent(label_genes, 0, 200, Short.MAX_VALUE).
				addComponent(count_max_genes)).addGroup(layout.
				createSequentialGroup().addComponent(label_pathways, 0, 200, Short.MAX_VALUE).
				addComponent(count_max_pathways)).addGroup(layout.
				createSequentialGroup().addComponent(label_marker, 0, 200, Short.MAX_VALUE).
				addComponent(count_max_marker)).addGroup(layout.
				createSequentialGroup().addComponent(label_transcripts, 0, 200, Short.MAX_VALUE).
				addComponent(count_max_transcripts)));
		layout.setVerticalGroup(
				layout.createParallelGroup().addGroup(layout.
				createSequentialGroup().addComponent(label_molecules).
				addComponent(label_reactions).addComponent(label_enzymes).
				addComponent(label_genes).addComponent(label_pathways).addGap(10).
				addComponent(label_marker).addComponent(label_transcripts)).
				addGroup(layout.createSequentialGroup().addComponent(count_max_molecules).
				addComponent(count_max_reactions).addComponent(count_max_enzymes).
				addComponent(count_max_genes).addComponent(count_max_pathways).
				addGap(10).addComponent(count_max_marker).addComponent(count_max_transcripts)));


		tree_graphs.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				updateGraphInformation();
			}
		});
	}

	public void updateGraphInformation() {
		clearDisplay();

		TreePath path = tree_graphs.getSelectionPath();
		if (path == null) {
			return;
		}

		Object o = path.getLastPathComponent();
		if (!(o instanceof MetabolicNetwork)) {
			return;
		}

		display((MetabolicNetwork) o);
	}

	public void display(MetabolicNetwork network) {
		if (network == null) {
			clearDisplay();
		}
		else {
			count_max_molecules.setText("" + network.getMolecules().size());
			count_max_reactions.setText("" + network.getReactions().size());
			count_max_enzymes.setText("" + network.getEnzymes().size());
			count_max_genes.setText("" + network.getGenes().size());
			count_max_pathways.setText("" + network.getPathways().size());
			count_max_marker.setText("" + network.getMarkers().size());
			count_max_transcripts.setText("" + network.getTranscripts().size());
		}
	}

	public void clearDisplay() {
		count_max_molecules.setText("0");
		count_max_reactions.setText("0");
		count_max_enzymes.setText("0");
		count_max_genes.setText("0");
		count_max_pathways.setText("0");
		count_max_marker.setText("0");
		count_max_transcripts.setText("0");
	}
}
