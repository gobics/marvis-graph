package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.sort.AbstractGraphScore;
import de.gobics.marvis.graph.sort.NetworkSorterName;
import de.gobics.marvis.graph.sort.NetworkSorterSEA;
import de.gobics.marvis.utils.swing.AbstractTask;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A background task to sort networks. Calculation of network scores can be
 * rather long and they are therefore calculated (based on a given sorting
 * scoring algorithm) and cached.
 *
 * @author manuel
 */
public class SortNetworksTask extends AbstractTask<MetabolicNetwork[], Void> {

	private static final Logger logger = Logger.getLogger(SortNetworksTask.class.
			getName());
	private final AbstractGraphScore sorter;
	private final MetabolicNetwork[] networks;
	private final MetabolicNetwork main_network;

	public SortNetworksTask(AbstractGraphScore sorter, MetabolicNetwork main_network, MetabolicNetwork[] networks) {
		if (sorter == null) {
			throw new NullPointerException("Sorting algorithm is null");
		}
		if (main_network == null) {
			throw new NullPointerException("Given main network is null");
		}
		if (networks == null) {
			throw new NullPointerException("Given network array is null");
		}
		this.sorter = sorter;
		this.main_network = main_network;
		this.networks = networks.clone();
	}

	@Override
	public MetabolicNetwork[] performTask() throws Exception {
		return sortNetworks();
	}

	public MetabolicNetwork[] sortNetworks() throws Exception {
		sendTitle("Sorting sub-networks");
		sendDescription("Calculation scores");
		ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, Runtime.
				getRuntime().availableProcessors() - 1));
		logger.fine("Starting threads to calculate the scores");
		// Determine the mode of sorting
		final boolean reverse_sorting = sorter instanceof NetworkSorterName || sorter instanceof NetworkSorterSEA ? false : true;
		
		// Set the main network
		sorter.setParent(main_network);

		// Build the data storages
		final Cache[] caches = new Cache[networks.length];
		LinkedList<Callable<Cache>> jobs = new LinkedList<>();
		setProgressMax(jobs.size());

		for (int i = 0; i < networks.length; i++) {
			final int idx = i;
			jobs.add(new Callable<Cache>() {

				@Override
				public Cache call() throws Exception {
					Cache result = new Cache(networks[idx], sorter.
							calculateScore(networks[idx]), reverse_sorting);
					incrementProgress();
					return result;
				}
			});
		}
		try {
			int idx = 0;
			List<Future<Cache>> results = pool.invokeAll(jobs);
			for (Future<Cache> f : results) {
				caches[idx] = f.get();
				idx++;
			}
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		finally {
			pool.shutdownNow();
		}

		if (isCancelled()) {
			return null;
		}

		logger.log(Level.FINE, "Sorting (cached) networks using native array sort method with algorithm: {0}", sorter);
		Arrays.sort(caches);

		for (int idx = 0; idx < networks.length; idx++) {
			networks[idx] = caches[idx].network;
		}

		return this.networks;
	}

	@SuppressWarnings("unchecked")
	private class Cache implements Comparable<Cache> {

		private final Comparable score;
		private final MetabolicNetwork network;
		private final boolean reverse;

		public Cache(MetabolicNetwork n, Comparable s) {
			this(n, s, false);
		}

		public Cache(MetabolicNetwork n, Comparable s, boolean reverse) {
			this.score = s;
			this.network = n;
			this.reverse = reverse;
		}

		@Override
		public int compareTo(Cache t) {
			int factor = reverse ? -1 : 1;
			return factor * score.compareTo(t.score);
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + network.getName() + ": " + score + "]";
		}
	}
}
