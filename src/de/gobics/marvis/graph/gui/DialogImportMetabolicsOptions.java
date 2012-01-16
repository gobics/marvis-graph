/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import au.com.bytecode.opencsv.CSVReader;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.gui.tasks.ImportMetabolicMarkerCSV;
import de.gobics.marvis.utils.swing.SpringUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author manuel
 */
public class DialogImportMetabolicsOptions extends DialogAbstract implements DocumentListener {

	private static final long serialVersionUID = 1L;
	protected File filename;
	protected JTable previewtable;
	protected JTextField column_id = new JTextField("1"),
			column_mass = new JTextField("3"),
			column_rt = new JTextField("2"),
			startline = new JTextField("1"), separator = new JTextField(","),
			marker_correction = new JTextField("0.0"),
			intensity_firstcolumn = new JTextField("4"),
			intensity_lastcolumn = new JTextField("76"),
			annotation_range = new JTextField("0.005");
	private final MarvisGraphMainWindow parent;

	public DialogImportMetabolicsOptions(MarvisGraphMainWindow parent, File filename) {
		super(parent, "Metabolic marker import options", ModalityType.DOCUMENT_MODAL);
		this.filename = filename;
		this.parent = parent;

		JPanel main = new JPanel(new BorderLayout());
		addOptions(main);

		// Initialize the Table and basic model
		previewtable = new JTable(new CsvPreviewTableModel(parent, filename));
		previewtable.setFillsViewportHeight(true);
		previewtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		previewtable.setPreferredScrollableViewportSize(new Dimension(500, 300));
		for (int i = 0; i < previewtable.getColumnCount(); i++) {
			previewtable.getColumnModel().getColumn(i).setMinWidth(100);
		}
		JScrollPane scrollTable = new JScrollPane(previewtable);
		main.add(scrollTable, BorderLayout.CENTER);
		main.setOpaque(true);

		JPanel optionPanel = new JPanel(new SpringLayout());
		optionPanel.add(new JLabel("ID column:"));
		optionPanel.add(this.column_id);
		this.column_id.setPreferredSize(defaultPreferredTextfieldDimension);
		this.column_id.selectAll();
		optionPanel.add(new JLabel("Retentiontime column:"));
		optionPanel.add(this.column_rt);
		this.column_rt.setPreferredSize(defaultPreferredTextfieldDimension);
		this.column_rt.selectAll();
		optionPanel.add(new JLabel("Mass column:"));
		optionPanel.add(this.column_mass);
		this.column_mass.setPreferredSize(defaultPreferredTextfieldDimension);
		this.column_mass.selectAll();
		optionPanel.add(new JLabel("Separator:"));
		optionPanel.add(this.separator);
		this.separator.setPreferredSize(defaultPreferredTextfieldDimension);
		this.separator.getDocument().addDocumentListener(this);
		optionPanel.add(new JLabel("Header line:"));
		optionPanel.add(this.startline);
		this.startline.setPreferredSize(defaultPreferredTextfieldDimension);
		optionPanel.add(new JLabel("Mass correction:"));
		optionPanel.add(this.marker_correction);
		this.marker_correction.setPreferredSize(defaultPreferredTextfieldDimension);
		optionPanel.add(new JLabel("Mass annotation range:"));
		optionPanel.add(this.annotation_range);
		this.annotation_range.setPreferredSize(defaultPreferredTextfieldDimension);
		optionPanel.add(new JLabel("First intensity column:"));
		optionPanel.add(this.intensity_firstcolumn);
		this.intensity_firstcolumn.setPreferredSize(defaultPreferredTextfieldDimension);
		optionPanel.add(new JLabel("Last intensity column:"));
		optionPanel.add(this.intensity_lastcolumn);
		this.intensity_lastcolumn.setPreferredSize(defaultPreferredTextfieldDimension);

		SpringUtilities.makeCompactGrid(optionPanel);

		main.add(optionPanel, BorderLayout.LINE_END);


		this.pack();
	}

