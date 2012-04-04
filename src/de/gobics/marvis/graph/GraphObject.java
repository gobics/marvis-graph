package de.gobics.marvis.graph;

/**
 * Graph objects are basic classes in the metabolic graph.
 *
 * @author Manuel Landesfeind <manuel@gobics.de>
 */
public abstract class GraphObject implements Comparable<GraphObject> {

	/**
	 * Every object in a graph MUST have an ID beside its class. These IDs will
	 * be kept unique by the MetabolicNetwork class
	 */
	protected String id;
	/**
	 * An URL to access the object within its source database.
	 */
	private String url = null;

	/**
	 * Creates a new GraphObject. Instances of the subclasses will be created by
	 * the {@link MetabolicNetwork}
	 *
	 * @param id
	 */
	public GraphObject(String id) {
		if (id == null) {
			throw new RuntimeException("ID string can not be null");
		}
		this.id = id;
	}

	/**
	 * Returns the ID of the graph object
	 *
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Returns a name of this object. This method may be overridden by
	 * subclasses.
	 *
	 * @return
	 */
	public String getName() {
		return this.getId();
	}

	/**
	 * Returns true iff an URL is set for this object.
	 *
	 * @return
	 */
	public boolean hasUrl() {
		return url != null && !url.isEmpty();
	}

	/**
	 * Set the URL to the given string.
	 *
	 * @param url the new URL
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Returns the URL
	 *
	 * @return the URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int compareTo(GraphObject other) {
		if (!this.getClass().equals(other.getClass())) {
			return this.getClass().getName().compareTo(other.getClass().getName());
		}
		return getId().compareTo(other.getId());
	}

	/**
	 * Returns a string containing the class of this object and its ID.
	 *
	 * @return a string refering to this object
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getId() + "]";
	}

	/**
	 * Returns true if the this and the other object have the same class and ID.
	 *
	 * @param other
	 * @return
	 */
	public boolean equals(GraphObject other) {
		return getClass().equals(other.getClass()) && getId().equals(other.getId());
	}
}
