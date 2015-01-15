/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing;

import cern.colt.matrix.DoubleMatrix2D;
import de.gobics.marvis.utils.ArrayUtils;
import de.gobics.marvis.utils.matrix.Algebra;
import de.gobics.marvis.utils.matrix.AnnotatedMatrix2D;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class MatrixHeatmap extends JPanel {

	private static final int MAXIMUM_SIZE = 500;
	private final MatrixDataset dataset;
	private final XYBlockRenderer renderer;
	private final XYPlot plot;
	private final JFreeChart chart;
	private final ChartPanel panel;
	private final JScrollBar bar_columns;
	private final JScrollBar bar_rows;

	public MatrixHeatmap(DoubleMatrix2D matrix) {
		this(null, matrix);
	}

	public MatrixHeatmap(String title, DoubleMatrix2D matrix) {
		this(title, matrix, Color.BLUE, Color.yellow, Color.RED);
	}

	public MatrixHeatmap(String title, DoubleMatrix2D matrix, Color lower_color, Color mean_color, Color upper_color) {
		setLayout(new BorderLayout());

		dataset = new MatrixDataset(matrix);

		SymbolAxis yAxis = new SymbolAxis("Rows", getRowLabels(0, 1));
		SymbolAxis xAxis = new SymbolAxis("Columns", getColumnLabels(0, 1));

		renderer = new XYBlockRenderer();

		plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setDomainAxisLocation(AxisLocation.TOP_OR_LEFT);


		chart = new JFreeChart(plot);
		if (title != null && !title.isEmpty()) {
			chart.setTitle(title);
		}
		chart.removeLegend();
		//chart.setAntiAlias(false);





		if (matrix.rows() > MAXIMUM_SIZE) {
			bar_rows = new JScrollBar(JScrollBar.VERTICAL, 0, MAXIMUM_SIZE - 1, 0, matrix.
					rows() - MAXIMUM_SIZE + 1);
			add(bar_rows, BorderLayout.LINE_END);
			bar_rows.addAdjustmentListener(new AdjustmentListener() {
				@Override
				public void adjustmentValueChanged(AdjustmentEvent ae) {
					MatrixHeatmap.this.updateViewport();
				}
			});
		}
		else {
			bar_rows = null;
		}
		if (matrix.columns() > MAXIMUM_SIZE) {
			bar_columns = new JScrollBar(JScrollBar.HORIZONTAL, 0, MAXIMUM_SIZE - 1, 0, matrix.
					columns() - MAXIMUM_SIZE + 1);
			add(bar_columns, BorderLayout.PAGE_END);
			bar_columns.addAdjustmentListener(new AdjustmentListener() {
				@Override
				public void adjustmentValueChanged(AdjustmentEvent ae) {
					MatrixHeatmap.this.updateViewport();
				}
			});
		}
		else {
			bar_columns = null;
		}


		panel = new ChartPanel(chart);
		add(panel, BorderLayout.CENTER);

		setView(0, 0, Math.min(matrix.rows(), MAXIMUM_SIZE), Math.min(matrix.
				columns(), MAXIMUM_SIZE));


		setHeatmapColor(lower_color, mean_color, upper_color);
	}

	private void updateViewport() {
		if ((bar_rows != null && bar_rows.getValueIsAdjusting())
				|| (bar_columns != null && bar_columns.getValueIsAdjusting())) {
			return;
		}

		int offset_rows = bar_rows != null ? bar_rows.getValue() : 0;
		int offset_columns = bar_columns != null ? bar_columns.getValue() : 0;
		
		int length_rows = dataset.getMatrix().rows() < offset_rows + MAXIMUM_SIZE ? dataset.getMatrix().rows() - offset_rows : MAXIMUM_SIZE;
		int length_cols = dataset.getMatrix().columns() < offset_columns + MAXIMUM_SIZE ? dataset.getMatrix().columns() - offset_rows : MAXIMUM_SIZE;

		setView(offset_rows, offset_columns, length_rows, length_cols);
	}

	private void setView(int offset_x, int offset_y, int rows, int columns) {
		dataset.setView(offset_x, offset_y, rows, columns);
		plot.setDomainAxis(new SymbolAxis("Columns", getColumnLabels(offset_y, columns)));
		plot.setRangeAxis(new SymbolAxis("Rows", getRowLabels(offset_x, rows)));
	}

	private String[] getRowLabels(int offset, int length) {
		DoubleMatrix2D matrix = dataset.getMatrix();
		String[] labels = new String[length];
		if (matrix instanceof AnnotatedMatrix2D) {
			Object[] labels2 = ((AnnotatedMatrix2D) matrix).getRowLabels();
			for (int idx = 0; idx < length; idx++) {
				labels[idx] = labels2[offset + idx].toString();
			}
		}
		else {
			for (int idx = offset; idx < offset + length; idx++) {
				labels[idx - offset] = "Row " + (idx + 1);
			}
		}

		return ArrayUtils.reverse(labels);
	}

	private String[] getColumnLabels(int offset, int length) {
		DoubleMatrix2D matrix = dataset.getMatrix();
		String[] labels = new String[length];
		if (matrix instanceof AnnotatedMatrix2D) {
			Object[] labels2 = ((AnnotatedMatrix2D) matrix).getColumnLabels();
			for (int idx = 0; idx < length; idx++) {
				labels[idx] = labels2[offset + idx].toString();
			}
		}
		else {
			for (int idx = offset; idx < offset + length; idx++) {
				labels[idx - offset] = "Column " + (idx + 1);
			}
		}
		return labels;
	}

	public void setPopupMenu(JPopupMenu menu) {
		panel.setPopupMenu(menu);
	}

	public void resetZoom() {
		panel.restoreAutoBounds();
	}

	public DoubleMatrix2D getMatrix() {
		return dataset.getMatrix();
	}

	public void setHeatmapColor(Color lower_color, Color mean_color, Color upper_color) {
		double min = Algebra.getInstance().min(getMatrix());
		double max = Algebra.getInstance().max(getMatrix());
		HeatmapPaintScale scale = new HeatmapPaintScale(min, max > min ? max : min + 1, lower_color, upper_color, mean_color);
		setHeatmapPaintScale(scale);
	}

	public void setHeatmapPaintScale(HeatmapPaintScale scale) {
		renderer.setPaintScale(scale);

		// Add a legend explaining the color
		NumberAxis scaleAxis = new NumberAxis();
		scaleAxis.setAxisLinePaint(Color.white);
		scaleAxis.setTickMarkPaint(Color.white);
		scaleAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 7));
		PaintScaleLegend legend = new PaintScaleLegend(scale, scaleAxis);
		legend.setStripOutlineVisible(true);
		legend.setSubdivisionCount(30);
		legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		legend.setAxisOffset(5.0);
		legend.setMargin(new RectangleInsets(5, 5, 5, 5));
		legend.setPadding(new RectangleInsets(10, 10, 10, 10));
		legend.setStripWidth(10);
		legend.setPosition(RectangleEdge.RIGHT);
		chart.clearSubtitles();
		chart.addSubtitle(legend);
	}

	public String getTitle() {
		return chart.getTitle().getText();
	}

	public void setTitle(String new_title) {
		chart.setTitle(new_title);
	}

	public JFreeChart getChart() {
		return chart;
	}

	public HeatmapPaintScale getHeatmapPaintScale() {
		return (HeatmapPaintScale) renderer.getPaintScale();
	}
}