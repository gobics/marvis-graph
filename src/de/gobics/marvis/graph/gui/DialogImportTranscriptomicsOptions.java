package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.gui.tasks.ImportAbstract;
import de.gobics.marvis.graph.gui.tasks.ImportTranscriptomics;
import de.gobics.marvis.utils.io.TabularDataReader;
import javax.swing.JPanel;

public class DialogImportTranscriptomicsOptions extends DialogImport {

	public DialogImportTranscriptomicsOptions(MarvisGraphMainWindow parent, TabularDataReader reader) {
		super(parent, reader);
	}

	@Override
	protected ImportAbstract createProcess(MetabolicNetwork network) {
		return new ImportTranscriptomics(network, getReader());
	}
}