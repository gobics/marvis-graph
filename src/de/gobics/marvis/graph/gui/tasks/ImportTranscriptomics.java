package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.InputObject;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.utils.io.TabularDataReader;
import java.io.IOException;
import java.util.logging.Logger;

public class ImportTranscriptomics extends ImportAbstract {

	private static final Logger logger = Logger.getLogger(ImportTranscriptomics.class.
			getName());
	private MetabolicNetwork network;

	public ImportTranscriptomics(MetabolicNetwork graph, TabularDataReader reader) {
		super(graph, reader);
		setTaskTitle("Importing transcriptomic marker");
		setTaskDescription("Importing transcriptomic data");
	}

	@Override
	protected InputObject createObject(int row, String id, Object[] data) throws IOException {
		return getNetwork().createTranscript(id);
	}
}
