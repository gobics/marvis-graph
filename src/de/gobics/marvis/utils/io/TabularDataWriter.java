package de.gobics.marvis.utils.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * An abstract class to support the writing of tabular data to a file.
 *
 * @author manuel
 */
public abstract class TabularDataWriter {

	private final File output_file;

	/**
	 * Create new writer for tabular data.
	 * @param outputfile the file to write to
	 * @throws IOException if the parent directory does not exist and can not be created.
	 */
	public TabularDataWriter(File outputfile) throws IOException {
		File parent = outputfile.getParentFile();
		if (!parent.exists()) {
			if (!parent.mkdirs()) {
				throw new IOException("Can not create directories: " + parent.getAbsolutePath());
			}
		}
		this.output_file = outputfile;
	}

	/**
	 * Returns the file to write to.
	 * @return 
	 */
	public File getOutputFile() {
		return output_file;
	}

	/**
	 * Returns a new {@link FileWriter} to (over-)write the output file.
	 * @return the file writer.
	 * @throws IOException if the file can not be opened
	 */
	public Writer getWriter() throws IOException {
		return new FileWriter(getOutputFile(), false);
	}
	
	public abstract void close() throws IOException;
	
	public abstract void appendRow(Object[] data) throws IOException;
}
