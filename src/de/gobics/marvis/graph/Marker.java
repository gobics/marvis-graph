package de.gobics.marvis.graph;

public class Marker extends InputObject {
	private static final long serialVersionUID = -6811893137140545957L;
	String correctionfactor = "UNKNOWN";
	Double mass = 0.0;
	float rt = -1;
	String[] additional_data = null;
	
	public Marker(String id) {
		super(id);
	}
	
	public void setMass(Double mass) {
		if (mass != null && mass < 0)
			mass = 0D;
		this.mass = mass;
	}

	public Double getMass() {
		return this.mass;
	}
	
	public void setCorrectionfactor(String cf){
		this.correctionfactor = cf;
	}
	
	public String getCorrectionfactor(){
		return this.correctionfactor;
	}

	public void setRetentionTime(float new_rt){
		if( new_rt < 0 )
			new_rt = -1;
		this.rt = new_rt;
	}

	public float getRetentionTime(){
		return rt;
	}

	public void setAdditionalData(String[] ad){
		this.additional_data = ad;
	}
	public String[] getAdditionalData(){
		return additional_data;
	}
}
