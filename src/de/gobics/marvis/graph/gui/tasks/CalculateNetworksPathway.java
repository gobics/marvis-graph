/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.utils.swing.AbstractTask;
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
public class CalculateNetworksPathway extends AbstractNetworkCalculation {

	private static final Logger logger = Logger.getLogger(CalculateNetworksPathway.class.
			getName());
	private final LinkedList<MetabolicNetwork> found_networks = new LinkedList<MetabolicNetwork>();

	public CalculateNetworksPathway(MetabolicNetwork network) {
		super(network);
	}

	@Override
	public AbstractNetworkCalculation like(MetabolicNetwork network) {
		return new CalculateNetworksPathway(network);
	}

	@Override
	protected MetabolicNetwork[] doTask() throws Exception {
		return calculateNetworks();
	}

	public MetabolicNetwork[] calculateNetworks() throws Exception {
		logger.fine("Searching start nodes");
		setTaskDescription("Searching start nodes");
		TreeSet<Pathway> possible_start_nodes = new TreeSet<Pathway>(getRootNetwork().
				getPathways());

		logger.fine("Beginning calculation of subnetworks with " + possible_start_nodes.
				size() + " possible start nodes: " + possible_start_nodes);
		setTaskDescription("Calculating sub networks");
		setProgressMax(possible_start_nodes.size());


		while (!possible_start_nodes.isEmpty()) {
			// Extract the first element from possible nodes
			Pathway next_pathway = possible_start_nodes.pollFirst();

			logger.finer("Using possible start node " + next_pathway);
			possible_start_nodes.remove(next_pathway);
			MetabolicNetwork new_network = generate_network(getRootNetwork().getReactions(next_pathway));
			new_network.setName("Subnetwork: " + (next_pathway.getName() != null ? next_pathway.
												  getName() : next_pathway.getId()));
			found_networks.add(new_network);

			if (isCanceled()) {
				return null;
			}

			incrementProgress();
		}

		return found_networks.toArray(new MetabolicNetwork[found_networks.size()]);
	}

}