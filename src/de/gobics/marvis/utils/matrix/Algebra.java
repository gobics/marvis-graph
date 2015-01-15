package de.gobics.marvis.utils.matrix;

import cern.colt.function.IntIntDoubleFunction;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * This package provides algebraic calculations that are not available in the
 * {@link cern.colt.matrix.linalg.Algebra} but need for the pathway profiling.
 *
 * @author Manuel Landesfeind <manuel@gobics.de>
 */
public class Algebra extends cern.colt.matrix.linalg.Algebra {

	private static Algebra instance = new Algebra();

	public static Algebra getInstance() {
		return instance;
	}

	/**
	 * Performs a dot-multiplication. That is:
	 *
	 * result[i,j] = first[i,j] * second[i,j]
	 *
	 * @param first
	 * @param second
	 * @param result
	 * @return the same matrix as given via the {@code result} parameter
	 */
	public DoubleMatrix2D dotMult(DoubleMatrix2D first, DoubleMatrix2D second, DoubleMatrix2D result) {
		if (first.rows() != second.rows()) {
			throw new RuntimeException("First and second matrix have unequal number of rows");
		}
		if (first.rows() != result.rows()) {
			throw new RuntimeException("Input matrices and result matrix have unequal number of rows");
		}
		if (first.columns() != second.columns()) {
			throw new RuntimeException("First and second matrix have unequal number of columns");
		}
		if (first.columns() != result.columns()) {
			throw new RuntimeException("Input matrices and result matrix have unequal number of columns");
		}

		for (int row = 0; row < first.rows(); row++) {
			for (int col = 0; col < first.columns(); col++) {
				result.setQuick(row, col, first.getQuick(row, col) / second.
						getQuick(row, col));
			}
		}


		return result;
	}

	/**
	 * Performs a multiplication of a 2D matrix with a scalar factor. The result
	 * is stored in the third parameter.
	 *
	 * {@core result[i,j] = matrix[i,j] * factor}
	 *
	 * This matrix is also returned. The operant matrix and the result matrix
	 * need to have the same dimension.
	 *
	 * @param matrix The matrix operant
	 * @param scalar the scalar operant
	 * @param result a matrix to store the result into
	 * @return The same object as the {@code result} matrix parameter
	 */
	public DoubleMatrix2D mult(DoubleMatrix2D matrix, double scalar, DoubleMatrix2D result) {
		if (result == null) {
			result = new DenseDoubleMatrix2D(matrix.rows(), matrix.columns());
		}
		if (matrix.rows() != result.rows()) {
			throw new RuntimeException("Source and result matrix have unequal number of rows");
		}
		if (matrix.columns() != result.columns()) {
			throw new RuntimeException("Source and result matrix have unequal number of columns");
		}
		for (int row_idx = 0; row_idx < matrix.rows(); row_idx++) {
			for (int col_idx = 0; col_idx < matrix.columns(); col_idx++) {
				result.setQuick(row_idx, col_idx, matrix.getQuick(row_idx, col_idx) * scalar);
			}
		}

		return result;
	}

	/**
	 * Reverse operation of the {@code mult} operation. Every element in the
	 * operant matrix is divided by the scalar
	 *
	 * @param operant Matrix to get divided
	 * @param scalar the scalar to divide by
	 * @param result a matrix containing the result
	 * @return the {@code result} object
	 */
	public DoubleMatrix2D divide(DoubleMatrix2D operant, double scalar, DoubleMatrix2D result) {
		return mult(operant, 1d / scalar, result);
	}

	/**
	 * Performs the multiplication of every element in the operant vector with
	 * the scalar and stores it into the result matrix. The operant and result
	 * vector need to have the same length.
	 *
	 * @param operant Operant vector with values to multiply by the scalar
	 * @param factor Scalar value to multiply each element in the vector with
	 * @param result A vector to store the results
	 * @return the {@code result} object
	 */
	public DoubleMatrix1D mult(DoubleMatrix1D operant, double factor, DoubleMatrix1D result) {
		if (result == null) {
			result = new DenseDoubleMatrix1D(operant.size());
		}
		if (operant.size() != result.size()) {
			throw new RuntimeException("Source and result vector have unequal number of entries");
		}

		for (int idx = 0; idx < operant.size(); idx++) {
			result.setQuick(idx, operant.getQuick(idx) * factor);
		}

		return result;
	}

