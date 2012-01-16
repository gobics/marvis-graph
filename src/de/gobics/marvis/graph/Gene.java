package de.gobics.marvis.graph;

public class Gene extends GraphObject {
	private String name = null;
	private String definition = null;
	
	public Gene(String id) {
		super(id);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDefinition(){
		return this.definition;
	}
	public void setDefinition(String definition){
		this.definition = definition;
	}
}
