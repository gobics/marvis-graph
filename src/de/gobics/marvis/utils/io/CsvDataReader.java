package de.gobics.marvis.utils.io;

import au.com.bytecode.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class CsvDataReader extends TabularDataReader {

	private static final Logger logger = Logger.getLogger(CsvDataReader.class.getName());
	private CSVReader reader;
	private char separator = ',', quote = '"',
			escape = '\\';
	private int row_count = -1;

	public CsvDataReader(File input) throws IOException {
		super(input);
	}

	public char getSeparator() {
		return separator;
	}

	public void setSeparator(char separator) {
		this.separator = separator;
	}

	public char getQuote() {
		return quote;
	}

	public void setQuote(char quote) {
		this.quote = quote;
	}

	public char getEscape() {
		return escape;
	}

	public void setEscape(char escape) {
		this.escape = escape;
	}

	@Override
	public Iterator<Object[]> getRowIterator() throws IOException {
		return new RowIterator(new CSVReader(getReader(), separator, quote, escape));
	}

	@Override
	public int getRowCount() throws IOException {
		if (row_count < 0) {
			row_count = 0;
			BufferedReader r = new BufferedReader(getReader());
			while (r.readLine() != null) {
				row_count++;
			}
		}
		return row_count;
	}

	private static class RowIterator implements Iterator<Object[]> {

		private final CSVReader reader;
		private String[] buffer;

		public RowIterator(CSVReader reader) {
			this.reader = reader;
		}

		@Override
		public boolean hasNext() {
			if (buffer != null) {
				return true;
			}
			try {
				return (buffer = reader.readNext()) != null;
			}
			catch (IOException ex) {
				logger.log(Level.SEVERE, "Can not read next line: ", ex);
			}
			return false;
		}

		@Override
		public Object[] next() {
			Object[] data = new Object[buffer.length];
			for (int i = 0; i < data.length; i++) {
				String d_string = buffer[i];
				data[i] = d_string;

				try {
					double d_double = Double.parseDouble(d_string.replaceAll(",", "."));
					if (d_double == Math.floor(d_double)) {
						data[i] = (Integer) new Double(d_double).intValue();
					}
					data[i] = new Double(d_double);
				}
				catch (NumberFormatException e) {
					// ignore
				}
			}
			buffer = null;
			return data;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}
}
