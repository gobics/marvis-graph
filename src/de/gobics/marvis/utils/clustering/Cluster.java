package de.gobics.marvis.utils.clustering;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * This class represents a hierarchical cluster. It can consist of several
 * datapoints or some subcluster.
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class Cluster<T> {
	private final Distance<T> distance;
	private final Cluster<T>[] subcluster;
	private final LinkedList<T> datapoints;

	public Cluster(Distance<T> distance, T datapoint) {
		this.distance = distance;
		subcluster = new Cluster[0];
		datapoints = new LinkedList<T>();
		datapoints.add(datapoint);
	}

	public Cluster(Distance<T> distance, T[] lipids) {
		this.distance = distance;
		subcluster = new Cluster[0];
		datapoints = new LinkedList<T>();
		datapoints.addAll(Arrays.asList(lipids));
	}

	public Cluster(Distance<T> distance, Cluster a, Cluster b) {
		this.distance = distance;
		datapoints = null;
		subcluster = new Cluster[]{a, b};
	}
	
	public Distance<T> getDistance(){
		return distance;
	}

	/**
	 * Returns true if this cluster contains sub clusters.
	 *
	 * @return
	 */
	public boolean hasSubcluster() {
		return subcluster.length > 0;
	}

	public int countSubcluster() {
		return subcluster.length;
	}

	public int countAllSubcluster() {
		return getAllSubcluster().size();
	}

	/**
	 * Returns true if this cluster contains datapoints (excluding subcluster).
	 *
	 * @return
	 */
	public boolean hasDatapoints() {
		return datapoints != null;
	}

	public int countDatapoints() {
		if (!hasDatapoints()) {
			return 0;
		}
		return datapoints.size();
	}

	/**
	 * Returns the number of datapoints that are located in this cluster
	 * (including datapoints from sub-cluster.
	 *
	 * @return
	 */
	public int countAllDatapoints() {
		return getAllDatapoints().size();
	}

	/**
	 * Returns the datapoints of this cluster.
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Collection<T> getDatapoints() {
		return (Collection<T>) datapoints.clone();
	}

	/**
	 * Returns a collection of all datapoints in this cluster and in all
	 * subclusters.
	 *
	 * @return all datapoints
	 */
	public Collection<T> getAllDatapoints() {
		LinkedList<T> datas = new LinkedList<T>();
		getAllDatapointsImpl(datas);
		return datas;
	}

	/**
	 * First adds alls datapoints of the current cluster in the {@code datas}
	 * parameter. Then iterates over all subcluster and calls {@code getAllDatapointsImpl}
	 * recursively.
	 *
	 * @param datas the collection to insert the datapoints into.
	 */
	private void getAllDatapointsImpl(Collection<T> datas) {
		if (hasDatapoints()) {
			datas.addAll(datapoints);
		}
		for (Cluster<T> c : subcluster) {
			c.getAllDatapointsImpl(datas);
		}
	}

	public Cluster[] getSubcluster() {
		return subcluster.clone();
	}

	/**
	 * Returns a collection of all subcluster in this cluster and in all
	 * subclusters.
	 *
	 * @return all datapoints
	 */
	public Collection<Cluster<T>> getAllSubcluster() {
		LinkedList<Cluster<T>> datas = new LinkedList<Cluster<T>>(Arrays.asList(subcluster));
		getAllSubclusterImpl(datas);
		return datas;
	}

	/**
	 * First adds alls subcluster of the current cluster in the {@code datas}
	 * parameter. Then iterates over all subcluster and calls {@code getAllSubclusterImpl}
	 * recursively.
	 *
	 * @param datas the collection to insert the datapoints into.
	 */
	private void getAllSubclusterImpl(Collection<Cluster<T>> datas) {
		datas.addAll(Arrays.asList(subcluster));
		for (Cluster<T> c : subcluster) {
			c.getAllSubclusterImpl(datas);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" + countDatapoints() + "/" + countSubcluster() + "/" + countAllDatapoints() + "/" + countAllSubcluster() + "}";
	}

	public String humanReadable() {
		StringBuilder sb = new StringBuilder("[");
		for (T l : getAllDatapoints()) {
			sb.append(l).append(",");
		}
		return sb.append("]").toString();
	}

	/**
	 * Returns the height of the cluster. That is the number of layers of
	 * cluster beneath him.
	 *
	 * @return
	 */
	public int getHeight() {
		if (!hasSubcluster()) {
			return 1;
		}
		int max = 0;
		for (Cluster<T> child : getSubcluster()) {
			max = Math.max(max, child.getHeight());
		}
		return max + 1;
	}
	
	public double getInternalDistance(){
		if( countSubcluster() != 2){
			throw new RuntimeException("Can not calculate distance "+countSubcluster()+" subcluster");
		}
		return distance.distance(subcluster[0], subcluster[1]);
	}
}
