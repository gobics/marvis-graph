package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.utils.swing.SpringUtilities;
import de.gobics.marvis.utils.swing.ProcessListenerDialog;
import java.awt.*;
import java.io.*;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;
import jxl.*;
import jxl.read.biff.*;
import de.gobics.marvis.graph.gui.tasks.ImportTranscriptomicsExcel;

import java.util.logging.Logger;

public class DialogImportTranscriptomicsOptions extends DialogAbstract {

	private static final Logger logger = Logger.getLogger(DialogImportTranscriptomicsOptions.class.
			getName());
	private static final long serialVersionUID = 1L;
	private final File excelfile;
	private JTextField id_column = new JTextField("1"),
			gene_id_column = new JTextField("2"),
			start_row = new JTextField("1");
	protected JTable previewTable;
	protected JTextField intensity_firstcolumn = new JTextField("3"),
			intensity_lastcolumn = new JTextField("9"),
			intensity_noOfSamplesPerCondition = new JTextField("3");

	public DialogImportTranscriptomicsOptions(final MarvisGraphMainWindow parent, final File excelfile) throws BiffException, IOException {
		super(parent, "Import genes from Excel file", ModalityType.APPLICATION_MODAL);
		this.excelfile = excelfile;

		JPanel main = new JPanel(new BorderLayout());
		this.add(main);

		previewTable = new JTable(new ExcelTableModel(excelfile));
		previewTable.setFillsViewportHeight(true);
		previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		previewTable.setPreferredScrollableViewportSize(new Dimension(500, 200));

		for (int i = 0; i < previewTable.getColumnCount(); i++) {
			previewTable.getColumnModel().getColumn(i).setMinWidth(100);
		}


		JScrollPane scrollTable = new JScrollPane(previewTable);
		main.add(scrollTable, BorderLayout.CENTER);
		main.setOpaque(true);

		JPanel optionPanel = new JPanel(new SpringLayout());
		optionPanel.add(new JLabel("ID column:"));
		optionPanel.add(id_column);
		optionPanel.add(new JLabel("Gene-ID column:"));
		optionPanel.add(gene_id_column);
		optionPanel.add(new JLabel("Header line:"));
		optionPanel.add(start_row);

		optionPanel.add(new JLabel("First intensity column):"));
		optionPanel.add(this.intensity_firstcolumn);
		this.intensity_firstcolumn.setPreferredSize(defaultPreferredTextfieldDimension);
		optionPanel.add(new JLabel("Last intensity column:"));
		optionPanel.add(this.intensity_lastcolumn);
		this.intensity_lastcolumn.setPreferredSize(defaultPreferredTextfieldDimension);

		SpringUtilities.makeCompactGrid(optionPanel);
		main.add(optionPanel, BorderLayout.LINE_END);

		this.pack();
	}

	public int getIdColumn() {
		if (id_column.getText() == null || id_column.getText().isEmpty()) {
			return -1;
		}
		return new Integer(this.id_column.getText()).intValue();
	}

	public int getGeneIdColumn() {
		return new Integer(this.gene_id_column.getText()).intValue();
	}

	public int getHeaderRow() {
		return new Integer(this.start_row.getText()).intValue();
	}

	public int getIntensityFirstColumn() {
		if (this.intensity_firstcolumn.getText().isEmpty()) {
			return -1;
		}
		return new Integer(this.intensity_firstcolumn.getText());
	}

	public int getIntensityLastColumn() {
		if (this.intensity_lastcolumn.getText().isEmpty()) {
			return -1;
		}
		return new Integer(this.intensity_lastcolumn.getText());
	}

	public ImportTranscriptomicsExcel getProcess(final MetabolicNetwork network) {
		logger.finer("Creating background task to read transcriptomic data");
		ImportTranscriptomicsExcel process = new ImportTranscriptomicsExcel(network, excelfile);
		process.setIdColumn(getIdColumn());
		process.setGeneIdColumn(getGeneIdColumn());
		process.setStartRow(getHeaderRow());
		process.setFirstIntensityColumn(getIntensityFirstColumn());

		// Intensity-Wizard
		ExcelTableModel model = (ExcelTableModel) previewTable.getModel();
		// Input header names
		String[] header_names = new String[getIntensityLastColumn() - getIntensityFirstColumn() + 1];

		for (int i = 0; i < header_names.length; i++) {
			header_names[i] = (String) model.getValueAt(getHeaderRow() - 1, i - 1 + getIntensityFirstColumn());
		}

		DialogIntensityMapper im = new DialogIntensityMapper((MarvisGraphMainWindow) getParent(), network, header_names);
		im.setVisible(true);

		if (!im.closedWithOk()) {
			return null;
		}
		process.setIntensityMapping(im.getConditionMapping());


		return process;
	}
}

class ExcelTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private static final int MAX_ROWS = 20;
	private String[][] data;
	private File datafile;

	public ExcelTableModel(File excel) throws BiffException, IOException {
		datafile = excel;
		readData();
	}

	private void readData() throws IndexOutOfBoundsException, BiffException, IOException {
		Workbook wb = Workbook.getWorkbook(datafile);
		Sheet sheet = wb.getSheet(0);
		if (sheet.getRows() > MAX_ROWS) {
			data = new String[MAX_ROWS][];
		}
		else {
			data = new String[sheet.getRows()][];
		}

		for (int row = 0; row < data.length; row++) {
			String[] column_data = new String[sheet.getRow(row).length];
			for (int column = 0; column < column_data.length; column++) {
				column_data[column] = sheet.getCell(column, row).getContents();
			}
			data[row] = column_data;
		}
		wb.close();
	}

	@Override
	public int getColumnCount() {
		int columns = 0;
		for (int i = 0; i < data.length; i++) {
			if (data[i].length > columns) {
				columns = data[i].length;
			}
		}
		return columns;
	}

	@Override
	public int getRowCount() {
		return data.length + 1;
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (row >= MAX_ROWS) {
			return new String("...");
		}

		if (data.length - 1 < row) {
			//logger.warn("Data from row "+row+" is wanted but length of data is: "+data.length);
			return null;
		}

		if (data[row] == null) {
			//logger.warn("Data from row "+row+" is wanted but is NULL");
			return null;
		}

		if (data[row].length - 1 < column) {
			//logger.warn("Data from column "+column+" is wanted but length of data["+row+"] is: "+data[row].length);
			return null;
		}

		//logger.finer("Returning value at: ["+row+"]["+column+"] length is: "+data[row].length);
		return data[row][column];
	}

	@Override
	public String getColumnName(int col) {
		return Integer.toString(col + 1);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}
}
