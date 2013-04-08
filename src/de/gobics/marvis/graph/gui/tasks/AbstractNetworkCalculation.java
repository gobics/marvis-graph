/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Reaction;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.graphview.ReactionGraph;
import de.gobics.marvis.utils.task.AbstractTask;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author manuel
 */
public abstract class AbstractNetworkCalculation extends AbstractTask<MetabolicNetwork[], Void> {

	private final MetabolicNetwork root_network;
	private ReactionGraph reactions_view;

	public AbstractNetworkCalculation(MetabolicNetwork root) {
		this.root_network = root;
		setCofactorThreshold(10);
		setTaskTitle("Detecting sub-networks");
	}

	public MetabolicNetwork getRootNetwork() {
		return root_network;
	}

	public void setCofactorThreshold(int cofactor_threshold) {
		reactions_view = new ReactionGraph(getRootNetwork(), false, cofactor_threshold);
	}

	public ReactionGraph getReactionView() {
		return reactions_view;
	}

	/**
	 * This method returns a new network calculator like the current one with
	 * equal settings but a new root network.
	 *
	 * @param new_root_network the new root network
	 */
	public abstract AbstractNetworkCalculation like(MetabolicNetwork new_root_network);

	/**
	 * Builds networks from the reactions.
	 *
	 * @param reactions
	 * @return
	 */
	public Collection<MetabolicNetwork> getSubnetworks(List<Reaction> reactions) {
		LinkedList<MetabolicNetwork> subs = new LinkedList<>();

		TreeSet<Reaction> visited = new TreeSet<>();
		while (!reactions.isEmpty()) {
			TreeSet<Reaction> reactions_for_subnet = new TreeSet<>();

			// Initialize list of nodes to visit
			LinkedList<Reaction> to_visit = new LinkedList<>();
			to_visit.add(reactions.get(0));
			reactions.remove(0);

			while (!to_visit.isEmpty()) {
				Reaction cur = to_visit.poll();
				reactions_for_subnet.add(cur);

				for (GraphObject neighbor_object : getReactionView().getNeighbors(cur)) {
					Reaction neighbor_reaction = (Reaction) neighbor_object;
					if (reactions.contains(neighbor_reaction) && !visited.contains(neighbor_reaction)) {
						to_visit.add(neighbor_reaction);
					}
					visited.add(neighbor_reaction);
				}
			}

			if (reactions_for_subnet.size() > 1) {
				MetabolicNetwork sub = generate_network(reactions_for_subnet);
				subs.add(sub);
			}

			for (Reaction r : reactions_for_subnet) {
				reactions.remove(r);
			}
		}

		return subs;
	}

	final protected MetabolicNetwork generate_network(Collection<Reaction> neighbor_nodes) {
		MetabolicNetwork network = new MetabolicNetwork(root_network);

		// Iterate over all objects and get there environments
		for (Reaction o : neighbor_nodes) {
			for (Relation r : root_network.getEnvironment(o)) {
				network.addRelation(r);
			}
		}

		Iterator<Reaction> iter = neighbor_nodes.iterator();
		while (network.getName() == null && iter.hasNext()) {
			Reaction next = iter.next();
			if (next.getName() != null) {
				network.setName("Subnetwork: " + next.getName());
				break;
			}
		}

		if (network.getName() == null && ! neighbor_nodes.isEmpty()) {
			network.setName("Subnetwork: " + neighbor_nodes.iterator().next().getId());
		}

		return network;
	}
}
