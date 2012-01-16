/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author manuel
 */
public class CalculateNetworksPathway extends SwingWorker<MetabolicNetwork[], Void> {

	private static final Logger logger = Logger.getLogger(CalculateNetworksPathway.class.
			getName());
	private final MetabolicNetwork root_network;
	private final LinkedList<MetabolicNetwork> found_networks = new LinkedList<MetabolicNetwork>();
	private int max_gaps = 0;

	public CalculateNetworksPathway(MetabolicNetwork network) {
		this.root_network = network;
	}

	@Override
	protected MetabolicNetwork[] doInBackground() throws Exception {
		return calculateNetworks();
	}

	public MetabolicNetwork[] calculateNetworks() throws Exception {
		logger.fine("Searching start nodes");
		getPropertyChangeSupport().firePropertyChange("description", "", "Searching start nodes");
		setProgress(0);
		TreeSet<Pathway> possible_start_nodes = new TreeSet<Pathway>(root_network.
				getPathways());

		logger.fine("Beginning calculation of subnetworks with " + possible_start_nodes.
				size() + " possible start nodes: " + possible_start_nodes);
		getPropertyChangeSupport().firePropertyChange("description", "", "Calculating sub networks");
		int max = possible_start_nodes.size();
		
		setProgress(0);
		int counter = 1;
		while (!possible_start_nodes.isEmpty()) {
			// Extract the first element from possible nodes
			Pathway next_vertex = possible_start_nodes.pollFirst();
			
			logger.finer("Using possible start node " + next_vertex);
			possible_start_nodes.remove(next_vertex);
			MetabolicNetwork new_network = generate_network(next_vertex);
			new_network.setName("Subnetwork: " + next_vertex.getName());
			found_networks.add(new_network);

			if (isCancelled()) {
				return null;
			}

			setProgress((counter++ / max) * 100);
		}

		setProgress(100);
		return found_networks.toArray(new MetabolicNetwork[found_networks.size()]);
	}

	private MetabolicNetwork generate_network(Pathway pathway) {
		MetabolicNetwork network = new MetabolicNetwork(root_network);

		for (Relation r : root_network.getAllPathwayComponents(pathway)) {
			network.addRelation(r);
		}

		return network;
	}

	public void setMaximumGaps(int intValue) {
		this.max_gaps = Math.abs(intValue);
	}
}