	/**
	 * Performs the scalar-by-vector division: result[i] = factor / vector[i];
	 *
	 * @param factor Scalar value to divide each element in the vector with
	 * @param operant Operant vector with values to divide by the scalar
	 * @param result A vector to store the results
	 * @return the {@code result} vector
	 */
	public DoubleMatrix1D divide(double factor, DoubleMatrix1D vector, DoubleMatrix1D result) {
		if (vector.size() != result.size()) {
			throw new IllegalArgumentException("Operant and result have different size: " + vector.
					size() + " != " + result.size());
		}
		for (int i = 0; i < vector.size(); i++) {
			if (vector.getQuick(i) != 0) {
				result.setQuick(i, factor / vector.getQuick(i));
			}
		}
		return result;
	}

	/**
	 * Reverse operation to the vector-scalar-multiplication.
	 *
	 * @param operant Operant vector with values to divide by the scalar
	 * @param factor Scalar value to divide each element in the vector with
	 * @param result A vector to store the results
	 * @return the {@code result} vector
	 */
	public DoubleMatrix1D divide(DoubleMatrix1D vector, double factor, DoubleMatrix1D result) {
		return mult(vector, 1d / factor, result);
	}

	/**
	 * Calculate the logarithm for each element in the operant matrix and store
	 * it in the result matrix. The logarithm is calculated using the {@code Math.log()}
	 * method.
	 *
	 * @param operant Contain element to calculate the log of
	 * @param result Matrix to store the result into
	 * @return the {@code result} matrix
	 */
	public DoubleMatrix2D log(DoubleMatrix2D operant, DoubleMatrix2D result) {
		if (result == null) {
			result = operant.copy();
		}
		for (int row = 0; row < operant.rows(); row++) {
			for (int col = 0; col < operant.columns(); col++) {
				result.setQuick(row, col, Math.log(operant.getQuick(row, col)));
			}
		}
		return result;
	}

	/**
	 * Calculate the logarithm for each element in the operant vector and store
	 * it in the result vector. The logarithm is calculated using the {@code Math.log()}
	 * method.
	 *
	 * @param operant Contain element to calculate the log of
	 * @param result Vector to store the result into
	 * @return the {@code result} vector
	 */
	public DoubleMatrix1D log(DoubleMatrix1D vector, DoubleMatrix1D result) {
		if (result == null) {
			result = vector.copy();
		}
		for (int idx = 0; idx < vector.size(); idx++) {
			result.setQuick(idx, Math.log(vector.getQuick(idx)));
		}
		return result;
	}

	/**
	 * Returns a matrix of the same size as the operant. The result matrix will
	 * contain a 1 in every cell if the corresponding cell in the operant matrix
	 * is not zero:
	 *
	 * {@code result[i,j] = operant[i,j] != 0 ? 1 : 0}
	 *
	 * @param operant The matrix to find the non-zero values
	 * @param result matrix to store the results into. Must have the same size
	 * as the operant matrix
	 * @return returns the result matrix object
	 */
	public DoubleMatrix2D nonzero(DoubleMatrix2D operant, DoubleMatrix2D result) {
		for (int row = 0; row < operant.rows(); row++) {
			for (int col = 0; col < operant.rows(); col++) {
				result.setQuick(row, col, operant.getQuick(row, col) != 0 ? 1 : 0);
			}
		}
		return result;
	}

	/**
	 * Returns a vector of the same size as the operant. The result vector will
	 * contain a 1 in every cell if the corresponding cell in the operant vector
	 * is not zero:
	 *
	 * {@code result[i] = operant[i] != 0 ? 1 : 0}
	 *
	 * @param operant The vector to find the non-zero values
	 * @param result vector to store the results into. Must have the same size
	 * as the operant vector
	 * @return returns the result vector object
	 */
	public DoubleMatrix1D nonzero(DoubleMatrix1D operant, DoubleMatrix1D result) {
		for (int idx = 0; idx < operant.size(); idx++) {
			result.setQuick(idx, operant.getQuick(idx) != 0 ? 1 : 0);
		}
		return result;
	}

