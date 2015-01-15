package de.gobics.marvis.utils.matrix;

/**
 * A matrix implementing this interface has row and column labels
 *
 * @author Manuel Landesfeind <manuel@gobics.de>
 */
public interface AnnotatedMatrix2D {

	/**
	 * Set the label for the row with the given index to the new label.
	 * @param idx Index of the row
	 * @param label New label
	 */
	public void setRowLabel(int idx, Object label);

	/**
	 * Set the label for the column with the given index to the new label.
	 * @param idx Index of the column
	 * @param label New label
	 */
	public void setColumnLabel(int idx, Object label);

	/**
	 * Returns the label of the given row.
	 * @param idx Index of the row
	 * @return label of the row
	 */
	public Object getRowLabel(int idx);

	/**
	 * Returns the label of the given column.
	 * @param idx Index of the row
	 * @return label of the row
	 */
	public Object getColumnLabel(int idx);

	/**
	 * Returns the first row that has a label equals the given one.
	 * @param label Label to search for
	 * @return row index
	 */
	public int getRowForLabel(Object label);

	/**
	 * Returns the first column that has a label equals the given one.
	 * @param label Label to search for
	 * @return column index
	 */
	public int getColumnForLabel(Object label);

	/**
	 * Returns an array containing all row label.
	 * @return The array with the labels
	 */
	public Object[] getRowLabels();

	/** 
	 * Returns an array containing all column label.
	 * @return an array with the labels.
	 */
	public Object[] getColumnLabels();

	/** 
	 * Set the row labels to the labels given in the {@code new_labels} array.
	 * The length of the given array has to equal the number of rows.
	 * @param new_labels 
	 */
	public void setRowLabels(Object[] new_labels);

	/** 
	 * Set the column labels to the labels given in the {@code new_labels} array.
	 * The length of the given array has to equal the number of columns.
	 * @param new_labels 
	 * @thows IllegalArgumentException if the new label array if to long
	 */
	public void setColumnLabels(Object[] new_labels);
	
	public LabelLookup getRowLookup();
	
	public LabelLookup getColumnLookup();
}
