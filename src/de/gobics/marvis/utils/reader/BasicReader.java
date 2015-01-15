/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.reader;

import java.io.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author manuel
 */
public class BasicReader {

	private String separator;
	private Pattern separator_regex;
	private final BufferedReader reader;
	private StringBuilder sb = new StringBuilder();
	private boolean ready = false;

	public BasicReader(File file) throws FileNotFoundException, IOException {
		this(file, "\n");
	}

	public BasicReader(File file, String separator) throws FileNotFoundException, IOException {
		String filename_lower = file.getName().toLowerCase();

		if (filename_lower.endsWith(".gz") && !filename_lower.endsWith(".tar.gz")) {
			reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
		}
		else {
			reader = new BufferedReader(new FileReader(file));
		}
		setSeparator(separator);
	}

	public BasicReader(BufferedReader reader) {
		this(reader, "\n");
	}

	public BasicReader(BufferedReader reader, String separator) {
		this.reader = reader;
		setSeparator(separator);
	}

	protected void setSeparator(String separator) {
		this.separator = separator;
		this.separator_regex = Pattern.compile(separator);
	}

	public String nextChunk() throws IOException {
		if (ready) {
			return null;
		}

		// If newline split return the next line
		if (separator.equals("\n")) {
			return reader.readLine();
		}

		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");

			if (sb.toString().contains(separator)) {
				String tokens[] = sb.toString().split(separator);
				sb = new StringBuilder();
				for (int idx = 1; idx < tokens.length; idx++) {
					sb.append(tokens[idx]);
				}
				return tokens[0];
			}
		}

		ready = true;
		return !sb.toString().isEmpty() ? sb.toString() : null;
	}
}
