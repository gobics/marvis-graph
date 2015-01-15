package de.gobics.marvis.utils.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

/**
 * Abstract writer to write data in a CSV (comma-separated-values) style to a
 * compressed file. The GZip algorithm will be used for compression.
 *
 * @author manuel
 */
public class CompressedCsvDataWriter extends CsvDataWriter {

	public CompressedCsvDataWriter(File destination) throws IOException {
		super(destination);
	}

	public Writer getWriter() throws IOException {
		return new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(getOutputFile())));
	}
}
