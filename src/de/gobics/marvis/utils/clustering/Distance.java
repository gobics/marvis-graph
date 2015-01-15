package de.gobics.marvis.utils.clustering;

/**
 * A class implementing this interface is used to calculate the distance between
 * two instances of {@code T}. This is needed by cluster algorithms.
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public interface Distance<T> {

	public double distance(T t1, T t2);
	
	//public double distance(Cluster<T> t1, T t2);
	
	public double distance(Cluster<T> c1, Cluster<T> c2);
}
