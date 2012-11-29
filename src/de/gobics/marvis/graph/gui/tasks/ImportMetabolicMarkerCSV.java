package de.gobics.marvis.graph.gui.tasks;

import au.com.bytecode.opencsv.CSVReader;
import de.gobics.marvis.graph.Marker;
import de.gobics.marvis.graph.MetabolicNetwork;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Process to import metabolic marker data from a CSV file into the graph. The
 * original graph will not be modified but an altered version will be returned.
 * If an error happens. The old graph is still valid.
 *
 * @author manuel
 */
public class ImportMetabolicMarkerCSV extends AbstractTask<MetabolicNetwork, Void> {

	private static final Logger logger = Logger.getLogger(ImportMetabolicMarkerCSV.class.
			getName());
	private MetabolicNetwork network;
	private File[] filenames = new File[0];
	private Integer id_column = 0, mass_column = 2, start_row = 1, rt_column = 1, weight_column = -1;
	private char separator = ',';
	private double correction_factor = 0.0;
	private int first_intensity_column = 0;
	private String[] condition_names = new String[0];
	private int id_counter = 1;

	public ImportMetabolicMarkerCSV(final MetabolicNetwork graph) {
		this.network = graph.clone();
	}

	public void setInputFiles(File[] datafiles) {
		this.filenames = datafiles;
	}

	public void setIdColumn(int id_column) {
		if (id_column < 1) {
			this.id_column = null;
		}
		else {
			this.id_column = id_column - 1;
		}
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

	public void setHeaderRow(int start_row) {
		this.start_row = start_row;
	}

	public void setSeparator(char c) {
		this.separator = c;
	}

	public void setFirstIntensityColumn(int column) {
		this.first_intensity_column = column - 1;
	}

	public void setIntensityMapping(String[] condition_names) {
		this.condition_names = condition_names;
	}

	public MetabolicNetwork importMarker() throws IOException {
		for (File current_file : filenames) {
			importMarker(current_file);
		}
		return network;
	}

	public MetabolicNetwork importMarker(File current_file) throws IOException {
		setProgressMax( getNumberOfLines(current_file) - start_row );
		
		sendDescription("Importing metabolomic data");
		CSVReader reader = new CSVReader(new FileReader(current_file), separator);

		// <= for convenience with Matlab MarVis
		for (int i = 1; i <= start_row; i++) {
			reader.readNext();
		}


		int minCol = (id_column == null || id_column < mass_column) ? mass_column : id_column;
		if (minCol < first_intensity_column + condition_names.length) {
			minCol = first_intensity_column + condition_names.length;
		}

		logger.finer("Will now import marker data");
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			String mid = Integer.toString(id_counter++);
			if (id_column != null) {
				mid = nextLine[id_column];
			}

			if (nextLine.length < minCol) {
				continue;
			}

			Marker marker = network.createMarker(mid);
			marker.setMass(new Double(nextLine[mass_column]).doubleValue() + correction_factor);
			if (rt_column >= 0) {
				marker.setRetentionTime(new Float(nextLine[rt_column]).
						floatValue());
			}
			if (weight_column != null) {
				marker.setWeight(new Double(nextLine[weight_column]).doubleValue());
			}

			if (first_intensity_column < 0) {
				continue;
			}

			// logger.finer("Importing intensity data for transcript: "+transcript_id);
			float[] intensity_data = new float[condition_names.length];
			for (int j = 0; j < condition_names.length; j++) {
				intensity_data[j] = new Float(nextLine[first_intensity_column + j].
						replace(',', '.'));
			}
			marker.setIntensity(condition_names, intensity_data);

			String[] additional_data = new String[nextLine.length - (condition_names.length + first_intensity_column)];
			System.arraycopy(nextLine, first_intensity_column + condition_names.length, additional_data, 0, additional_data.length);
			marker.setAdditionalData(additional_data);

			incrementProgress();

			if (isCancelled()) {
				return null;
			}
		}
		reader.close();

		logger.finer("Read marker data");

		return network;
	}

	public int getNumberOfLines(File current_file) throws IOException {
		FileReader fr = new FileReader(current_file);
		BufferedReader br = new BufferedReader(fr);
		int count = 0;
		while (br.readLine() != null) {
			count++;
		}
		return count;
	}

	@Override
	public MetabolicNetwork performTask() throws Exception {
		return importMarker();
	}

	public void setWeightColumn(Integer weightColumn) {
		if (weightColumn < 1) {
			weightColumn = null;
		}
		weight_column = weightColumn - 1;
	}
}
