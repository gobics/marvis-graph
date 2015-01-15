package de.gobics.marvis.utils.swing.io;

import de.gobics.marvis.utils.io.TabularDataWriter;
import java.io.IOException;
import javax.swing.table.TableModel;

/**
 *
 * @author manuel
 */
public class TableModelWriter {

	private final TabularDataWriter writer;

	public TableModelWriter(TabularDataWriter writer) {
		this.writer = writer;
	}

	public void writeModel(TableModel model) throws IOException {
		writeModel(model, true);
	}

	public void writeModel(TableModel model, boolean write_header) throws IOException {
		int col_count = model.getColumnCount();
		if (write_header) {
			String[] header_names = new String[col_count];
			for (int col = 0; col < col_count; col++) {
				header_names[col] = model.getColumnName(col);
			}
			writer.appendRow(header_names);
		}

		for (int row = 0; row < model.getRowCount(); row++) {
			Object[] line = new Object[col_count];
			for (int col = 0; col < col_count; col++) {
				line[col] = model.getValueAt(row, col);
			}
			writer.appendRow(line);
		}
		writer.close();
	}
}