	public ImportMetabolicMarkerCSV getProcess(final MetabolicNetwork network) {
		ImportMetabolicMarkerCSV process = new ImportMetabolicMarkerCSV(network);
		process.setIdColumn(getIdColumn());
		process.setMassColumn(getMassColumn());
		process.setCorrectionfactor(getCorrectionfactor());
		process.setHeaderRow(getHeaderRow());
		process.setSeparator(getSeparator());
		process.setFirstIntensityColumn(getIntensityFirstColumn());
		process.setRetentiontimeColumn(getRetentiontimeColumn());

		// Input header names
		int first_intensity_index = getIntensityFirstColumn() -1;
		if (first_intensity_index < 0) {
			return process;
		}

		String[] header_names = new String[getIntensityLastColumn() - getIntensityFirstColumn() + 1];

		CsvPreviewTableModel model = (CsvPreviewTableModel) previewtable.getModel();
		
		for (int i = 0; i < header_names.length; i++) {
			header_names[i] = (String) model.getValueAt(getHeaderRow() - 1, i + first_intensity_index);
		}

		DialogIntensityMapper im = new DialogIntensityMapper((MarvisGraphMainWindow) getParent(), network, header_names);
		im.setVisible(true);
		if (im.closedWithOk()) {
			header_names = im.getConditionMapping();
		}
		else {
			return null;
		}
		process.setIntensityMapping(header_names);

		return process;
	}

	private double getCorrectionfactor() {
		return new Double(this.marker_correction.getText().replace(",", ".")).
				doubleValue();
	}

	private double getMassrange() {
		return new Double(this.annotation_range.getText().replace(",", ".")).
				doubleValue();
	}

	public int getMassColumn() {
		return new Integer(this.column_mass.getText());
	}

	public int getRetentiontimeColumn() {
		if (this.column_rt.getText().isEmpty()) {
			return -1;
		}
		return new Integer(this.column_rt.getText());
	}

	public Integer getIdColumn() {
		if (this.column_id.getText() == null || this.column_id.getText().isEmpty()) {
			return -1;
		}
		return new Integer(this.column_id.getText());
	}

	public Integer getHeaderRow() {
		if (this.startline.getText().isEmpty()) {
			return 1;
		}
		return new Integer(this.startline.getText());
	}

	public char getSeparator() {
		if (this.separator.getText().isEmpty()) {
			return ',';
		}
		return this.separator.getText().charAt(0);
	}

	public Integer getIntensityFirstColumn() {
		if (this.intensity_firstcolumn.getText().isEmpty()) {
			return -1;
		}
		return new Integer(this.intensity_firstcolumn.getText());
	}

	public Integer getIntensityLastColumn() {
		if (this.intensity_lastcolumn.getText().isEmpty()) {
			return -1;
		}
		return new Integer(this.intensity_lastcolumn.getText());
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		if (arg0.getDocument().equals(separator.getDocument())) {
			updatePreviewTable();
		}
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		if (arg0.getDocument().equals(separator.getDocument())) {
			updatePreviewTable();
		}
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		if (arg0.getDocument().equals(separator.getDocument())) {
			updatePreviewTable();
		}
	}

	private void updatePreviewTable() {
		previewtable.setModel(new CsvPreviewTableModel(this.parent, filename, getSeparator()));
		for (int i = 0; i < previewtable.getColumnCount(); i++) {
			TableColumn c = previewtable.getColumnModel().getColumn(i);
			c.setMinWidth(100);
		}
		previewtable.updateUI();
	}
}

class CsvPreviewTableModel extends AbstractTableModel {

	private static final Logger logger = Logger.getLogger(CsvPreviewTableModel.class.
			getName());
	private static final long serialVersionUID = 4361866101510164324L;
	private static final int MAX_ROWS = 20;
	private MarvisGraphMainWindow parent;
	private File filename;
	private String[][] data = null;
	private char separator = ',';

	public CsvPreviewTableModel(MarvisGraphMainWindow parent, File filename) {
		this.parent = parent;
		this.filename = filename;
		readData();
	}

	public CsvPreviewTableModel(MarvisGraphMainWindow parent, File filename, char separator) {
		this.parent = parent;
		this.filename = filename;
		this.separator = separator;
		readData();
	}

	public void setSeparator(char separator) {
		this.separator = separator;
		//logger.finer("Setting separator to: " + this.separator);
		readData();
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
			return "...";
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

	private void readData() {
		logger.finer("Reading data");
		data = new String[MAX_ROWS][];

		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(filename), separator);
			String[] nextLine;
			int counter;
			for (counter = 0; counter < MAX_ROWS && ((nextLine = reader.readNext()) != null); counter++) {
				//logger.finer("Read line "+(counter+1)+" with "+nextLine.length+" data fields");
				data[counter] = nextLine.clone();
			}
		}
		catch (Exception e) {
			parent.showErrorBox("Can not load data from file", e);
		}
	}
}
