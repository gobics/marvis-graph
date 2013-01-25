/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.utils.swing.AbstractTask;

/**
 *
 * @author manuel
 */
public abstract class AbstractNetworkCalculation extends AbstractTask<MetabolicNetwork[], Void> {

	/**
	 * This method returns a new network calculator like the current one with
	 * equal settings but a new root network.
	 * @param new_root_network the new root network
	 */
	public abstract AbstractNetworkCalculation like(MetabolicNetwork new_root_network);
	
}
