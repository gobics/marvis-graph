package de.gobics.marvis.graph;
/**
 * A transcript is some kind of experimental measurement that references to a
 * gene in the metabolic network, e.g. from mirco-array, EST or RNA-Seq.
 * @author Manuel Landesfein &lt;manuel@gobics.de&gt;
 */
public class Transcript extends ExperimentalMarker {

	/**
	 * Construction of transcripts should be done using {@link MetabolicNetwork#createTranscript(java.lang.String) }.
	 * @param id 
	 */
	public Transcript(String id) {
		super(id);
	}
}
