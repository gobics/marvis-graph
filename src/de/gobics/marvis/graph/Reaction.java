package de.gobics.marvis.graph;

public class Reaction extends GraphObject {
	
	String name = null;
	String equation = null;
	String description = null;
	String ecnumber = null;
	
	public Reaction(String id) {
		super( id);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setName(String name) {
		this.name= name;
	}

	public String getName() {
		return this.name;
	}

	public void setEquation(String equation) {
		this.equation = equation;
	}

	public String getEquation() {
		return this.equation;
	}

	public String getEcNumber() {
		return ecnumber;
	}

	public void setEcNumber(String ecnumber) {
		this.ecnumber = ecnumber;
	}
	
	
}
