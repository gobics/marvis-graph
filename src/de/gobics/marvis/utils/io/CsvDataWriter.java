package de.gobics.marvis.utils.io;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.IOException;

/**
 * Abstract writer to write data in a CSV (comma-separated-values) style to a
 * file.
 *
 * @author manuel
 */
public class CsvDataWriter extends TabularDataWriter {

	private char separator = ',';
	private char quoteCharacter = '"';
	private char escapeCharacter = '\\';
	private String lineEnd = "\n";
	private CSVWriter csvwriter = null;

	public CsvDataWriter(File destination) throws IOException {
		super(destination);
	}

	protected CSVWriter getCSVWriter() throws IOException {
		if (csvwriter == null) {
			csvwriter = new CSVWriter(getWriter(), separator, quoteCharacter, escapeCharacter, lineEnd);
		}
		return csvwriter;
	}

	public char getSeparator() {
		return separator;
	}

	public void setSeparator(char separator) {
		this.separator = separator;
	}

	public char getQuoteCharacter() {
		return quoteCharacter;
	}

	public void setQuoteCharacter(char quoteCharacter) {
		this.quoteCharacter = quoteCharacter;
	}

	public char getEscapeCharacter() {
		return escapeCharacter;
	}

	public void setEscapeCharacter(char escapeCharacter) {
		this.escapeCharacter = escapeCharacter;
	}

	public String getLineEnd() {
		return lineEnd;
	}

	public void setLineEnd(String lineEnd) {
		this.lineEnd = lineEnd;
	}

	/**
	 * Write a new line to the file.
	 *
	 * @param contents
	 */
	public void writeLine(Object[] contents) throws IOException {
		String[] strings = new String[contents.length];
		for (int idx = 0; idx < contents.length; idx++) {
			strings[idx] = contents[idx].toString();
		}
		writeLine(strings);
	}

	/**
	 * Write the new contents to the file.
	 *
	 * @param line_contents
	 * @throws IOException
	 */
	public void writeLine(String[] line_contents) throws IOException {
		getCSVWriter().writeNext(line_contents);
	}

	/**
	 * Append the given contents to the file. Each array will be handled as a
	 * new line.
	 *
	 * @param file_contents
	 * @throws IOException
	 */
	public void writeFile(String[][] file_contents) throws IOException {
		for (String[] line : file_contents) {
			writeLine(line);
		}
	}

	/**
	 * Append the given contents to the file. Each array will be handled as a
	 * new line.
	 *
	 * @param file_contents
	 * @throws IOException
	 */
	public void writeFile(Object[][] file_contents) throws IOException {
		for (Object[] line : file_contents) {
			writeLine(line);
		}
	}

	@Override
	public void close() throws IOException {
		getWriter().close();
	}

	@Override
	public void appendRow(Object[] data) throws IOException {
		writeLine(data);
	}
}
