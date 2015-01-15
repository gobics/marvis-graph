package de.gobics.marvis.utils.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

/**
 * An abstract class to support the writing of tabular data to a file.
 *
 * @author manuel
 */
public abstract class TabularDataReader {

	private final File input_file;

	/**
	 * Create new writer for tabular data.
	 *
	 * @param input the file to write to
	 * @throws IOException if the parent directory does not exist and can not be
	 * created.
	 */
	public TabularDataReader(File input_file) throws IOException {
		if (!input_file.exists()) {
			throw new IOException("File does not exist: " + input_file.getAbsolutePath());
		}
		this.input_file = input_file;
	}

	/**
	 * Tries to find an appropriate reader for the given filename.
	 *
	 * @param filename
	 * @return a {@link TabularDataReader} to read the file.
	 * @throws IOException
	 */
	public static TabularDataReader getReader(File filename) throws IOException {
		if (filename.getName().toLowerCase().matches(".*\\.xlsx?")) {
			return new ExcelDataReader(filename);
		}
		return new CsvDataReader(filename);
	}

	/**
	 * Returns the file to write to.
	 *
	 * @return
	 */
	public File getInputFile() {
		return input_file;
	}

	/**
	 * Returns a new {@link Reader} to read the output file.
	 *
	 * @return the file writer.
	 * @throws IOException if the file can not be opened
	 */
	public Reader getReader() throws IOException {
		return new FileReader(getInputFile());
	}

	public abstract Iterator<Object[]> getRowIterator() throws IOException;

	public abstract int getRowCount() throws IOException;
}
