/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.gui.tasks.ImportAbstract;
import de.gobics.marvis.utils.io.CsvDataReader;
import de.gobics.marvis.utils.io.ExcelDataReader;
import de.gobics.marvis.utils.io.TabularDataReader;
import de.gobics.marvis.utils.swing.SpringUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author manuel
 */
public abstract class DialogImport extends JPanel {

	private static final int PREVIEW_MAX_ROWS = 25;
	private final MarvisGraphMainWindow main_window;
	private final TabularDataReader reader;
	private final JTable previewtable;
	private final JPanel optionPanel = new JPanel(new SpringLayout());
// First line to parse
	private final NumberOption startline;
	// Default columns
	private final NumberOption column_id;
	private final NumberOption column_annotations;
	private final NumberOption intensity_firstcolumn;
	private final NumberOption intensity_lastcolumn;
	private final NumberOption column_score;
	// CSV Specific
	protected StringOption csv_separator = null;
	// Excel Specific
	protected NumberOption excel_sheet = null;

	protected DialogImport(MarvisGraphMainWindow parent, TabularDataReader input_reader) {
		super(new BorderLayout());
		this.reader = input_reader;
		this.main_window = parent;

		// Initialize the Table and basic model
		previewtable = new JTable();
		previewtable.setFillsViewportHeight(true);
		previewtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		previewtable.setPreferredScrollableViewportSize(new Dimension(500, 300));
		for (int i = 0; i < previewtable.getColumnCount(); i++) {
			previewtable.getColumnModel().getColumn(i).setMinWidth(100);
		}
		JScrollPane scrollTable = new JScrollPane(previewtable);
		add(scrollTable, BorderLayout.CENTER);

		add(optionPanel, BorderLayout.LINE_END);

		// Initialize the options


		if (reader instanceof CsvDataReader) {
			csv_separator = addStringOption("CSV Separator", true, ",");
			// ... more CSV options?
			csv_separator.addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent de) {
					if (csv_separator.getString().isEmpty()) {
						return;
					}
					((CsvDataReader) reader).setSeparator(csv_separator.getString().charAt(0));
					updatePreviewTable();
				}

				@Override
				public void removeUpdate(DocumentEvent de) {
					// ignore
				}

				@Override
				public void changedUpdate(DocumentEvent de) {
					if (csv_separator.getString().isEmpty()) {
						return;
					}
					((CsvDataReader) reader).setSeparator(csv_separator.getString().charAt(0));
					updatePreviewTable();
				}
			});

		}
		else if (reader instanceof ExcelDataReader) {
			excel_sheet = addNumberOption("Excel sheet", true, 1);
			excel_sheet.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					try {
						ExcelDataReader reader2 = (ExcelDataReader) reader;
						int sheet = excel_sheet.getNumber();
						if (sheet <= reader2.countSheets()) {
							reader2.setSheet(sheet - 1);
						}
						else {
							excel_sheet.setNumber(reader2.countSheets());
						}
						updatePreviewTable();
					}
					catch (IOException ex) {
						Logger.getLogger(DialogImport.class.getName()).log(Level.WARNING, null, ex);
					}


				}
			});
		}

		startline = addNumberOption("Header row", true, 1);
		column_id = addNumberOption("ID column", true, 1);
		column_annotations = addNumberOption("Annotation column", false, 2);
		intensity_firstcolumn = addNumberOption("First intensity column", false, 4);
		intensity_lastcolumn = addNumberOption("Last intensity column", false, 75);
		column_score = addNumberOption("Score column", false, 76);


		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updatePreviewTable();
			}
		});
	}

	/**
	 * Build the process to import data from this dialog.
	 *
	 * @param network
	 * @return
	 */
	public ImportAbstract getProcess(MetabolicNetwork network) {
		ImportAbstract process = createProcess(network);
		process.setIdColumn(getIdColumn());

		if (column_annotations.isSelected()) {
			process.setAnnotationColumn(getAnnotationColumn());
		}
		process.setScoreColumn(getScoreColum());
		process.setFirstRow(getFirstRow());

		if (!intensity_firstcolumn.isSelected() || !intensity_lastcolumn.isSelected()) {
			//System.out.println("First: "+intensity_firstcolumn.isSelected());
			//System.out.println("Second: "+intensity_lastcolumn.isSelected());
			return process;
		}

		// Input header names
		process.setFirstIntensityColumn(getIntensityFirstColumn());
		Iterator<Object[]> row_iter;
		try {
			row_iter = reader.getRowIterator();
		}
		catch (IOException ex) {
			Logger.getLogger(DialogImport.class.getName()).log(Level.SEVERE, "Can not get condition names: ", ex);
			main_window.display_error("Can not get condition names", ex);
			return null;
		}

		// Skip previous rows
		for (int i = 1; i < getFirstRow() && row_iter.hasNext(); i++) {
			row_iter.next();
		}

		Integer start = getIntensityFirstColumn();
		Integer end = getIntensityLastColumn();
		//System.out.println("Intensity start: " + start);
		//System.out.println("Intensity end: " + end);
		if (start != null && start >= 0 && end != null && end >= start) {
			Object[] data = row_iter.next();
			String[] header_names = new String[end - start + 1];
			for (int i = 0; i < header_names.length; i++) {
				header_names[i] = data[start + i - 1].toString();
			}

			//System.out.println("Display intensity mapper dialog for: " + Arrays.toString(header_names));
			DialogIntensityMapper im = new DialogIntensityMapper(network, header_names);
			if (!im.showDialog(main_window)) {
				return null;
			}
			header_names = im.getConditionMapping();
			process.setIntensityMapping(header_names);
		}

		return process;
	}

	/**
	 * Create the implementation specific import process.
	 *
	 * @param network
	 * @return
	 */
	protected abstract ImportAbstract createProcess(MetabolicNetwork network);

	/**
	 * Shows the dialog.
	 *
	 * @return true if dialog was closed with approval (process can be created)
	 */
	public boolean showDialog() {
		int res = JOptionPane.showConfirmDialog(main_window, this, "Import options", JOptionPane.OK_CANCEL_OPTION);
		return res == JOptionPane.OK_OPTION;
	}

	private Integer getScoreColum() {
		return column_score.getNumber();
	}

	private Integer getAnnotationColumn() {
		return column_annotations.getNumber();
	}

	public Integer getIdColumn() {
		return column_id.getNumber();
	}

	public Integer getFirstRow() {
		return startline.getNumber();
	}

	public Integer getIntensityFirstColumn() {
		return intensity_firstcolumn.getNumber();
	}

	public Integer getIntensityLastColumn() {
		return intensity_lastcolumn.getNumber();
	}

	private void updatePreviewTable() {
		Iterator<Object[]> iter;
		try {
			iter = getReader().getRowIterator();
		}
		catch (IOException ex) {
			main_window.display_error("Can not open input file", ex);
			return;
		}

		int col_counter = 0;
		int row_counter = 10;
		Object[][] data = new Object[PREVIEW_MAX_ROWS][];

		for (row_counter = 0; row_counter < PREVIEW_MAX_ROWS && iter.hasNext(); row_counter++) {
			Object[] row = iter.next();
			col_counter = Math.max(col_counter, row.length);
			data[row_counter] = row;
		}

		String[] columns = new String[col_counter];
		for (int i = 0; i < columns.length; i++) {
			columns[i] = Integer.toString(i + 1);
		}

		DefaultTableModel model = new DefaultTableModel(data, columns);
		previewtable.setModel(model);
		model.setColumnCount(col_counter);
		previewtable.updateUI();
	}

	protected TabularDataReader getReader() {
		return reader;
	}

	protected final NumberOption addNumberOption(String name, boolean required, int default_value) {
		return addNumberOption(name, required, new SpinnerNumberModel(default_value, 1, Integer.MAX_VALUE, 1));
	}

	protected final NumberOption addNumberOption(String name, boolean required, SpinnerNumberModel model) {
		optionPanel.add(new JLabel(name));
		final JSpinner spinner = new JSpinner(model);
		JCheckBox cb_active = null;
		if (!required) {
			cb_active = new JCheckBox("", false);
			cb_active.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					spinner.setEnabled(((JCheckBox) ae.getSource()).isSelected());
				}
			});

			spinner.setEnabled(cb_active.isSelected());
			optionPanel.add(cb_active);
		}
		else {
			optionPanel.add(new JLabel());
		}

		optionPanel.add(spinner);
		SpringUtilities.makeCompactGrid(optionPanel, 3);

		return new NumberOption(model, cb_active);
	}

	protected final StringOption addStringOption(String name, boolean required, String default_value) {
		optionPanel.add(new JLabel(name));
		final JTextField text = new JTextField(default_value);
		JCheckBox cb_active = null;

		if (!required) {
			cb_active = new JCheckBox("", false);
			cb_active.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					text.setEnabled(((JCheckBox) ae.getSource()).isSelected());
				}
			});
			text.setEnabled(cb_active.isSelected());

			optionPanel.add(cb_active);
		}
		else {
			optionPanel.add(new JLabel());
		}

		optionPanel.add(text);
		SpringUtilities.makeCompactGrid(optionPanel, 3);
		return new StringOption(text);
	}

	protected static class NumberOption {

		private final JCheckBox cb;
		private final SpinnerNumberModel model;

		public NumberOption(SpinnerNumberModel model) {
			this(model, null);
		}

		/**
		 * Create a new number option.
		 * 
		 * @param model the model for user input
		 * @param cb_active  might be null
		 */
		public NumberOption(SpinnerNumberModel model, JCheckBox cb_active) {
			this.model = model;
			this.cb = cb_active;
		}

		public boolean isSelected() {
			return cb == null || cb.isSelected();
		}

		public int getNumber() {
			if (isSelected()) {
				return model.getNumber().intValue();
			}
			return -1;
		}

		public void addChangeListener(ChangeListener cl) {
			model.addChangeListener(cl);
		}

		private void setNumber(int countSheets) {
			model.setValue(new Integer(countSheets));
		}
	}

	protected static class StringOption {

		private final JCheckBox cb;
		private final JTextField model;

		public StringOption(JTextField model) {
			this(model, null);
		}

		public StringOption(JTextField model, JCheckBox cb_active) {
			this.model = model;
			this.cb = cb_active;
		}

		public boolean isSelected() {
			return cb == null || cb.isSelected();
		}

		public String getString() {
			if (isSelected()) {
				return model.getText();
			}
			return null;
		}

		public void addDocumentListener(DocumentListener dl) {
			model.getDocument().addDocumentListener(dl);
		}
	}
}
