/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

import de.gobics.marvis.utils.io.CsvDataReader;
import de.gobics.marvis.utils.io.CsvDataWriter;
import de.gobics.marvis.utils.io.ExcelDataReader;
import de.gobics.marvis.utils.io.ExcelDataWriter;
import de.gobics.marvis.utils.io.TabularDataReader;
import de.gobics.marvis.utils.io.TabularDataWriter;
import de.gobics.marvis.utils.swing.io.TableModelWriter;
import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author manuel
 */
public class ChooserTabularData extends ChooserAbstract {

	private static final ChooserTabularData global_instance = new ChooserTabularData();

	public ChooserTabularData() {
		setAcceptAllFileFilterUsed(true);
		setFileFilter(new FileFilterExcelX());
		addChoosableFileFilter(new FileFilterExcel());
		addChoosableFileFilter(new FileFilterCsv());
	}

	public static ChooserTabularData getInstance() {
		return global_instance;
	}

	public TabularDataReader getDataReader() throws IOException {
		File f = getSelectedFileChecked();
		FileFilter ff = getFileFilter();

		if (ff != null && ff instanceof FileFilterCsv) {
			return new CsvDataReader(f);
		}
		return new ExcelDataReader(f);
	}

	public TabularDataWriter getDataWriter() throws IOException {
		File f = getSelectedFileChecked();
		FileFilter ff = getFileFilter();

		if (ff != null && ff instanceof FileFilterCsv) {
			return new CsvDataWriter(f);
		}
		return new ExcelDataWriter(f);
	}

	public TableModelWriter getTablemodelWriter() throws IOException {
		return new TableModelWriter(getDataWriter());
	}
}
