/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.matrix;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class DenseDoubleMatrix2D extends cern.colt.matrix.impl.DenseDoubleMatrix2D implements AnnotatedMatrix2D {

	private final Object[] row_label;
	private final Object[] column_label;

	public DenseDoubleMatrix2D(double[][] doubles) {
		super(doubles);
		trimToSize();
		row_label = new Object[rows()];
		for (int idx = 0; idx < row_label.length; idx++) {
			row_label[idx] = Integer.toString(idx + 1);
		}
		column_label = new Object[columns()];
		for (int idx = 0; idx < column_label.length; idx++) {
			column_label[idx] = Integer.toString(idx + 1);
		}
	}

	public DenseDoubleMatrix2D(int i, int i1) {
		super(i, i1);
		row_label = new Object[rows()];
		for (int idx = 0; idx < row_label.length; idx++) {
			row_label[idx] = Integer.toString(idx + 1);
		}
		column_label = new Object[columns()];
		for (int idx = 0; idx < column_label.length; idx++) {
			column_label[idx] = Integer.toString(idx + 1);
		}
	}

	public DenseDoubleMatrix2D(DoubleMatrix2D matrix) {
		super(matrix.rows(), matrix.columns());
		matrix.trimToSize();

		for (int row = 0; row < rows(); row++) {
			for (int col = 0; col < columns(); col++) {
				setQuick(row, col, matrix.getQuick(row, col));
			}
		}

		row_label = new Object[rows()];
		column_label = new Object[columns()];
		if (matrix instanceof AnnotatedMatrix2D) {
			setRowLabels(((AnnotatedMatrix2D) matrix).getRowLabels());
			setColumnLabels(((AnnotatedMatrix2D) matrix).getColumnLabels());
		}
		else {
			for (int idx = 0; idx < row_label.length; idx++) {
				row_label[idx] = Integer.toString(idx + 1);
			}
			for (int idx = 0; idx < column_label.length; idx++) {
				column_label[idx] = Integer.toString(idx + 1);
			}
		}
	}

	public static DenseDoubleMatrix2D transformTo2D(cern.colt.matrix.DoubleMatrix1D template, boolean in_row) {
		DenseDoubleMatrix2D result;
		if (in_row) {
			result = new DenseDoubleMatrix2D(1, template.size());
			for (int idx = 0; idx < template.size(); idx++) {
				result.setQuick(1, idx, template.getQuick(idx));
			}
		}
		else {
			result = new DenseDoubleMatrix2D(template.size(), 1);
			for (int idx = 0; idx < template.size(); idx++) {
				result.setQuick(idx, 1, template.getQuick(idx));
			}
		}
		result.trimToSize();
		return result;

	}

	public DenseDoubleMatrix2D(Object[] row_labels, Object[] col_labels) {
		this(row_labels.length, col_labels.length);
		setRowLabels(row_labels);
		setColumnLabels(col_labels);
	}

	public void setRowLabel(int idx, Object label) {
		if (idx < 0 || idx > rows() - 1) {
			throw new IndexOutOfBoundsException();
		}
		row_label[idx] = label;
	}

	public void setColumnLabel(int idx, Object label) {
		if (idx < 0 || idx > columns() - 1) {
			throw new IndexOutOfBoundsException();
		}
		column_label[idx] = label;
	}

	public Object getRowLabel(int idx) {
		return row_label[idx];
	}

	public Object getColumnLabel(int idx) {
		return column_label[idx];
	}

	public LabelLookup getRowLookup() {
		HashMap<Object, HashSet<Integer>> lookup = new HashMap<Object, HashSet<Integer>>();
		for (int idx = 0; idx < rows(); idx++) {
			if (row_label[idx] != null) {
				if (lookup.containsKey(row_label[idx])) {
					lookup.get(row_label[idx]).add(idx);
				}
				else {
					HashSet<Integer> set = new HashSet<Integer>();
					set.add(idx);
					lookup.put(row_label[idx], set);
				}
			}
		}
		return new LabelLookup(lookup);
	}

	public LabelLookup getColumnLookup() {
		HashMap<Object, HashSet<Integer>> lookup = new HashMap<Object, HashSet<Integer>>();
		for (int idx = 0; idx < columns(); idx++) {
			if (column_label[idx] != null) {
				if (lookup.containsKey(column_label[idx])) {
					lookup.get(column_label[idx]).add(idx);
				}
				else {
					HashSet<Integer> set = new HashSet<Integer>();
					set.add(idx);
					lookup.put(column_label[idx], set);
				}
			}
		}
		return new LabelLookup(lookup);
	}

	public int getRowForLabel(Object label) {
		return getRowLookup().getFirst(label);
	}

	public int getColumnForLabel(Object label) {
		return getColumnLookup().getFirst(label);
	}

	public Object[] getRowLabels() {
		return row_label.clone();
	}

	public Object[] getColumnLabels() {
		return column_label.clone();
	}

	public void setRowLabels(Object[] new_labels) {
		if (new_labels.length > rows()) {
			throw new IndexOutOfBoundsException("New string array is longer than number of rows");
		}
		System.arraycopy(new_labels, 0, row_label, 0, new_labels.length);

	}

	public void setColumnLabels(Object[] new_labels) {
		if (new_labels.length > columns()) {
			throw new IllegalArgumentException("New string array is longer than number of rows");
		}

		System.arraycopy(new_labels, 0, column_label, 0, new_labels.length);
	}

	/**
	 * Replaces the values in the row
	 * <pre>row_idx</pre> with the given vector. This is useful if no new matrix
	 * should be constructed.
	 *
	 * @param row_idx the row to replace
	 * @param row_vector the values with that the current values should be
	 * replaced
	 * @throws IllegalArgumentException if the row_vector is to long or short
	 */
	public void replaceRowI(int row_idx, DoubleMatrix1D row_vector) {
		if (row_vector.size() != columns()) {
			throw new IllegalArgumentException("The vector needs to contain " + columns() + " entries, not: " + row_vector.
					size());
		}
		for (int col_idx = 0; col_idx < row_vector.size(); col_idx++) {
			setQuick(row_idx, col_idx, row_vector.getQuick(col_idx));
		}
	}

	@Override
	public DenseDoubleMatrix2D viewSelection(int[] rows, int[] cols) {
		DenseDoubleMatrix2D result = new DenseDoubleMatrix2D(super.
				viewSelection(rows, cols));
		for (int idx = 0; idx < rows.length; idx++) {
			result.setRowLabel(idx, this.getRowLabel(rows[idx]));
		}
		for (int idx = 0; idx < cols.length; idx++) {
			result.setColumnLabel(idx, this.getColumnLabel(cols[idx]));
		}

		return result;
	}

	/**
	 * Returns the lowest value in the matrix. Note that the natural ordering is
	 * used, e.g. -1 is lower than 0.
	 *
	 * @return
	 */
	public double min() {
		DoubleArrayList values = new DoubleArrayList();
		getNonZeros(new IntArrayList(), new IntArrayList(), values);
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
	public double max() {
		DoubleArrayList values = new DoubleArrayList();
		getNonZeros(new IntArrayList(), new IntArrayList(), values);
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
	public double minAbs() {
		DoubleArrayList values = new DoubleArrayList();
		getNonZeros(new IntArrayList(), new IntArrayList(), values);
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
	public double maxAbs() {
		DoubleArrayList values = new DoubleArrayList();
		getNonZeros(new IntArrayList(), new IntArrayList(), values);
		double value = 0;
		for (int idx = 0; idx < values.size(); idx++) {
			value = Math.max(value, Math.abs(values.getQuick(idx)));
		}
		return value;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + rows() + "x" + columns() + "]";
	}

	@Override
	public DoubleMatrix2D like() {
		return new DenseDoubleMatrix2D(rows(), columns());
	}

	@Override
	public DoubleMatrix2D like(int rows, int columns) {
		return new DenseDoubleMatrix2D(rows, columns);
	}

	@Override
	public DoubleMatrix1D like1D(int size) {
		return new SparseDoubleMatrix1D(size);
	}

	public String toStringLong() {
		StringBuilder sb = new StringBuilder(this.toString());
		for (Object label : getColumnLabels()) {
			sb.append("\t").append(label);
		}
		sb.append("\n");
		for (int row = 0; row < rows(); row++) {
			sb.append(getRowLabel(row));
			for (int col = 0; col < columns(); col++) {
				sb.append("\t").append(getQuick(row, col));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