	/**
	 * Multiply every column in the matrix dot-wise with the corresponding value
	 * in the vector operant:
	 *
	 * {@code result[i,j] = matrix_operant[i,j] * vector_operant[i]}
	 *
	 * @param matrix_operant The matrix operant
	 * @param operant The vector with the same length as matrix rows
	 * @param result a matrix to store the results into
	 * @return returns the result parameter object
	 */
	public DoubleMatrix2D dotMult(DoubleMatrix2D matrix_operant, DoubleMatrix1D vector_operant, DoubleMatrix2D result) {
		for (int row = 0; row < matrix_operant.rows(); row++) {
			for (int col = 0; col < matrix_operant.rows(); col++) {
				result.setQuick(row, col, matrix_operant.getQuick(row, col) * vector_operant.
						getQuick(row));
			}
		}

		return result;
	}

	public DoubleMatrix1D dotMult(DoubleMatrix1D vector1, DoubleMatrix1D vector2, DoubleMatrix1D result) {
		if (vector1.size() != vector2.size()) {
			throw new IllegalArgumentException("Both operants need to have the same length");
		}
		if (vector1.size() != result.size()) {
			throw new IllegalArgumentException("Result needs to have the same length as the operants");
		}

		for (int idx = 0; idx < vector1.size(); idx++) {
			result.setQuick(idx, vector1.getQuick(idx) * vector2.getQuick(idx));
		}
		return result;
	}

	public DoubleMatrix2D normEukledian(DoubleMatrix2D operant, DoubleMatrix2D result) {
		if (operant.rows() != result.rows() && operant.columns() != result.
				columns()) {
			throw new MatrixDimensionsOutOfBounds(operant, result);
		}

		for (int row_idx = 0; row_idx < operant.rows(); row_idx++) {
			double squared_sums = 0;
			for (int col_idx = 0; col_idx < operant.columns(); col_idx++) {
				squared_sums += Math.pow(operant.getQuick(row_idx, col_idx), 2);
			}
			double divisor = Math.sqrt(squared_sums);
			if (divisor > 0) {
				for (int col_idx = 0; col_idx < operant.columns(); col_idx++) {
					result.setQuick(row_idx, col_idx, operant.getQuick(row_idx, col_idx) / divisor);
				}
			}
		}

		return result;
	}

	/**
	 * Calculates the sum of each column and stores it into the result vector. A
	 * matrix with n rows and m columns requires a result vector with m entries.
	 *
	 * @param operant
	 * @param result
	 * @return
	 */
	public DoubleMatrix1D columnSums(final DoubleMatrix2D operant, final DoubleMatrix1D result) {
		if (operant.columns() != result.size()) {
			throw new IndexOutOfBoundsException("Result has " + result.size() + " entries but requires " + operant.
					columns());
		}
		operant.forEachNonZero(new IntIntDoubleFunction() {

			public double apply(int row, int col, double d) {
				result.setQuick(col, result.getQuick(col) + d);
				return d;
			}
		});
		return result;
	}

	/**
	 * Calculates the sum of each row and stores it into the result vector. A
	 * matrix with n rows and m columns requires a result vector with n entries.
	 *
	 * @param operant
	 * @param result
	 * @return the result parameter for convenience
	 */
	public DoubleMatrix1D rowSums(final DoubleMatrix2D operant, final DoubleMatrix1D result) {
		if (operant.rows() != result.size()) {
			throw new IndexOutOfBoundsException("Result has " + result.size() + " entries but requires " + operant.
					rows());
		}
		operant.trimToSize();
		operant.forEachNonZero(new IntIntDoubleFunction() {

			public double apply(int row, int col, double d) {
				result.setQuick(row, result.getQuick(row) + d);
				return d;
			}
		});
		return result;
	}

