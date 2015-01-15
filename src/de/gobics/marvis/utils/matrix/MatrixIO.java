
package de.gobics.marvis.utils.matrix;

import java.io.*;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Provide static methods to read and write matrices to the hard disk.
 * @author Manuel Landesfeind {@link mailto:manuel@gobics.de}
 */
public class MatrixIO {
	/**
	 * Uses the {@link java.util.logging} facilities for logging.
	 */
	private static final Logger logger = Logger.getLogger(MatrixIO.class.getName());
	
	/**
	 * Use tabs as default delimiter
	 */
	protected static final String delimiter = "\t";
	

	/** 
	 * Write the given matrix {@code matrix} into the file {@code out}. A 
	 * {@link FileOutputStream} will be used to write to the file.
	 * 
	 * See {@code MatrixIO.writeMatrixDense} for format information.
	 * 
	 * @param matrix The matrix to write
	 * @param out A file to store the matrix to
	 * @throws IOException Throws exceptions if the file can not be written
	 * @see MatrixIO.writeMatrixDense
	 */
	public static void writeMatrix(SparseDoubleMatrix2D matrix, File out) throws IOException {
		writeMatrixDense(matrix, new FileOutputStream(out));
	}

	/** 
	 * Write the given matrix {@code matrix} into the file {@code out}. But use
	 * a {@link GZIPOutputStream} to compress the data.
	 * 
	 * {@see writeMatrixDense()}
	 * 
	 * @param matrix The matrix to write
	 * @param out A file to store the matrix to
	 * @throws IOException Throws exceptions if the file can not be written
	 */
	
	public static void writeMatrixZipped(SparseDoubleMatrix2D matrix, File out) throws IOException {
		writeMatrixDense(matrix, new GZIPOutputStream(new FileOutputStream(out)));
	}

