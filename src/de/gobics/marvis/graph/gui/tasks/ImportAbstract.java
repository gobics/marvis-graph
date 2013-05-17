package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.InputObject;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.utils.io.TabularDataReader;
import de.gobics.marvis.utils.task.AbstractTask;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

public abstract class ImportAbstract extends AbstractTask<MetabolicNetwork, Void> {

	private static final Logger logger = Logger.getLogger(ImportAbstract.class.
			getName());
	private MetabolicNetwork network;
	private Integer id_column = 0;
	private Integer annotation_column = null;
	private Integer weight_column = null;
	private Integer start_row = 1;
	private int first_intensity_column = 0;
	private String[] condition_names = new String[0];
	private final TabularDataReader reader;

	public ImportAbstract(final MetabolicNetwork graph, TabularDataReader reader) {
		this.network = graph.clone();
		this.reader = reader;
		setTaskDescription("Importing metabolomic data");
		setTaskTitle("Importing metabolic marker candidates");
	}

	/**
	 * Counting starts with 1.
	 *
	 * @param id_column
	 */
	public void setIdColumn(int id_column) {
		if (id_column < 1) {
			throw new IllegalArgumentException("ID columns must be a integer greater than 0");
		}
		else {
			this.id_column = id_column - 1;
		}
	}

	/**
	 * Counting starts with 1.
	 *
	 * @param annotation_column
	 */
	public void setAnnotationColumn(int annotation_column) {
		if (annotation_column < 1) {
			this.annotation_column = null;
		}
		else {
			this.annotation_column = annotation_column - 1;
		}
	}
	
	public int getAnnotationColumnIndex(){
		return this.annotation_column;
	}

	/**
	 * Counting starts with 1.
	 *
	 * @param annotation_column
	 */
	public void setWeightColumn(int weight_column) {
		if (weight_column < 1) {
			this.weight_column = null;
		}
		else {
			this.weight_column = weight_column - 1;
		}
	}

	/**
	 * Counting starts with 1.
	 *
	 * @param start_row
	 */
	public void setFirstRow(int start_row) {
		this.start_row = Math.max(1, Math.abs(start_row));
	}

	/**
	 * Counting starts with 1.
	 *
	 * @param start_row
	 */
	public void setFirstIntensityColumn(int column) {
		this.first_intensity_column = column - 1;
	}

	public void setIntensityMapping(String[] condition_names) {
		this.condition_names = condition_names;
	}

	protected MetabolicNetwork getNetwork() {
		return network;
	}

	public MetabolicNetwork importData() throws IOException {
		setProgressMax(reader.getRowCount() - start_row);
		Iterator<Object[]> row_iter = reader.getRowIterator();

		// <= for convenience with Matlab MarVis
		for (int i = 1; i <= start_row && row_iter.hasNext(); i++) {
			row_iter.next();
		}

		logger.finer("Will now import marker data");
		int row_counter = start_row;
		while (row_iter.hasNext()) {
			row_counter++;
			Object[] data = row_iter.next();

			assertLength(row_counter, id_column, data);
			InputObject io = createObject(row_counter, data[id_column].toString(), data);

			// Parse the condition names
			if (condition_names.length > 0) {
				float[] intensity_data = new float[condition_names.length];
				for (int j = first_intensity_column; j < first_intensity_column + condition_names.length; j++) {
					intensity_data[j - first_intensity_column] = assertNumber(row_counter, j, data).floatValue();
				}
				io.setIntensity(condition_names, intensity_data);
			}

			if (annotation_column != null) {
				assertLength(row_counter, annotation_column, data);
				io.setAnnotation(data[annotation_column].toString());
			}
			
			// Parse the input weight
			if (weight_column != null) {
				io.setWeight(assertNumber(row_counter, weight_column, data).doubleValue());
			}

			incrementProgress();
			if (isCanceled()) {
				return null;
			}
		}

		logger.finer("Read marker data");
		return network;
	}

	@Override
	public MetabolicNetwork doTask() throws Exception {
		return importData();
	}

	protected Number assertNumber(int row, int column, Object[] data) throws IOException {
		assertLength(row, column, data);
		if (!(data[column] instanceof Number)) {
			throw new IOException("Cell " + column + " in row " + row + " is not a number:" + data[column]);
		}
		return (Number) data[column];
	}

	protected void assertLength(int row, int index, Object[] data) throws IOException {
		if (data.length < index) {
			throw new IOException("Row " + row + " is to short: expected at least " + (index + 1) + " cells but got " + data.length);
		}
	}

	protected abstract InputObject createObject(int row, String id, Object[] data) throws IOException;
}
