package de.gobics.marvis.graph;
public class Enzyme extends GraphObject{
	String name = null;
	String description = null;
	String ec = null;
	
	public Enzyme(String id) {
		super(id);
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public String getEC() {
		return ec;
	}

	public void setEC(String ec) {
		this.ec = ec;
	}

}
