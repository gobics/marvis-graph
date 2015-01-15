/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.matrix;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class SparseDoubleMatrix2D extends cern.colt.matrix.impl.SparseDoubleMatrix2D implements AnnotatedMatrix2D {

	private final Object[] row_label;
	private final Object[] column_label;

	public SparseDoubleMatrix2D(double[][] doubles) {
		super(doubles);
		trimToSize();
		row_label = new Object[rows()];
		column_label = new Object[columns()];
	}

	public SparseDoubleMatrix2D(int i, int i1) {
		super(i, i1);
		row_label = new Object[rows()];
		column_label = new Object[columns()];
	}

	public SparseDoubleMatrix2D(int i, int i1, int i2, double d, double d1) {
		super(i, i1, i2, d, d1);
		row_label = new Object[rows()];
		column_label = new Object[columns()];
	}

	public SparseDoubleMatrix2D(cern.colt.matrix.DoubleMatrix2D template) {
		this(template.rows(), template.columns());
		for (int row = 0; row < rows(); row++) {
			for (int col = 0; col < columns(); col++) {
				double v = template.getQuick(row, col);
				if (v != 0) {
					setQuick(row, col, v);
				}
			}
		}
	}

	public static SparseDoubleMatrix2D transformTo2D(cern.colt.matrix.DoubleMatrix1D template, boolean in_row) {
		SparseDoubleMatrix2D result;
		if (in_row) {
			result = new SparseDoubleMatrix2D(1, template.size());
			for (int idx = 0; idx < template.size(); idx++) {
				result.setQuick(1, idx, template.getQuick(idx));
			}
		}
		else {
			result = new SparseDoubleMatrix2D(template.size(), 1);
			for (int idx = 0; idx < template.size(); idx++) {
				result.setQuick(idx, 1, template.getQuick(idx));
			}
		}
		result.trimToSize();
		return result;

	}

	public SparseDoubleMatrix2D(Object[] row_labels, Object[] col_labels) {
		this(row_labels.length, col_labels.length);
		setRowLabels(row_labels);
		setColumnLabels(col_labels);
	}

	public void setRowLabel(int idx, Object label) {
		if (idx < 0 || idx >= rows()) {
			throw new IndexOutOfBoundsException("Try to set label for row " + idx + " in matrix with " + rows() + " rows");
		}
		row_label[idx] = label;
	}

	public void setColumnLabel(int idx, Object label) {
		if (idx < 0 || idx >= columns()) {
			throw new IndexOutOfBoundsException("Try to set label for column " + idx + " in matrix with " + columns() + " columns");
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
			throw new IndexOutOfBoundsException("New label array is longer than number of rows");
		}
		System.arraycopy(new_labels, 0, row_label, 0, new_labels.length);
	}

	public void setColumnLabels(Object[] new_labels) {
		if (new_labels.length > columns()) {
			throw new IndexOutOfBoundsException("New string array is longer than number of rows");
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
	public SparseDoubleMatrix2D viewSelection(int[] rows, int[] cols) {
		SparseDoubleMatrix2D result = new SparseDoubleMatrix2D(super.
				viewSelection(rows, cols));

		for (int idx = 0; idx < rows.length; idx++) {
			result.setRowLabel(idx, this.getRowLabel(rows[idx]));
		}
		for (int idx = 0; idx < cols.length; idx++) {
			result.setColumnLabel(idx, this.getColumnLabel(cols[idx]));
		}


		return result;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + rows() + "x" + columns() + "]";
	}

	@Override
	public DoubleMatrix2D like() {
		return new SparseDoubleMatrix2D(rows(), columns());
	}

	@Override
	public DoubleMatrix2D like(int rows, int columns) {
		return new SparseDoubleMatrix2D(rows, columns);
	}

	@Override
	public DoubleMatrix1D like1D(int size) {
		return new SparseDoubleMatrix1D(size);
	}

	public String dump() {
		StringBuilder sb = new StringBuilder(toString());
		for (Object clabel : getColumnLabels()) {
			sb.append("\t").append(clabel.toString());
		}
		sb.append("\n");

		for (int row = 0; row < rows; row++) {
			sb.append(getRowLabel(row).toString());
			for (int col = 0; col < columns; col++) {
				sb.append("\t").append(Double.toString(getQuick(row, col)));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
