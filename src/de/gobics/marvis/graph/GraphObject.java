package de.gobics.marvis.graph;

public abstract class GraphObject implements Comparable<GraphObject> {
	protected String id;
	private String url = null;

	public GraphObject(String id) {
		if( id == null ){
			throw new RuntimeException("ID string can not be null");
		}
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.getId();
	}

	public boolean hasUrl(){
		return url != null && ! url.isEmpty();
	}
	public void setUrl(String url){
		this.url = url;
	}
	public String getUrl(){
		return url;
	}

	@Override
	public final int compareTo(GraphObject other) {
		if (!this.getClass().equals(other.getClass())) {
			return this.getClass().getName().compareTo(other.getClass().getName());
		}
		return getId().compareTo(other.getId());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getId() + "]";
	}

	public boolean equals(GraphObject other) {
//		System.out.println("Compare " + this + " to " + other);
//		System.out.println(" Class: " + getClass().equals(other.getClass()));
//		System.out.println(" ID:    " + getId().equals(other.getId()));
//		System.out.println(" Result: " + (getClass().equals(other.getClass()) && getId().equals(other.getId())));
		return getClass().equals(other.getClass()) && getId().equals(other.getId());
	}
}
