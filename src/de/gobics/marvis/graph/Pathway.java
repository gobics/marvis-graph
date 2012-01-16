package de.gobics.marvis.graph;

public class Pathway extends GraphObject {
	String name = null;
	String description = null;
	
	public Pathway( String id) {
		super(id);
	}
	
	public String getName() {
	return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String desctription) {
		this.description = desctription;
	}
}