	/**
	 * Calculates the sum of each row and stores it into the result matrix. A
	 * matrix with n rows and m columns requires a result matrix with n rows and
	 * only one column.
	 *
	 * @param operant
	 * @param result
	 * @return the result parameter for convenience
	 */
	public DoubleMatrix2D rowSums2D(final DoubleMatrix2D operant, final DoubleMatrix2D result) {
		if (operant.rows() != result.rows()) {
			throw new IndexOutOfBoundsException("Result has " + result.size() + " entries but requires " + operant.
					rows());
		}
		if (result.columns() != 1) {
			throw new IndexOutOfBoundsException("Result matrix has to contain only 1 column");
		}
		operant.forEachNonZero(new IntIntDoubleFunction() {

			public double apply(int row, int col, double d) {
				result.setQuick(row, 1, result.getQuick(row, 1) + d);
				return d;
			}
		});
		return result;
	}

	/**
	 * Returns the lowest value in the matrix. Note that the natural ordering is
	 * used, e.g. -1 is lower than 0.
	 *
	 * @return
	 */
	public double min(DoubleMatrix2D matrix) {
		DoubleArrayList values = new DoubleArrayList();
		matrix.getNonZeros(new IntArrayList(), new IntArrayList(), values);
		double value = 0;
		for (int idx = 0; idx < values.size(); idx++) {
			value = Math.min(value, values.getQuick(idx));
		}
		return value;
	}

	/**
	 * Returns the highest value in the matrix. Note that the natural ordering
	 * is used, e.g. 1 is higher than -10.
	 *
	 * @return
	 */
	public double max(DoubleMatrix2D matrix) {
		DoubleArrayList values = new DoubleArrayList();
		matrix.getNonZeros(new IntArrayList(), new IntArrayList(), values);
		double value = 0;
		for (int idx = 0; idx < values.size(); idx++) {
			value = Math.max(value, values.getQuick(idx));
		}
		return value;
	}

	/**
	 * Returns the lowest value in the matrix. Note that the natural ordering is
	 * not used, e.g. 0 is lower than -1.
	 *
	 * @return
	 */
	public double minAbs(DoubleMatrix2D matrix) {
		DoubleArrayList values = new DoubleArrayList();
		matrix.getNonZeros(new IntArrayList(), new IntArrayList(), values);
		double value = 0;
		for (int idx = 0; idx < values.size(); idx++) {
			value = Math.min(value, Math.abs(values.getQuick(idx)));
		}
		return value;
	}

	/**
	 * Returns the lowest value in the matrix. Note that the natural ordering is
	 * not used, e.g. -1 is higher than 0.
	 *
	 * @return
	 */
	public double maxAbs(DoubleMatrix2D matrix) {
		DoubleArrayList values = new DoubleArrayList();
		matrix.getNonZeros(new IntArrayList(), new IntArrayList(), values);
		double value = 0;
		for (int idx = 0; idx < values.size(); idx++) {
			value = Math.max(value, Math.abs(values.getQuick(idx)));
		}
		return value;
	}

	/**
	 * Calculates the dot-wise minus: result[i] = operant1[i] - operant2[i]. If
	 * boolean flag is set to true, the absolute values are calculated:
	 * result[i] = abs(operant1[i] - operant2[i]). Returns the result parameter.
	 *
	 * @param operant1
	 * @param operant2
	 * @param result
	 * @param abs
	 * @return
	 */
	public DoubleMatrix1D minus(DoubleMatrix1D operant1, DoubleMatrix1D operant2, DoubleMatrix1D result, boolean abs) {
		for (int i = 0; i < operant1.size(); i++) {
			double diff = operant1.getQuick(i) - operant2.getQuick(i);
			result.set(i, abs ? Math.abs(diff) : diff);
		}
		return result;
	}

	/**
	 * Calculates the sum of all values in the vector.
	 */
	public double sum(DoubleMatrix1D operant) {
		double sum = 0;
		for (int i = 0; i < operant.size(); i++) {
			sum += operant.getQuick(i);
		}
		return sum;
	}

	public DoubleMatrix1D add(DoubleMatrix1D operant1, DoubleMatrix1D operant2, DoubleMatrix1D result) {
		for (int i = 0; i < operant1.size(); i++) {
			result.set(i, operant1.getQuick(i) + operant2.getQuick(i));
		}
		return result;
	}
}
