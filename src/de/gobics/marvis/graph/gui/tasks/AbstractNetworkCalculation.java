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

	public int getCofactorThreshold() {
		return reactions_view.getCofactorThreshold();
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

	/**
	 * Generates a metabolic network based on the given reactions. T
	 *
	 * @param neighbor_nodes
	 * @return
	 */
	final protected MetabolicNetwork generate_network(Collection<Reaction> neighbor_nodes) {
		MetabolicNetwork network = new MetabolicNetwork(root_network);

		// Iterate over all objects and get there environments
		for (Reaction o : neighbor_nodes) {
			for (Relation r : root_network.getEnvironment(o)) {
				network.addRelation(r);
			}
		}

		network.setName("Subnetwork: " + getNetworkName(network));

		return network;
	}

	@Override
	protected MetabolicNetwork[] doTask() throws Exception {
		return calculateNetworks();
	}

	abstract public MetabolicNetwork[] calculateNetworks() throws Exception;

	/**
	 * Generate a name for the network that consists of the given reactions.
	 * This is generally the alphanumerical first name found in the list of the
	 * reactions.
	 *
	 * @param reactions
	 * @return a name for the network
	 */
	protected String getNetworkName(MetabolicNetwork network) {
		Iterator<Reaction> iter = network.getReactions().iterator();
		if( ! iter.hasNext() )
			return "Unkown";

		// Try to find a reaction with a name
		while (iter.hasNext()) {
			Reaction next = iter.next();
			if (next.getName() != null) {
				return next.getName();
			}
		}

		// Otherwise use first reactions ID
		return network.getReactions().iterator().next().getId();
	}
}
