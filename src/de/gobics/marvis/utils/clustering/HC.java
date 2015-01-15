package de.gobics.marvis.utils.clustering;

import cern.colt.function.IntIntDoubleFunction;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import de.gobics.marvis.utils.matrix.MatrixIO;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * This class implements the basic hierachical clustering algorithm.
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class HC<T> {

	private static final Logger logger = Logger.getLogger(HC.class.getName());
	private final T[] data;
	private final Distance<T> distance;
	private final LinkedList<Cluster<T>> cluster = new LinkedList<Cluster<T>>();
	private DenseDoubleMatrix2D distances;

	public HC(T[] data, Distance<T> distance) {
		this.distance = distance;
		this.data = data;
		initCluster();

	}

	public HC(Cluster<T>[] cluster, Distance<T> distance) {
		this.distance = distance;
		this.data = null;
		this.cluster.addAll(Arrays.asList(cluster));
	}

	public HC(Collection<Cluster<T>> cluster, Distance<T> distance) {
		this.distance = distance;
		this.data = null;
		this.cluster.addAll(cluster);
	}

	private void initCluster() {
		cluster.clear();
		for (T t : data) {
			cluster.add(new Cluster<T>(distance, t));
		}
	}

	@SuppressWarnings("unchecked")
	public Cluster<T> cluster() {
		logger.fine("Clustering " + cluster.size() + " items");

		calculateDistances();
		while (cluster.size() > 2) {
			Cluster<T>[] pair = getMinimunPair();
			updateDistances(pair[0], pair[1]);
		}

		return new Cluster<T>(distance, cluster.get(0), cluster.get(1));
	}

	/**
	 * Calculates the pairwise distances between all clusters.
	 */
	private void calculateDistances() {
		logger.fine("Calculating all pairwise distances");
		int size = cluster.size();
		distances = new DenseDoubleMatrix2D(size, size);
		for (int row_idx = 0; row_idx < size; row_idx++) {
			for (int col_idx = row_idx + 1; col_idx < size; col_idx++) {
				double d = distance.distance(cluster.get(row_idx), cluster.get(col_idx));
				distances.setQuick(row_idx, col_idx, d);
				distances.setQuick(col_idx, row_idx, d);
			}
		}
	}

	/**
	 * Searches the distances matrix for a pair with minimum distance. The
	 * returned array will always have a length of 2.
	 *
	 * @return a Cluster[2] array
	 */
	private Cluster[] getMinimunPair() {
		int idx1 = -1;
		int idx2 = -1;
		double min = Double.MAX_VALUE;

		for (int row_idx = 0; row_idx < distances.rows(); row_idx++) {
			for (int col_idx = row_idx + 1; col_idx < distances.columns(); col_idx++) {
				if (distances.getQuick(row_idx, col_idx) < min) {
					min = distances.getQuick(row_idx, col_idx);
					idx1 = row_idx;
					idx2 = col_idx;
				}
			}
		}

		return new Cluster[]{cluster.get(idx1), cluster.get(idx2)};
	}

	/**
	 * Merge the given two cluster and update all distances in the distance
	 * matrix.
	 *
	 * @param c1
	 * @param c2
	 */
	private void updateDistances(Cluster<T> c1, Cluster<T> c2) {
		int idx1 = cluster.indexOf(c1);
		int idx2 = cluster.indexOf(c2);
		if (idx1 > idx2) { // ensure that idx1 < idx2
			int tmp = idx1;
			idx1 = idx2;
			idx2 = tmp;
		}
		// Create a new cluster based on the two clusters to merge.
		Cluster<T> new_cluster = new Cluster<T>(distance, c1, c2);

		// Construct a new matrix containing one row and one column less than the former distance matrix
		final DenseDoubleMatrix2D new_distances = new DenseDoubleMatrix2D(cluster.
				size() - 1, cluster.size() - 1);

		// Build an array containing the indizes without the indizes of cluster c1 and c2
		int[] view_selection = new int[cluster.size() - 2];
		int view_idx = 0;
		for (int i = 0; i < cluster.size(); i++) {
			if (i != idx1 && i != idx2) {
				view_selection[view_idx++] = i;
			}
		}
		// Get the selection and copy the data
		distances.viewSelection(view_selection, view_selection).forEachNonZero(new IntIntDoubleFunction() {

			public double apply(int i, int i1, double d) {
				new_distances.setQuick(i, i1, d);
				return d;
			}
		});

		// Remove the old clusters and add the new one
		cluster.remove(idx2); // Use reverse oder to not destroy the indizes
		cluster.remove(idx1);
		cluster.add(new_cluster);

		// Recalculate the pairwise distances
		int row_idx = cluster.indexOf(new_cluster);
		for (int col_idx = 0; col_idx < cluster.size(); col_idx++) {
			double d = distance.distance(new_cluster, cluster.get(col_idx));
			new_distances.setQuick(row_idx, col_idx, d);
			new_distances.setQuick(col_idx, row_idx, d);
		}

		this.distances = new_distances;
	}
}
