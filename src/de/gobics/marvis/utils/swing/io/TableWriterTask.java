package de.gobics.marvis.utils.swing.io;

import de.gobics.marvis.utils.swing.AbstractTask;
import javax.swing.table.TableModel;

/**
 *
 * @author manuel
 */
public class TableWriterTask extends AbstractTask<Boolean, Void> {

	public TableModelWriter writer;
	private final TableModel model;
	private final boolean write_header;

	public TableWriterTask(TableModelWriter writer, TableModel model, boolean write_header) {
		this.writer = writer;
		this.model = model;
		this.write_header = write_header;
	}

	@Override
	protected Boolean performTask() throws Exception {
		sendDescription("Write data");
		writer.writeModel(model, write_header);
		return true;
	}
}
