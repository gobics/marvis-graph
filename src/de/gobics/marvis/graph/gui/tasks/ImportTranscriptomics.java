package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.Gene;
import de.gobics.marvis.graph.ExperimentalMarker;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Transcript;
import de.gobics.marvis.utils.io.TabularDataReader;
import java.io.IOException;

public class ImportTranscriptomics extends ImportAbstract {

	public ImportTranscriptomics(MetabolicNetwork graph, TabularDataReader reader) {
		super(graph, reader);
		setTaskTitle("Importing transcriptomic marker");
		setTaskDescription("Importing transcriptomic data");
	}

	@Override
	protected ExperimentalMarker createObject(int row, String id, Object[] data) throws IOException {
		return getNetwork().createTranscript(id);
	}
}
