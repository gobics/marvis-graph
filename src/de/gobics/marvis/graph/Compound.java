package de.gobics.marvis.graph;

public class Compound extends GraphObject {
    String name = null;
    String description = null;
    String formula = null;
    float mass = 0f;
	String inchi = null;

    public Compound(String id) {
        super(id);
    }

    public void setName(String name) {
        this.name = name;
    }

	@Override
    public String getName() {
        return this.name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setMass(float mass) {
        if (mass < 0) {
            mass = 0f;
        }
        this.mass = mass;
    }

    public float getMass() {
        return this.mass;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getFormula() {
        return this.formula;
    }

	public String getInchi() {
		return inchi;
	}

	public void setInchi(String inchi) {
		this.inchi = inchi;
	}

	public boolean hasInchi(){
		return inchi != null && ! inchi.isEmpty();
	}
	
	@Override
	public String toString(){
		return "Compound{id="+getId()+";mass="+getMass()+";name='"+getName()+"'}";
	}

}