	/**
	 * Write the given matrix {@code matrix} into the output stream {@code outstream}.
	 * Format is as follows
	 * 
	 * The first line contains a hash-symbol then the number of rows a delimiter and the number of columns:
	 *  {@code #num_rows	num_cols}
	 * This allows the load method to quickly initialize the matrix with the needed rows and columns.
	 * 
	 * The second line starts with a delimiter and the the labels of the columns.
	 * All following rows have the row label as first "cell" and then the corresponding values.
	 * 
	 * This format allows users to open the written matrices in spreadsheet applications.
	 * 
	 * @param matrix The matrix to write to the stream
	 * @param outstream The output stream the matrix will be written to
	 * @throws IOException Throws exceptions of the output stream can not be written
	 */
	public static void writeMatrixDense(SparseDoubleMatrix2D matrix, OutputStream outstream) throws IOException {
		logger.finer("Saving matrix " + matrix);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outstream));
		writer.write("#" + matrix.rows() + delimiter + matrix.columns() + "\n");

		// Write Column names
		for (int col = 0; col < matrix.columns(); col++) {
			writer.write(delimiter + matrix.getColumnLabel(col));
		}
		writer.write("\n");

		for (int row = 0; row < matrix.rows(); row++) {
			writer.write("" + matrix.getRowLabel(row));

			for (int col = 0; col < matrix.columns(); col++) {
				writer.write(delimiter + matrix.getQuick(row, col));
			}
			writer.write("\n");
		}

		writer.close();
	}

	/** 
	 * Loads a matrix from the given {@code in} file
	 * @param in The file to load from
	 * @return A {@link SparseDoubleMatrix2D} containing the loaded matrix
	 * @throws IOException 
	 */
	public static SparseDoubleMatrix2D loadMatrix(File in) throws IOException {
		return loadMatrixDense(new FileInputStream(in));
	}

	/**
	 * Loads a matrix from a gzipped file.
	 * @param in The file to load from
	 * @return A {@link SparseDoubleMatrix2D} containing the loaded matrix
	 * @throws IOException on reading errors
	 */
	public static SparseDoubleMatrix2D loadMatrixZipped(File in) throws IOException {
		return loadMatrixDense(new GZIPInputStream(new FileInputStream(in)));
	}

	/**
	 * Loads a matrix from the given input stream. The format has to be equivalent to the
	 * format in the {@code writeMatrixDense()} method.
	 * 
	 * @param instream The stream to read from
	 * @return A {@link SparseDoubleMatrix2D} containing the loaded matrix.
	 * @throws IOException on reading errors
	 */
	public static SparseDoubleMatrix2D loadMatrixDense(InputStream instream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(instream));

		// Read the header line
		String[] tokens = in.readLine().substring(1).split(delimiter);
		int number_of_rows = new Integer(tokens[0]);
		int number_of_columns = new Integer(tokens[1]);

		SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(number_of_rows, number_of_columns);

		// Read the column labels
		tokens = in.readLine().substring(1).split(delimiter);
		matrix.setColumnLabels(tokens);

		// read the rest
		for (int row = 0; row < matrix.rows(); row++) {
			tokens = in.readLine().split(delimiter);
			matrix.setRowLabel(row, tokens[0]);
			for (int col = 0; col < matrix.columns(); col++) {
				matrix.setQuick(row, col, new Double(tokens[col + 1]));
			}
		}

		in.close();
		return matrix;
	}

	/**
	 * Write the given matrix in sparse format to the given {@link File}. A normal
	 * {@link FileOutputStream} will be used.
	 * 
	 * For format specification see {@code MatrixIO.writeMatrixSparse()}.
	 * 
	 * @param matrix The matrix to save
	 * @param out The file to write to
	 * @throws IOException An exception is thrown if the {@link FileOutputStream} can not be opened or written
	 */
	public static void writeMatrixSparse(SparseDoubleMatrix2D matrix, File out) throws IOException {
		writeMatrixSparse(matrix, new FileOutputStream(out));
	}
	
	
	
	/**
	 * Write the given matrix in sparse format to the given {@link File} but use
	 * a {@link GZIPOutputStream} to compress the data.
	 * 
	 * For format specification see {@code MatrixIO.writeMatrixSparse()}.
	 * 
	 * @param matrix The matrix to save
	 * @param out The file to write to
	 * @throws IOException An exception is thrown if the {@link FileOutputStream} can not be opened or written
	 */	
	
	public static void writeMatrixSparseZipped(SparseDoubleMatrix2D matrix, File out) throws IOException {
		writeMatrixSparse(matrix, new GZIPOutputStream(new FileOutputStream(out)));
	}

	/**
	 * Write the given matrix to the given output stream. The format differs from the
	 * {@code writeMatrixDense()} method because only non-zero values will be stored.
	 * 
	 * The first line written will contain a hash, the number of rows and columns like in the
	 * {@code writeMatrixDense()} method:
	 * 
	 *  {@code #num_rows	num_cols}
	 * 
	 * The line contains the row labels (delimited by the default delimiter) and
	 * the third line will contain the column labels.
	 * 
	 * All rows after that will be triples consisting of the row and column index
	 * and the corresponding variable:
	 * 
	 *  1	2	0.2
	 *  2	5	0.1
	 * 
	 * Using this file format a lot of hard disk space can be saved for very sparse
	 * matrices. Nevertheless, the files can not be easily opened in a spreadsheet
	 * application.
	 * 
	 * @param matrix The matrix to save
	 * @param outstream Output Stream to write to
	 * @throws IOException An exception will thrown in case of write errors
	 */
	public static void writeMatrixSparse(SparseDoubleMatrix2D matrix, OutputStream outstream) throws IOException {
		logger.finer("Saving sparse matrix " + matrix);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outstream));
		
		// Write header information with number of rows and columns
		writer.write("#" + matrix.rows() + delimiter + matrix.columns() + "\n");

		// Write row names
		for (int col = 0; col < matrix.rows(); col++) {
			writer.write(delimiter + matrix.getRowLabel(col));
		}
		writer.write("\n");
		// Write Column names
		for (int col = 0; col < matrix.columns(); col++) {
			writer.write(delimiter + matrix.getColumnLabel(col));
		}
		writer.write("\n");

		// Write the data
		for (int row = 0; row < matrix.rows(); row++) {
			for (int col = 0; col < matrix.columns(); col++) {
				double val = matrix.getQuick(row, col);
				if (val != 0) {
					writer.write(row + delimiter + col + delimiter + val + "\n");
				}
			}

			writer.close();
		}

	}

	/**
	 * Loads sparse Matrix from the given file. The format of the file
	 * has to match the specifications in the {@code writeMatrixSparse()} method.
	 * This is a wrapper for:
	 * 
	 * {@code loadMatrixSparse(new FileInputStream(in))}
	 *  
	 * @param in The file to read from
	 * @return A {@link SparseDoubleMatrix2D} that contains the loaded values.
	 * @throws IOException 
	 */
	public static SparseDoubleMatrix2D loadMatrixSparse(File in) throws IOException {
		return loadMatrixSparse(new FileInputStream(in));
	}

	/**
	 * Loads sparse Matrix from the given gzipped file. The format of the file
	 * has to match the specifications in the {@code writeMatrixSparse()} method.
	 * This is a wrapper for:
	 * 
	 * {@code loadMatrixSparse(new GZIPInputStream(new FileInputStream(in)))}
	 * 
	 * @param in
	 * @return
	 * @throws IOException 
	 */
	public static SparseDoubleMatrix2D loadMatrixSparseZipped(File in) throws IOException {
		return loadMatrixSparse(new GZIPInputStream(new FileInputStream(in)));
	}

	/**
	 * Loads a matrix from sparse file format into a {@link SparseDoubleMatrix2D}.
	 * The given {@link InputStream} has to follow the specifications from the
	 * {@code writeMatrixSparse()} method.
	 * 
	 * @param instream The stream to get the data from
	 * @return A {@link SparseDoubleMatrix2D} that contains the data
	 * @throws IOException 
	 */
	public static SparseDoubleMatrix2D loadMatrixSparse(InputStream instream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(instream));

		// Read the header line
		String[] tokens = in.readLine().substring(1).split(delimiter);
		int number_of_rows = new Integer(tokens[0]);
		int number_of_columns = new Integer(tokens[1]);

		SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(number_of_rows, number_of_columns);

		// Read the row labels
		tokens = in.readLine().substring(1).split(delimiter);
		matrix.setRowLabels(tokens);
		// Read the column labels
		tokens = in.readLine().substring(1).split(delimiter);
		matrix.setColumnLabels(tokens);

		// read the rest
		String line = null;
		while ((line = in.readLine()) != null) {
			tokens = line.split(delimiter);
			int row = new Integer(tokens[0]);
			int col = new Integer(tokens[1]);
			double val = new Double(tokens[2]);
			matrix.setQuick(row, col, val);
		}

		in.close();
		return matrix;

	}

	
	/**
	 * Writes the given matrix to the given file. The format differs from the usual
	 * formats because the resulting CSV file shall be read with the Matlab software.
	 * 
	 * Three files will be created:
	 * 1) The given file to contain the matrix in CSV format (with "," as separator).
	 * 2) The given file with a ".rows" suffix that contains the labels of the rows in every row
	 * 3) The given file with a ".columns" suffix that contains the labels of the columns in every row
	 * 
	 * @param tosave The matrix to save
	 * @param file The file to write to
	 * @throws IOException An exception will be thrown if the files can not be opened or written.
	 */
	public static void writeMatrixForMatlab(SparseDoubleMatrix2D tosave, File file) throws IOException {
		logger.finer("Saving matrix " + tosave + " to: " + file.getAbsolutePath());
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		for (int row = 0; row < tosave.rows(); row++) {
			writer.write("" + tosave.getQuick(row, 0));
			for (int col = 1; col < tosave.columns(); col++) {
				writer.write("," + tosave.getQuick(row, col));
			}
			writer.write("\n");
		}

		writer.close();

		// Write row labels
		writer = new BufferedWriter(new FileWriter(file.getAbsolutePath() + ".rows"));
		for (int row = 0; row < tosave.rows(); row++) {
			writer.write(tosave.getRowLabel(row).toString());
			writer.write("\n");
		}
		writer.close();

		// Write columns labels
		writer = new BufferedWriter(new FileWriter(file.getAbsolutePath() + ".columns"));
		for (int col_idx = 0; col_idx < tosave.columns(); col_idx++) {
			writer.write(tosave.getColumnLabel(col_idx).toString());
			writer.write("\n");
		}
		writer.close();

	}
	
	
	
	
}
