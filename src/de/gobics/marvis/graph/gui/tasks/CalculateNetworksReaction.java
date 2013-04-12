/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.*;
import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class CalculateNetworksReaction extends AbstractNetworkCalculation {

	private static final Logger logger = Logger.getLogger(CalculateNetworksReaction.class.
			getName());
	private int max_gaps = 0;
	private int cofactor_treshold = 100;

	public CalculateNetworksReaction(MetabolicNetwork network) {
		super(network);
		setTaskTitle("Detect subnetworks with reaction heuristic");
	}

	@Override
	public AbstractNetworkCalculation like(MetabolicNetwork new_root_network) {
		CalculateNetworksReaction clone = new CalculateNetworksReaction(getRootNetwork());
		clone.setMaximumGaps(max_gaps);
		clone.setCofactorTreshold(cofactor_treshold);
		return clone;
	}

	@Override
	public MetabolicNetwork[] calculateNetworks() throws Exception {
		logger.fine("Searching start nodes");
		setTaskDescription("Searching start nodes");


		List<Reaction> subnetwork_nodes = new LinkedList<>();
		setTaskDescription("Calculating sub networks");

		for (Reaction r : getRootNetwork().getReactions()) {
			if (getRootNetwork().isExplainableWithGap(r, max_gaps)) {
				subnetwork_nodes.add(r);
			}

			if (isCanceled()) {
				return null;
			}
		}

		Collection<MetabolicNetwork> found_networks = getSubnetworks(subnetwork_nodes);
		logger.info("Found " + found_networks.size() + " networks");
		return found_networks.toArray(new MetabolicNetwork[found_networks.size()]);
	}

	public void setMaximumGaps(int intValue) {
		this.max_gaps = Math.abs(intValue);
	}

	public void setCofactorTreshold(int value) {
		this.cofactor_treshold = Math.abs(value);
	}
}