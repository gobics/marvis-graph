/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.sort.AbstractGraphSort;
import de.gobics.marvis.graph.sort.NetworkSorterName;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author manuel
 */
public class SortNetworksTask extends SwingWorker<MetabolicNetwork[], Void> {

	private static final Logger logger = Logger.getLogger(SortNetworksTask.class.
			getName());
	private final AbstractGraphSort sorter;
	private final MetabolicNetwork[] networks;

	public SortNetworksTask(AbstractGraphSort sorter, MetabolicNetwork[] networks) {
		if (sorter == null) {
			throw new NullPointerException("Sorting algorithm is null");
		}
		if (networks == null) {
			throw new NullPointerException("Given network array is null");
		}
		this.sorter = sorter;
		this.networks = networks.clone();
	}

	@Override
	protected MetabolicNetwork[] doInBackground() throws Exception {
		return sortNetworks();
	}

	public MetabolicNetwork[] sortNetworks() throws Exception {
		getPropertyChangeSupport().firePropertyChange("description", null, "Sorting...");
		getPropertyChangeSupport().firePropertyChange("title", null, "Sorting sub networks");
		
		logger.fine("Sorting networks using native array sort method with algorithm: " + sorter);
		
		// Sort the arrays in reverse order. This results in "best-first" (except for names)
		if( sorter instanceof NetworkSorterName){
			Arrays.sort(networks, sorter);
		}
		else {
			Arrays.sort(networks, new ReverseComparator(sorter));
		}

		return this.networks;
	}
}

class ReverseComparator implements Comparator<MetabolicNetwork> {

	private final Comparator<MetabolicNetwork> comparator;

	public ReverseComparator(Comparator<MetabolicNetwork> comparator) {
		this.comparator = comparator;
	}

	@Override
	public int compare(MetabolicNetwork o1, MetabolicNetwork o2) {
		return -1 * comparator.compare(o1, o2);
	}
}