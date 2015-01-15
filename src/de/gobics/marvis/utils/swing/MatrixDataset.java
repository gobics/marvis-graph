package de.gobics.marvis.utils.swing;

import cern.colt.matrix.DoubleMatrix2D;
import java.util.LinkedList;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;

/**
 * This class is used to visualize a matrix in the JFreeChart. It implements the
 * {@link XYZDataset} to be used in JFreeChart.
 *
 * A matrix is used as data-backend. The x-coordinates are the columns and the
 * y-coordinates are the rows.
 *
 * There are several conversions done here. The JFreeChart counting starts in
 * the left-bottom corner, while Matrix counts start in the left-top corner.
 * Therefore, the true row has to calculated every time it is accessed.
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class MatrixDataset implements XYZDataset {

	private final LinkedList<DatasetChangeListener> listener = new LinkedList<DatasetChangeListener>();
	private final DoubleMatrix2D matrix;
	private int offset_rows = -1;
	private int offset_columns = -1;
	private int length_rows = -1;
	private int length_columns = -1;

	public MatrixDataset(DoubleMatrix2D matrix) {
		this.matrix = matrix;
		setView(0, 0, matrix.rows(), matrix.columns());
	}

	public DoubleMatrix2D getMatrix(){
		return matrix;
	}
	
	public void setView(int offset_rows, int offset_columns, int length_rows, int length_columns) {
		if (length_rows < 0) {
			throw new IllegalArgumentException("Number of displayed rows has to be greater than zero, but not: " + length_rows);
		}
		if (length_columns < 0) {
			throw new IllegalArgumentException("Number of displayed columns has to be greater than zero, but not: " + length_columns);
		}
		if (offset_rows < 0) {
			throw new IllegalArgumentException("Row offset has to be greater or equal zero, not: " + offset_rows);
		}
		if (offset_columns < 0) {
			throw new IllegalArgumentException("Column offset has to be greater or equal zero, not: " + offset_columns);
		}

		if (offset_rows + length_rows > matrix.rows()) {
			throw new IllegalArgumentException("Try to display rows " + offset_rows + " to " + (offset_rows + length_rows) + " but matrix has rows: " + matrix.
					rows());
		}
		if (offset_columns + length_columns > matrix.columns()) {
			throw new IllegalArgumentException("Try to display columns " + offset_columns + " to " + (offset_columns + length_columns) + " but matrix has columns: " + matrix.
					columns());
		}

		this.offset_rows = offset_rows;
		this.offset_columns = offset_columns;
		this.length_rows = length_rows;
		this.length_columns = length_columns;
		//System.err.println("New viewpoint starts at " + offset_rows + "x" + offset_columns + " and displays " + length_rows + " rows and " + length_columns + " columns");
		notifyListener();
	}

	private int trueRow(int row) {
		return matrix.rows() + offset_rows - row - 1;
	}

	private int trueColumn(int column) {
		return column + offset_columns;
	}

	@Override
	public Number getZ(int row, int col) {
		return (Double) getZValue(row, col);
	}

	@Override
	public double getZValue(int row, int col) {
		//System.err.println("Request " + row + "x" + col + " translates " + trueRow(row) + "x" + trueColumn(col));
		//System.err.println(" ... with value: " + matrix.getQuick(trueRow(row), trueColumn(col)));
		return matrix.getQuick(trueRow(row), trueColumn(col));
	}

	@Override
	public DomainOrder getDomainOrder() {
		return DomainOrder.NONE;
	}

	@Override
	public Number getX(int row, int col) {
		return (Integer) col;
	}

	@Override
	public double getXValue(int row, int col) {
		return (double) col;
	}

	@Override
	public Number getY(int row, int col) {
		return (Integer) row;
	}

	@Override
	public double getYValue(int row, int col) {
		return (double) row;
	}

	@Override
	public int getItemCount(int i) {
		return length_columns;
	}

	@Override
	public int getSeriesCount() {
		return length_rows;
	}

	@Override
	public Comparable getSeriesKey(int col) {
		return "Matrix";
	}

	@Override
	public int indexOf(Comparable cmprbl) {
		throw new UnsupportedOperationException("Woah!");//return matrix.getRowForLabel(((Object)cmprbl).toString());
	}

	@Override
	public void addChangeListener(DatasetChangeListener dl) {
		listener.add(dl);
	}

	@Override
	public void removeChangeListener(DatasetChangeListener dl) {
		listener.remove(dl);
	}

	private void notifyListener() {
		DatasetChangeEvent e = new DatasetChangeEvent(this, this);
		for (DatasetChangeListener l : listener) {
			l.datasetChanged(e);
		}
	}

	@Override
	public DatasetGroup getGroup() {
		return new DatasetGroup();
	}

	@Override
	public void setGroup(DatasetGroup dg) {
		// ignore
	}
}
