package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.gui.tasks.ImportAbstract;
import de.gobics.marvis.graph.gui.tasks.ImportMetabolicMarker;
import de.gobics.marvis.utils.io.TabularDataReader;

/**
 *
 * @author manuel
 */
public class DialogImportMetabolicsOptions extends DialogImport {

	private static int option_colum_mass = 3;
	private static int option_colum_rt = 2;
	private final NumberOption column_mass;
	private final NumberOption column_rt;

	public DialogImportMetabolicsOptions(MarvisGraphMainWindow parent, TabularDataReader reader) {
		super(parent, reader);

		column_rt = addNumberOption("Retention time column", false, option_colum_rt);
		column_mass = addNumberOption("Mass column", false, option_colum_mass);
	}

	@Override
	protected ImportAbstract createProcess(MetabolicNetwork network) {
		ImportMetabolicMarker process = new ImportMetabolicMarker(network, getReader());
		process.setMassColumn(column_mass.getNumber());
		option_colum_mass = column_mass.getNumber();
		process.setRetentiontimeColumn(column_rt.getNumber());
		option_colum_rt = column_rt.getNumber();
		return process;
	}
}