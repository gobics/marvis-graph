package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.*;
import java.util.Collection;

/**
 *
 * @author manuel
 */
public class AnnotateTranscripts extends AnnotateAbstract<Transcript> {


	public AnnotateTranscripts(MetabolicNetwork graph) {
		super(graph);
		setTaskDescription("Annotate genes with transcript marker");
	}


	@Override
	protected void annotate(Transcript object) {
		if (object.hasAnnotation()) {
			addAnnotations(RelationshipType.TRANSCRIPT_ISFROM_GENE, object, getAnnotations(Gene.class, object));
		}
	}

	@Override
	protected Collection<Transcript> getExperimentalMarker() {
		return getNetwork().getTranscripts();
	}
}
