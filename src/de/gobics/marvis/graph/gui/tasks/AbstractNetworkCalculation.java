/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Reaction;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.utils.task.AbstractTask;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author manuel
 */
public abstract class AbstractNetworkCalculation extends AbstractTask<MetabolicNetwork[], Void> {

	private final MetabolicNetwork root_network;

	public AbstractNetworkCalculation(MetabolicNetwork root) {
		this.root_network = root;
		setTaskTitle("Detecting sub-networks");
	}
	
	public MetabolicNetwork getRootNetwork(){
		return root_network;
	}

	/**
	 * This method returns a new network calculator like the current one with
	 * equal settings but a new root network.
	 *
	 * @param new_root_network the new root network
	 */
	public abstract AbstractNetworkCalculation like(MetabolicNetwork new_root_network);

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
		
		if( network.getName() == null ){
			network.setName("Subnetwork: " + neighbor_nodes.iterator().next().getId());
		}

		return network;
	}
}
