package de.gobics.marvis.graph;

public class Transcript extends InputObject {

	private String aa_sequence = null;
	private String nt_sequence = null;

	public Transcript(String id) {
		super(id);
	}

	public String getAASequence() {
		return aa_sequence;
	}

	public void setAASequence(String aa_sequence) {
		this.aa_sequence = aa_sequence;
	}

	public boolean hasAASequence() {
		return aa_sequence != null && !aa_sequence.isEmpty();
	}

	public String getNTSequence() {
		return nt_sequence;
	}

	public void setNTSequence(String nt_sequence) {
		this.nt_sequence = nt_sequence;
	}

	public boolean hasNTSequence() {
		return nt_sequence != null && !nt_sequence.isEmpty();
	}
}
