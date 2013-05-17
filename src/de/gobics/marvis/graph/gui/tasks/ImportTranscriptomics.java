package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.Gene;
import de.gobics.marvis.graph.InputObject;
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
	protected InputObject createObject(int row, String id, Object[] data) throws IOException {
		Transcript t = getNetwork().createTranscript(id);

		Integer idx = getAnnotationColumnIndex();
		if (idx != null && idx >= 0) {
			String gid = assertString(row, idx, data);
			System.out.print("Annotation string is: " + gid);
			if (gid != null) {
				Gene g = getNetwork().getGene(gid);
				System.out.print(" => " + gid.toString());
				if (g != null) {
					getNetwork().isFrom(t, g);
				}
			}
			System.out.println();
		}

		return t;
	}
}
