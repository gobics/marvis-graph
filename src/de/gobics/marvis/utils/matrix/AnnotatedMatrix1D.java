package de.gobics.marvis.utils.matrix;

/**
 * A vector implementing this interface has labels
 *
 * @author manuel
 */
public interface AnnotatedMatrix1D {

	/**
	 * Set the label for the cell with the given index to the new label.
	 * @param idx Index of the cell
	 * @param label New label
	 */
	public void setLabel(int idx, Object label);

	/**
	 * Returns the label of the given cell.
	 * @param idx Index of the cell
	 * @return label of the cell
	 */
	public Object getLabel(int idx);

	/**
	 * Returns the first cell that has a label equals the given one.
	 * @param label Label to search for
	 * @return cell index
	 */
	public int getIndexForLabel(Object label);

	/**
	 * Set all label to new values at once. The {@code new_labels} array must have
	 * the same length as the size of this vector.
	 * @param new_labels 
	 */
	public void setLabels(Object[] new_labels);

	/**
	 * Returns an array containing all labels.
	 * @return 
	 */
	public Object[] getLabels();
	
	public LabelLookup getLookup();
}
