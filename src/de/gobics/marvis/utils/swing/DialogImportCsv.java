package de.gobics.marvis.utils.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import au.com.bytecode.opencsv.CSVReader;
import java.awt.event.ActionListener;

public class DialogImportCsv extends DialogCentered implements DocumentListener, ActionListener{

	private static final long serialVersionUID = 1L;

	protected static JFileChooser filechooser = new JFileChooser(new File("."));

	private static final Dimension defaultPreferredTextfieldDimension = new Dimension(50, (int) new JTextField().getSize().getHeight());
	protected File filename;
	protected JTable previewtable;
	protected final JTextField
			header_row = new JTextField("1"),
			first_data_row = new JTextField("2"),
			separator = new JTextField(",");
	protected final JButton button_ok = new JButton("Import"), button_cancel = new JButton("Cancel");
	protected JPanel optionPanel = new JPanel(new SpringLayout());
	protected int clickedButton = JOptionPane.CANCEL_OPTION;;

	public DialogImportCsv(Window parent, File filename) {
		super(parent, ModalityType.DOCUMENT_MODAL);
		this.setTitle("Import CSV");
		this.filename = filename;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel main = new JPanel(new BorderLayout());

		previewtable = new JTable(new CsvPreviewTableModel(this, filename));
		previewtable.setFillsViewportHeight(true);
		previewtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		previewtable.setPreferredScrollableViewportSize(new Dimension(500, 300));
		for (int i = 0; i < previewtable.getColumnCount(); i++) {
			previewtable.getColumnModel().getColumn(i).setMinWidth(100);
		}
		JScrollPane scrollTable = new JScrollPane(previewtable);
		main.add(scrollTable, BorderLayout.CENTER);
		main.setOpaque(true);

		optionPanel.add(new JLabel("Separator:"));
		optionPanel.add(this.separator);
		this.separator.setPreferredSize(defaultPreferredTextfieldDimension);
		this.separator.getDocument().addDocumentListener(this);

		optionPanel.add(new JLabel("Header line:"));
		optionPanel.add(this.header_row);
		this.header_row.setPreferredSize(defaultPreferredTextfieldDimension);

		optionPanel.add(new JLabel("First data line:"));
		optionPanel.add(this.first_data_row);
		this.first_data_row.setPreferredSize(defaultPreferredTextfieldDimension);

		SpringUtilities.makeCompactGrid(optionPanel);
		this.add(optionPanel, BorderLayout.LINE_END);

		this.pack();
	}

	/**
	 * Returns the row containing the header or -1 if no header is given.
	 * Counting will start with 1.
	 * @return
	 */
	public Integer getHeaderRow() {
		if (this.header_row.getText().isEmpty()) {
			return -1;
		}
		return new Integer(this.header_row.getText());
	}

	/**
	 * Returns the first row containing data. Row counting is started with 1.
	 * If no first data row is given -1 will be returned
	 * @return
	 */
	public Integer getFirstDataRow() {
		if (this.first_data_row.getText().isEmpty()) {
			return -1;
		}
		return new Integer(this.first_data_row.getText());
	}

	/**
	 * Returns the separator.
	 * @return
	 */
	public char getSeparator() {
		if (this.separator.getText().isEmpty()) {
			return ',';
		}
		return this.separator.getText().charAt(0);
	}

	/**
	 * Creates a CSVReader with the given options. Will also skip the first rows
	 * such that the next call to {@code CSVReader.readNext()} will return the
	 * first data row.
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public CSVReader getReader() throws FileNotFoundException, IOException {
		CSVReader reader = new CSVReader(new FileReader(filename), getSeparator());

		for( int i = 0; i < getFirstDataRow() ; i++)
			reader.readNext();

		return reader;
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
		CsvPreviewTableModel model = (CsvPreviewTableModel) previewtable.getModel();
		model.setSeparator( getSeparator() );
		for (int i = 0; i < previewtable.getColumnCount(); i++) {
			TableColumn c = previewtable.getColumnModel().getColumn(i);
			c.setMinWidth(100);
		}
		previewtable.updateUI();
	}

	public void actionPerformed(ActionEvent e) {
		if( e.getSource().equals(button_ok))
			clickedButton = JOptionPane.OK_OPTION;
		else if( e.getSource().equals(button_cancel) )
			clickedButton = JOptionPane.CANCEL_OPTION;
	}

	/**
	 * Displays the dialog and waits for the termination. Returns the button,
	 * the user has clicked.
	 * @return {@code JOptionPane.OK_OPTION} or {@code JOptionPane.CANCEL_OPTION}
	 */
	public int showDialog() {
		setVisible(true);
		return clickedButton;
	}
}

class CsvPreviewTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 4361866101510164324L;
	private static final int MAX_ROWS = 20;
	private final DialogImportCsv parent;
	private final File file;
	private char separator = ',';
	private String[][] data = null;

	public CsvPreviewTableModel(DialogImportCsv parent, File csvfile) {
		this.parent = parent;
		this.file = csvfile;
		readData();
	}

	public void setSeparator(char sep){
		this.separator = sep;
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

		//logger.debug("Returning value at: ["+row+"]["+column+"] length is: "+data[row].length);
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
		data = new String[MAX_ROWS][];
		try {
			CSVReader reader = new CSVReader(new FileReader(file), separator);
			String[] nextLine;
			int counter;
			for (counter = 0; counter < MAX_ROWS && ((nextLine = reader.readNext()) != null); counter++) {
				//logger.debug("Read line "+(counter+1)+" with "+nextLine.length+" data fields");
				data[counter] = nextLine.clone();
			}
		} catch (Exception ex) {
			new DialogError(this.parent, "Can not read csv file.", ex).setVisible(true);
			return;
		}
	}
}
