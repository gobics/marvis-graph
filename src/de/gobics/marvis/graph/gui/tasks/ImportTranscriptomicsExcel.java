package de.gobics.marvis.graph.gui.tasks;
import java.io.*;
import jxl.*;
import de.gobics.marvis.graph.*;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

public class ImportTranscriptomicsExcel extends SwingWorker<MetabolicNetwork, Void> {

	private static final Logger logger = Logger.getLogger(ImportTranscriptomicsExcel.class.
			getName());
	private File transcriptomicsFile;
	private MetabolicNetwork network;
	private int start_row, id_column, gene_id_column;
	private String[] condition_names = new String[0];
	private int first_intensity_column = 2;

	public ImportTranscriptomicsExcel(MetabolicNetwork graph, File transcriptomicsFile) {
		this.transcriptomicsFile = transcriptomicsFile;
		this.network = graph.clone();
		this.setIdColumn(1);
		this.setGeneIdColumn(2);
		this.setStartRow(1);
	}

	public void setStartRow(int r) {
		this.start_row = r;
	}

	public void setIdColumn(int c) {
		this.id_column = c - 1;
	}

	public void setGeneIdColumn(int c) {
		this.gene_id_column = c - 1;
	}

	public void setFirstIntensityColumn(int column) {
		this.first_intensity_column = column - 1;
	}

	public void setIntensityMapping(String[] condition_names) {
		this.condition_names = condition_names;
	}

	public MetabolicNetwork importTranscripts() throws Exception {
		Workbook workbook = Workbook.getWorkbook(this.transcriptomicsFile);

		logger.finer("Try to open the excel file");
		Sheet sheet = workbook.getSheet(0);
		int current = 1;
		int max = sheet.getRows();

		logger.finer("Preparing intensity configuration");
		int minCol = id_column;
		if (minCol < first_intensity_column + condition_names.length) {
			minCol = first_intensity_column + condition_names.length;
		}


		logger.finer("Importing data");
		getPropertyChangeSupport().firePropertyChange("description", null, "Importing transcriptomic data");
		for (int row = this.start_row; row < sheet.getRows(); row++) {
			String transcript_id = "t" + (row - start_row);
			if (id_column >= 0) {
				sheet.getCell(this.id_column, row).getContents();
			}
			String gene_id = sheet.getCell(this.gene_id_column, row).getContents();

			if (gene_id.equals("NA")) {
				continue;
			}

			Transcript transcript = this.network.createTranscript(transcript_id);

			Gene gene = network.getGene(gene_id);
			if (gene != null) {
				network.isFrom(transcript, gene);
			} else {
				logger.warning("Can not find gene with id: '"+gene_id+"'");
			}

			// logger.finer("Importing intensity data for transcript: "+transcript_id);
			float[] intensity_data = new float[condition_names.length];
			for (int j = 0; j < condition_names.length; j++) {
				Cell cell = sheet.getCell(first_intensity_column + j, row);
				// Check if this is a number
				if (cell.getContents().isEmpty()) {
					intensity_data[j] = 0f;
				}
				else if (cell.getType().equals(CellType.NUMBER)) {
					intensity_data[j] = (float) ((NumberCell) cell).getValue();
				}
				else {
					intensity_data[j] = new Float(cell.getContents().replace(',', '.'));
				}
			}
			transcript.setIntensity(condition_names, intensity_data);

			setProgress(row / max);
			if (isCancelled()) {
				return null;
			}

		}
		logger.finer("Imported " + (current - 1) + " transcripts. Closing excel file");
		workbook.close();

		return network;
	}

	@Override
	protected MetabolicNetwork doInBackground() throws Exception {
		return importTranscripts();
	}
}
