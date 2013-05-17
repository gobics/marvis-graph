package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.InputObject;
import de.gobics.marvis.graph.Marker;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.utils.io.TabularDataReader;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Process to import metabolic marker data from a CSV file into the graph. The
 * original graph will not be modified but an altered version will be returned.
 * If an error happens. The old graph is still valid.
 *
 * @author manuel
 */
public class ImportMetabolicMarker extends ImportAbstract {

	private static final Logger logger = Logger.getLogger(ImportMetabolicMarker.class.
			getName());
	private Integer mass_column = 2, rt_column = 1;
	private double correction_factor = 0.0;

	public ImportMetabolicMarker(final MetabolicNetwork graph, TabularDataReader reader) {
		super(graph, reader);
		setTaskDescription("Importing metabolomic data");
		setTaskTitle("Importing metabolic marker candidates");
	}

	public void setMassColumn(int mass_column) {
		if (mass_column < 1) {
			this.mass_column = null;
		}
		else {
			this.mass_column = mass_column - 1;
		}
	}

	public void setRetentiontimeColumn(int rt_column) {
		if (rt_column < 0) {
			this.rt_column = null;
		}
		else {
			this.rt_column = rt_column - 1;
		}
	}

	public void setCorrectionfactor(double factor) {
		this.correction_factor = factor;
	}

	@Override
	protected InputObject createObject(int row, String id, Object[] data) throws IOException{
		Marker m = getNetwork().createMarker(id);
		
		if( mass_column >= 0 ){
			m.setMass( assertNumber(row, mass_column, data).doubleValue() + correction_factor );
		}
		if(rt_column >= 0){
			m.setRetentionTime(assertNumber(row, rt_column, data).floatValue());
		}
		
		return m;
	}
}
