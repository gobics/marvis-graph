/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.utils.swing.AbstractTask;
import de.gobics.marvis.graph.*;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author manuel
 */
public class CalculateNetworksReaction extends AbstractTask<MetabolicNetwork[], Void> {

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
	protected MetabolicNetwork[] performTask() throws Exception {
		return calculateNetworks();
	}

	public MetabolicNetwork[] calculateNetworks() throws Exception {
		logger.fine("Searching start nodes");
		sendDescription("Searching start nodes");
		TreeSet<Reaction> possible_start_nodes = new TreeSet<Reaction>();
		for (Reaction reaction : root_network.getReactions()) {
			possible_start_nodes.add(reaction);
		}

		logger.fine("Beginning calculation of subnetworks with " + possible_start_nodes.
				size() + " possible start nodes: " + possible_start_nodes);
		sendDescription("Calculating sub networks");
		setProgressMax(possible_start_nodes.size());

		// Store the initial number of nodes to set progress status
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
			found_networks.add(new_network);

			Iterator<Reaction> reactions = new_network.getReactions().iterator();
			while (reactions.hasNext() && new_network.getName() == null) {
				Reaction r = reactions.next();
				if (r.getName() != null) {
					new_network.setName("Subnetwork: " + r.getName());
				}
			}
			if (new_network.getName() == null) {
				new_network.setName("Subnetwork: " + next_vertex.getId());
			}



			if (isCancelled()) {
				return null;
			}


			incrementProgress(new_network.getReactions().size());
		}

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
				// If this compound is a cofactor or not explainable do not go further
				if (root_network.getReactions(c).size() >= cofactor_treshold || !root_network.
						isExplainable(c)) {
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

	public void setCofactorTreshold(int value) {
		this.cofactor_treshold = Math.abs(value);
	}
}