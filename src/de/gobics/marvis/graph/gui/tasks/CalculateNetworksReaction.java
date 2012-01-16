/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.*;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author manuel
 */
public class CalculateNetworksReaction extends SwingWorker<MetabolicNetwork[], Void> {

	private static final Logger logger = Logger.getLogger(CalculateNetworksReaction.class.
			getName());
	private final MetabolicNetwork root_network;
	private final LinkedList<MetabolicNetwork> found_networks = new LinkedList<MetabolicNetwork>();
	private int max_gaps = 0;
	private int cofactor_treshold = 100;

	public CalculateNetworksReaction(MetabolicNetwork network) {
		this.root_network = network;
	}

	@Override
	protected MetabolicNetwork[] doInBackground() throws Exception {
		return calculateNetworks();
	}

	public MetabolicNetwork[] calculateNetworks() throws Exception {
		logger.fine("Searching start nodes");
		getPropertyChangeSupport().firePropertyChange("description", "", "Searching start nodes");
		TreeSet<Reaction> possible_start_nodes = new TreeSet<Reaction>();
		for (Reaction reaction : root_network.getReactions()) {
			possible_start_nodes.add(reaction);
		}

		logger.fine("Beginning calculation of subnetworks with " + possible_start_nodes.
				size() + " possible start nodes: " + possible_start_nodes);
		getPropertyChangeSupport().firePropertyChange("description", "", "Calculating sub networks");
		double max = ((double) possible_start_nodes.size()) / 100.0d;
		if (max == 0) {
			max = 1;
		}

		// Store the initial number of nodes to set progress status
		int num_start_nodes = possible_start_nodes.size();


		setProgress(0);
		while (!possible_start_nodes.isEmpty()) {
			// Extract the first element from possible nodes
			Reaction next_vertex = possible_start_nodes.pollFirst();
			logger.finer("Using possible start node " + next_vertex);
			Set<Reaction> neighbor_nodes = fetch_all_neighbors(next_vertex);
			if (neighbor_nodes.isEmpty()) {
				continue;
			}
			possible_start_nodes.removeAll(neighbor_nodes);
			MetabolicNetwork new_network = generate_network(neighbor_nodes);
			new_network.setName("Subnetwork: " + next_vertex.getName());
			found_networks.add(new_network);

			if (isCancelled()) {
				return null;
			}

			setProgress((int) ((num_start_nodes - possible_start_nodes.size()) / max));
		}

		setProgress(100);
		logger.info("Found " + found_networks.size() + " networks");
		return found_networks.toArray(new MetabolicNetwork[found_networks.size()]);
	}

	private Set<Reaction> fetch_all_neighbors(Reaction start_vertex) {
		logger.fine("Fetching all neighbors of: " + start_vertex);
		TreeSet<Reaction> subnetwork_nodes = new TreeSet<Reaction>();
		TreeSet<Reaction> not_explainable = new TreeSet<Reaction>();
		TreeSet<Reaction> to_visit = new TreeSet<Reaction>();
		to_visit.add(start_vertex);

		// Iterate until to_visit is empty
		Reaction current;
		while ((current = to_visit.pollFirst()) != null) {
			if (root_network.isExplainableWithGap(current, max_gaps)) {
				subnetwork_nodes.add(current);
			}
			else {
				not_explainable.add(current);
				continue;
			}

			for (Compound c : root_network.getCompounds(current)) {
				if (root_network.getReactions(c).size() >= cofactor_treshold) {
					continue;
				}
				for (Reaction neighbor : root_network.getReactions(c)) {
					if (!subnetwork_nodes.contains(neighbor) && !not_explainable.
							contains(neighbor)) {
						to_visit.add(neighbor);
					}
				}
			}
		}

		logger.finer("Visited nodes: " + (subnetwork_nodes.size() + not_explainable.
				size()));
		logger.fine("Found subnetwork containing " + subnetwork_nodes.size() + " reaction nodes: " + subnetwork_nodes);

		return subnetwork_nodes;
	}

	private MetabolicNetwork generate_network(Set<Reaction> neighbor_nodes) {
		MetabolicNetwork network = new MetabolicNetwork(root_network);

		// Iterate over all objects and get there environments
		for (Reaction o : neighbor_nodes) {
			for (Relation r : root_network.getEnvironment(o)) {
				network.addRelation(r);
			}
		}

		return network;
	}

	public void setMaximumGaps(int intValue) {
		this.max_gaps = Math.abs(intValue);
	}
	
	public void setCofactorTreshold(int value){
		this.cofactor_treshold = Math.abs(value);
	}
}