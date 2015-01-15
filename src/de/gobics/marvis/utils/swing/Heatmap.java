package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.ArrayUtils;
import de.gobics.marvis.utils.ColorUtils;
import de.gobics.marvis.utils.ColorUtils.ScalingType;
import de.gobics.marvis.utils.DoubleUtils;
import de.gobics.marvis.utils.matrix.SparseDoubleMatrix2D;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import net.sf.javaml.clustering.SOM;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

public class Heatmap extends JPanel implements MouseListener, MouseMotionListener {

	private static final Logger logger = Logger.getLogger(Heatmap.class.getName());
	private int offset_x_left = 30;
	private int offset_x_right = 0;
	private int offset_y_top = 0;
	private int offset_y_bottom = 15;
	private int legend_x_width = 20;
	private Point2D tooltip_point = null;
	private Color color_low = Color.BLUE;
	private Color color_high = Color.RED;
	private int color_distance;
	private double color_scale = 1;
	private HashSet<HeatmapListener> listener =
			new HashSet<HeatmapListener>();
	private MouseEvent drag_event;

	public double getColorScale() {
		return color_scale;
	}

	public static enum HistogramType {

		Raw, LabelXMean, LabelYMean
	}
	private HistogramType type = HistogramType.Raw;
	private double[][] data_points;
	private double[][] data_points_to_draw;
	private Object[] labels_x;
	private Object[] labels_x_to_draw;
	private Object[] labels_y;
	private Object[] labels_y_to_draw;
	private boolean[] selected_x;
	private boolean[] selected_y;
	private int labels_x_first;
	private int labels_x_last;
	private int labels_y_first;
	private int labels_y_last;
	private double max;
	private double min;
	private boolean normalize_data = false;
	
	public Heatmap(double[][] data) {
		setDatapoints(data);

		String[] ls = new String[data.length];
		for (int i = 0; i < data.length; i++) {
			ls[i] = Integer.toString(i + 1);
		}
		setLabelX(ls);

		ls = new String[data[0].length];
		for (int i = 0; i < data[0].length; i++) {
			ls[i] = Integer.toString(i + 1);
		}
		setLabelY(ls);

		addMouseListener(this);
		addMouseMotionListener(this);
		setMinimumSize(new Dimension(50, 50));
		setPreferredSize(new Dimension(200, 100));
		updateColorDistance();
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(
				offset_x_left + offset_x_right + 10,
				offset_y_top + offset_y_bottom + 10);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = getMinimumSize();
		d.height += 100;
		d.width += 300;
		return d;
	}

	public void setHistogramType(HistogramType t) {
		data_points_to_draw = null;
		type = t;
	}

	public void setColorScale(ScalingType type) {
		color_scale = ColorUtils.getFactorFromScale(type);

	}

	public void setColorScale(double d) {
		this.color_scale = d;
	}

	public void setNormalizeData(boolean value) {
		normalize_data = value;
		data_points_to_draw = null;
	}

	public boolean getNormalizeData() {
		return this.normalize_data;
	}

	public void setDatapoints(double[][] data) {
		int vector_length = data[0].length;
		for (int i = 0; i < data.length; i++) {
			if (data[i].length != vector_length) {
				throw new RuntimeException("Data is not a matrix");
			}
		}
		data_points = data.clone();
		calculateMaxMinValues();
	}

	public int getSizeX() {
		return data_points.length;
	}

	public int getSizeY() {
		return data_points[0].length;
	}

	public double getMaxValue() {
		return max;
	}

	public void setMaxValue(double new_max) {
		if (new_max < getMinValue()) {
			setMinValue(new_max);
		}
		max = new_max;
	}

	public double getMinValue() {
		return min;
	}

	public void setMinValue(double new_min) {
		if (new_min > getMaxValue()) {
			setMaxValue(new_min);
		}
		min = new_min;
	}

	public void calculateMaxMinValues() {
		max = 0;
		min = 0;
		for (double[] vector : data_points) {
			for (double d : vector) {
				if (d > max) {
					max = d;
				}
				if (d < min) {
					min = d;
				}
			}
		}
	}

	void resetZoom() {
		labels_x_first = 0;
		labels_x_last = labels_x_to_draw.length - 1;
		labels_y_first = 0;
		labels_y_last = labels_y_to_draw.length - 1;
	}

	public double getIntervallSize() {
		return Math.abs(getMaxValue() - getMinValue());
	}

	public double[][] getDatapoints() {
		return data_points.clone();
	}

	public void setLabelX(Object[] label) {
		if (label.length != getSizeX()) {
			logger.log(Level.SEVERE, "Length of label array (" + label.length + ") does not equal data array (" + data_points.length + ")");
			throw new RuntimeException("Length of label array (" + label.length + ") has to equal data array x (" + data_points.length + ")");
		}
		labels_x = label;
	}

	public void setLabelY(Object[] label) {
		if (label.length != getSizeY()) {
			logger.log(Level.SEVERE, "Length of label array does not equal data array y");
			throw new RuntimeException("Length of label array (" + label.length + ") has to equal data array y (" + data_points[0].length + ")");
		}
		labels_y = label.clone();
	}

	public void setColorLow(Color nl) {
		if (nl != null && !color_low.equals(nl)) {
			color_low = nl;
			updateColorDistance();
			repaint();
		}
	}

	public void setColorHigh(Color nh) {
		if (nh != null && !color_high.equals(nh)) {
			color_high = nh;
			updateColorDistance();
			repaint();
		}
	}

	public Color getColorLow() {
		return color_low;
	}

	public Color getColorHigh() {
		return color_high;
	}

	public void setLabelXFirst(Object label) {
		setLabelXFirst(ArrayUtils.indexOf(labels_x_to_draw, label));
	}

	public void setLabelXFirst(int index) {
		if (index > 0 && index < labels_x_to_draw.length) {
			labels_x_first = index;
			labels_x_last = Math.max(labels_x_first, labels_x_last);
		}
	}

	public void setLabelXLast(Object label) {
		setLabelXLast(ArrayUtils.lastIndexOf(labels_x_to_draw, label));
	}

	public void setLabelXLast(int index) {
		if (index >= 0 && index < labels_x_to_draw.length) {
			labels_x_last = index;
			labels_x_first = Math.min(labels_x_first, labels_x_last);
			logger.finer("DEBUG: " + labels_x_first + " <-> " + labels_x_last);
		}
	}

	public void setLabelYFirst(Object label) {
		setLabelYFirst(ArrayUtils.indexOf(labels_y_to_draw, label));
	}

	public void setLabelYFirst(int index) {
		if (index >= 0 && index < labels_y_to_draw.length) {
			labels_y_first = index;
			labels_y_last = Math.max(labels_y_first, labels_y_last);
		}
	}

	public void setLabelYLast(Object label) {
		setLabelYLast(ArrayUtils.lastIndexOf(labels_y_to_draw, label));
	}

	public void setLabelYLast(int index) {
		if (index >= 0 && index < labels_y_to_draw.length) {
			labels_y_last = index;
			labels_y_first = Math.min(labels_y_first, labels_y_last);
			logger.finer("DEBUG: " + labels_y_first + " <-> " + labels_y_last);
		}
	}

	public int countLabelsToDrawX() {
		return labels_x_last - labels_x_first + 1;
	}

	public int countLabelsToDrawY() {
		return labels_y_last - labels_y_first + 1;
	}

	public Object[] getLabelsX() {
		return labels_x.clone();
	}

	public Object[] getLabelsY() {
		return labels_y.clone();
	}

	public Object getLabelX(Point location) {
		logger.finer("Getting label for location: " + location);
		int idx = getIdxByPointX(location.x);
		if (idx == -1) {
			return null;
		}
		return labels_x_to_draw[idx];
	}

	public Object getLabelY(Point location) {
		int idx = getIdxByPointY(location.y);
		if (idx == -1) {
			return null;
		}
		return labels_y_to_draw[idx];
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Ensure the calculated values are available
		calcDtd();
		calculateOffsets(g);

		// Calculate sizes of the historgram
		int max_width = getWidth() - offset_x_left - offset_x_right - legend_x_width;
		int max_height = getHeight() - offset_y_top - offset_y_bottom;

		// x is the x-coord of the current bar
		int x = offset_x_left;
		int y = offset_y_top;
		int x_width = (int) Math.floor((max_width - 2) / countLabelsToDrawX());
		int y_width = (int) Math.floor((max_height - 2) / countLabelsToDrawY());
		int true_width = countLabelsToDrawX() * x_width;
		int true_height = countLabelsToDrawY() * y_width;

		// Fill the background of the heatmap
		g.setColor(Color.WHITE);
		g.fillRect(offset_x_left, offset_y_top, true_width, true_height);
		g.setColor(Color.BLACK);

		for (int i = labels_x_first; i <= labels_x_last; i++) {
			y = offset_y_top;
			for (int j = labels_y_first; j <= labels_y_last; j++) {
				g.setColor(ColorUtils.getScaledColor(data_points_to_draw[i][j], min, max, color_low, color_high, color_scale));
				g.fillRect(x, y, x_width, y_width);
				y += y_width;
			}
			x += x_width;
		}

		// Draw border of heatmap
		g.setColor(Color.BLACK);
		g.drawRect(offset_x_left, offset_y_top, true_width, true_height);

		// Display labels
		x = offset_x_left;
		g.setColor(Color.BLACK);
		Object old_label = null;
		for (int i = labels_x_first; i <= labels_x_last; i++) {
			if( labels_x_to_draw[i] == null ){
				labels_x_to_draw[i] = "null";
			}
			if (!labels_x_to_draw[i].equals(old_label)) {
				g.drawString(labels_x_to_draw[i].toString(), x + 2, getHeight() - 2);
				old_label = labels_x_to_draw[i];
			}
			g.drawLine(x, offset_y_top, x, true_height + 2);
			if (selected_x[i]) {
				g.drawLine(x + 1, offset_y_top + true_height + 4, x + x_width - 1, offset_y_top + true_height + 4);
			}
			x += x_width;
		}
		y = offset_y_top + y_width;
		old_label = null;
		for (int i = labels_y_first; i <= labels_y_last; i++) {
			if( labels_y_to_draw[i] == null ){
				labels_y_to_draw[i] = "null";
			}
			if (!labels_y_to_draw[i].equals(old_label)) {
				g.drawString(labels_y_to_draw[i].toString(), 2, y);
				old_label = labels_y_to_draw[i];
			}
			if (selected_y[i]) {
				g.drawLine(offset_x_left - 3, y - 1, offset_x_left - 3, y - y_width + 1);
			}
			g.drawLine(offset_x_left - 2, y, offset_x_left + true_width, y);
			y += y_width;
		}
		// Labels ready

		// Draw legend
		x = getWidth() - offset_x_right + 5 - legend_x_width;
		for (int i = 0; i < true_height; i++) {
			y = offset_y_top + i;
			g.setColor(ColorUtils.getScaledColor((true_height - i) * (getIntervallSize() / true_height), min, max, color_low, color_high, this.color_scale));
			g.fillRect(x, y, legend_x_width, 1);
		}
		g.setColor(Color.BLACK);
		g.drawRect(x, offset_y_top, legend_x_width, true_height);
		g.drawString(Double.toString(DoubleUtils.crop(getMaxValue(), 1)), x + legend_x_width + 2, 15);
		g.drawString(Double.toString(DoubleUtils.crop(getMinValue(), 1)), x + legend_x_width + 2, true_height);


		// Tooltip
		if (tooltip_point != null) {
			int current_idx_x = getIdxByPointX(tooltip_point.getX());
			int current_idx_y = getIdxByPointY(tooltip_point.getY());
			if (current_idx_x != -1 && current_idx_y != -1) { // In heatmap
				g.drawString(Double.toString(data_points_to_draw[current_idx_x][current_idx_y]), (int) (tooltip_point.
						getX() + 2), (int) (tooltip_point.getY() - 2));
			}
			else if (tooltip_point.getX() >= x) { // In Legend
				g.drawString(Double.toString((true_height - (tooltip_point.getY() - offset_y_top)) * (getIntervallSize() / true_height)), (int) (tooltip_point.
						getX() + 2), (int) (tooltip_point.getY() - 2));
			}
		}
	}

	private void calculateOffsets(Graphics g) {
		FontMetrics metrics = g.getFontMetrics();

		// Calculate xoffset left
		offset_x_left = 0;
		for (Object labely : labels_y_to_draw) {
			int length = labely != null ? metrics.stringWidth(labely.toString()) : metrics.stringWidth("null");
			offset_x_left = offset_x_left > length ? offset_x_left : length;
		}
		offset_x_left += 5;

		// Calculate x offset right
		offset_x_right = metrics.stringWidth(Double.toString(getMaxValue()));
		if (offset_x_right < metrics.stringWidth(Double.toString(min))) {
			offset_x_right = metrics.stringWidth(Double.toString(min));
		}
		offset_x_right += 5;
	}

	public void calcDtd() {
		// Check if a calculation update is needed
		if (data_points_to_draw != null) {
			return;
		}

		switch (type) {
			case Raw:
				calcDtdRaw();
				break;
			case LabelXMean:
				calcDtdMeanX();
				break;
			case LabelYMean:
				calcDtdMeanY();
				break;
			default:
				throw new RuntimeException("Can not calculate for type: " + type);
		}


		if (getNormalizeData()) {
			normalizeDatapoints();
		}
		else {
			calculateMaxMinValues();
		}

		clearSelection();
		resetZoom();
	}

	private void calcDtdRaw() {
		logger.finer("Calculating raw data points");

		// Copy data matrix (Dont use System.arraycopy !!!)
		data_points_to_draw = new double[data_points.length][data_points[0].length];
		for (int x = 0; x < data_points.length; x++) {
			for (int y = 0; y < data_points[0].length; y++) {
				data_points_to_draw[x][y] = data_points[x][y];
			}
		}


		labels_x_to_draw = labels_x.clone();
		labels_y_to_draw = labels_y.clone();
	}

	private void normalizeDatapoints() {
		logger.finer("Normalizing data");

		for (int x = 0; x < data_points_to_draw.length; x++) {
			double scaling_factor = 0;

			for (int y = 0; y < data_points_to_draw[0].length; y++) {
				scaling_factor = Math.max(scaling_factor, Math.abs(data_points_to_draw[x][y]));
			}

			logger.finer("Scaling vector '" + labels_x_to_draw[x] + "' with factor: " + scaling_factor);

			for (int y = 0; y < data_points_to_draw[0].length; y++) {
				data_points_to_draw[x][y] /= scaling_factor;
			}

			setMaxValue(Math.min(max, 1));
			setMinValue(Math.max(min, -1));
		}
	}

	private void calcDtdMeanX() {
		logger.finer("Calculation mean x values");
		LinkedList<Object> names = new LinkedList<Object>();
		LinkedList<LinkedList<double[]>> data_list = new LinkedList<LinkedList<double[]>>();

		for (int i = 0; i < data_points.length; i++) {
			int idx = names.indexOf(labels_x[i]);
			LinkedList<double[]> points = null;

			if (idx < 0) {
				points = new LinkedList<double[]>();
				names.add(labels_x[i]);
				data_list.add(points);
			}
			else {
				points = data_list.get(idx);
			}

			points.add(data_points[i]);
		}

		data_points_to_draw = new double[names.size()][getSizeY()];
		labels_x_to_draw = names.toArray(new Object[names.size()]);

		for (int idx = 0; idx < labels_x_to_draw.length; idx++) {
			double[] sum = new double[getSizeY()];
			LinkedList<double[]> points = data_list.get(idx);
			for (double[] values : points) {
				for (int i = 0; i < values.length; i++) {
					sum[i] += values[i];
				}
			}
			for (int i = 0; i < sum.length; i++) {
				sum[i] /= points.size();
			}
			data_points_to_draw[idx] = sum;
		}
	}

	private void calcDtdMeanY() {
		logger.finer("Calculation mean y values");
		LinkedList<Object> names = new LinkedList<Object>();
		LinkedList<LinkedList<double[]>> data_list = new LinkedList<LinkedList<double[]>>();

		for (int i = 0; i < getSizeY(); i++) {
			int idx = names.indexOf(labels_y[i]);
			LinkedList<double[]> points = null;

			if (idx < 0) {
				points = new LinkedList<double[]>();
				names.add(labels_y[i]);
				data_list.add(points);
			}
			else {
				points = data_list.get(idx);
			}

			double[] y_values = new double[getSizeX()];
			for (int j = 0; j < y_values.length; j++) {
				y_values[j] = data_points[j][i];
			}
			points.add(y_values);
		}

		data_points_to_draw = new double[getSizeX()][names.size()];
		labels_y_to_draw = names.toArray(new Object[names.size()]);
		labels_x_to_draw = labels_x;

		for (int idx = 0; idx < labels_y_to_draw.length; idx++) {
			double[] sum = new double[getSizeX()];
			LinkedList<double[]> points = data_list.get(idx);
			for (double[] values : points) {
				for (int i = 0; i < values.length; i++) {
					sum[i] += values[i];
				}
			}
			for (int i = 0; i < sum.length; i++) {
				data_points_to_draw[i][idx] = sum[i] / points.size();
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			int x = getIdxByPointX(e.getX());
			int y = getIdxByPointY(e.getY());
			if (x >= 0 && y >= 0) {
				if (e.isControlDown()) {
					selected_x[x] = !selected_x[x];
					selected_y[y] = !selected_y[y];
					fireHeatmapSelectionEvent(data_points_to_draw[x][y], labels_x_to_draw[x], labels_y_to_draw[y], e.
							getPoint());
				}
				else if (e.getClickCount() > 1) {
					clearSelection();
					selected_x[x] = true;
					selected_y[y] = true;
					fireHeatmapDoubleClickEvent(data_points_to_draw[x][y], labels_x_to_draw[x], labels_y_to_draw[y], e.
							getPoint());
				}
				else {
					clearSelection();
					selected_x[x] = true;
					selected_y[y] = true;
					fireHeatmapClickEvent(data_points_to_draw[x][y], labels_x_to_draw[x], labels_y_to_draw[y], e.
							getPoint());
				}
			}
			repaint();
		}
		else if (e.getButton() == MouseEvent.BUTTON3) {
			new HeatmapMenu(this, e.getPoint()).show(this, e.getX(), e.getY());
		}
	}

	/**
	 * Returns the x index of {@code data_points_to_draw} corresponding to
	 * the x location. Will return -1 the location is outside the heatmap.
	 * @param x
	 * @return
	 */
	private int getIdxByPointX(double x) {
		double true_x = x - offset_x_left;
		if (true_x < 0) {
			return -1;
		}
		//logger.finer("DEBUG true_x: "+true_x);
		double full_width = getWidth() - offset_x_left - offset_x_right - legend_x_width + 5;
		//logger.finer("DEBUG full_width: "+full_width);
		if (true_x > full_width) {
			return -1;
		}
		double cell_width = Math.floor(full_width / countLabelsToDrawX());
		//logger.finer("DEBUG cell_width: "+cell_width);
		int idx = (int) Math.floor(true_x / cell_width) + labels_x_first;
		return idx >= 0 && idx < data_points_to_draw.length ? idx : -1;
	}

	/**
	 * Returns the y index of {@code data_points_to_draw[} corresponding to
	 * the y location. Will return -1 the location is outside the heatmap.
	 * @param y
	 * @return
	 */
	private int getIdxByPointY(double y) {
		double true_y = y - offset_y_top;
		//logger.finer("DEBUG true_y: " + true_y);
		if (true_y < 0) {
			return -1;
		}
		double true_height = getHeight() - offset_y_top - offset_y_bottom;
		//logger.finer("DEBUG true_height: " + true_height);
		if (true_y > true_height) {
			return -1;
		}
		double cell_height = Math.floor(true_height / data_points_to_draw[0].length);
		//logger.finer("DEBUG cell_height: " + cell_height);
		int idx = (int) Math.floor(true_y / cell_height) + labels_y_first;
		return idx >= 0 && idx < data_points_to_draw[0].length ? idx : -1;
	}

	public void mousePressed(MouseEvent e) {
		; // ignore
	}

	public void mouseReleased(MouseEvent e) {
		if (drag_event != null) {
			int start_idx_x = getIdxByPointX(drag_event.getX());
			int start_idx_y = getIdxByPointY(drag_event.getY());
			int end_idx_x = getIdxByPointX(e.getX());
			int end_idx_y = getIdxByPointY(e.getY());

			if (start_idx_x > end_idx_x) {
				int tmp = start_idx_x;
				start_idx_x = end_idx_x;
				end_idx_x = tmp;
			}
			if (start_idx_y > end_idx_y) {
				int tmp = start_idx_y;
				start_idx_y = end_idx_y;
				end_idx_y = tmp;
			}

			if (drag_event.isControlDown() && e.isControlDown()) {
				// Select
				start_idx_x = Math.max(start_idx_x, 0);
				end_idx_x = Math.min(end_idx_x, data_points_to_draw.length);
				start_idx_y = Math.max(start_idx_y, 0);
				end_idx_y = Math.min(end_idx_y, data_points_to_draw[0].length);

				clearSelection();
				for (int idx = start_idx_x; idx <= end_idx_x; idx++) {
					selected_x[idx] = true;
				}
				for (int idx = start_idx_y; idx <= end_idx_y; idx++) {
					selected_y[idx] = true;
				}
				fireHeatmapSelectionEvent(data_points_to_draw[start_idx_x][start_idx_y], labels_x_to_draw[start_idx_x], labels_y_to_draw[start_idx_y], e.
						getPoint());

			}
			else {
				// zoom
				setZoom(start_idx_x, end_idx_x, start_idx_y, end_idx_y);
			}
			drag_event = null;


			repaint();
		}
		setCursor(Cursor.getDefaultCursor());
	}

	public void mouseEntered(MouseEvent e) {
		; // ignore
	}

	public void mouseExited(MouseEvent e) {
		tooltip_point = null;
		repaint();
	}

	public void mouseDragged(MouseEvent e) {
		if (drag_event == null) {
			logger.finer("Drag started");
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			this.drag_event = e;
		}
	}

	public void mouseMoved(MouseEvent e) {
		tooltip_point = e.getPoint();
		repaint();
	}

	private void updateColorDistance() {
		int r1 = getColorLow().getRed();
		int g1 = getColorLow().getGreen();
		int b1 = getColorLow().getBlue();
		int r2 = getColorHigh().getRed();
		int g2 = getColorHigh().getGreen();
		int b2 = getColorHigh().getBlue();

		color_distance = Math.abs(r1 - r2);
		color_distance += Math.abs(g1 - g2);
		color_distance += Math.abs(b1 - b2);
	}

	public void addHeatmapListener(HeatmapListener l) {
		listener.add(l);
	}

	public void removeHeatmapListener(HeatmapListener l) {
		listener.remove(l);
	}

	public void fireHeatmapClickEvent(double value, Object x, Object y, Point p) {
		HeatmapEvent event = new HeatmapEvent(this, value, x, y, p);
		logger.finer("Fire heatmap click event: " + event);
		for (HeatmapListener l : listener) {
			l.heatmapClicked(event);
		}
	}

	public void fireHeatmapDoubleClickEvent(double value, Object x, Object y, Point p) {
		HeatmapEvent event = new HeatmapEvent(this, value, x, y, p);
		logger.finer("Fire heatmap double-click event: " + event);
		for (HeatmapListener l : listener) {
			l.heatmapDoubleClicked(event);
		}
	}

	public void fireHeatmapSelectionEvent(double value, Object x, Object y, Point p) {
		HeatmapEvent event = new HeatmapEvent(this, value, x, y, p);
		logger.finer("Fire heatmap selection change event: " + event);
		for (HeatmapListener l : listener) {
			l.heatmapSelectionChanged(event);
		}
	}

	public void clusterData() {
		clusterData(data_points_to_draw.length);
	}

	public void clusterData(int no_of_clusters) {
		logger.finer("Preparing data to cluster");
		SOM som = new SOM(no_of_clusters, 1, SOM.GridType.HEXAGONAL, 50, 0.1, 1, SOM.LearningType.LINEAR, SOM.NeighbourhoodFunction.GAUSSIAN);
		Dataset ds = new DefaultDataset();

		int[] label_to_id_mapping = new int[countLabelsToDrawX()];

		for (int i = labels_x_first; i <= labels_x_last; i++) {
			Instance ins = new DenseInstance(data_points_to_draw[i]);
			label_to_id_mapping[i - labels_x_first] = ins.getID();
			ds.add(ins);
		}


		logger.finer("Performing clustering");
		Dataset[] cluster = som.cluster(ds);

		logger.finer("Resorting");
		Object[] sorted_label = new Object[label_to_id_mapping.length];
		double[][] sorted_data = new double[label_to_id_mapping.length][data_points_to_draw[0].length];
		int new_idx = 0;

		for (Dataset c : cluster) {
			for (int i = 0; i < c.size(); i++) {
				int idx = ArrayUtils.indexOf(label_to_id_mapping, c.get(i).getID());
				sorted_label[new_idx] = labels_x_to_draw[idx];
				sorted_data[new_idx] = data_points_to_draw[idx];
				new_idx++;
			}
		}

		System.arraycopy(sorted_label, 0, labels_x_to_draw, labels_x_first, sorted_label.length);
		System.arraycopy(sorted_data, 0, data_points_to_draw, labels_x_first, sorted_data.length);

		repaint();
	}

	public int[] getSelectedIndizesX() {
		LinkedList<Integer> selected = new LinkedList<Integer>();
		for (int idx = 0; idx < selected_x.length; idx++) {
			if (selected_x[idx]) {
				selected.add(idx);
			}
		}
		int[] ret = new int[selected.size()];
		int idx = 0;
		for (Integer i : selected) {
			ret[idx++] = i.intValue();
		}
		return ret;
	}

	public int[] getSelectedIndizesY() {
		LinkedList<Integer> selected = new LinkedList<Integer>();
		for (int idx = 0; idx < selected_y.length; idx++) {
			if (selected_y[idx]) {
				selected.add(idx);
			}
		}
		int[] ret = new int[selected.size()];
		int idx = 0;
		for (Integer i : selected) {
			ret[idx++] = i.intValue();
		}
		return ret;
	}

	public Object[] getSelectedLabelX() {
		int[] indizes = getSelectedIndizesX();
		Object[] selected_label = new Object[indizes.length];
		for (int idx = 0; idx < indizes.length; idx++) {
			selected_label[idx] = labels_x_to_draw[indizes[idx]];
		}
		return selected_label;
	}

	public Object[] getSelectedLabelY() {
		int[] indizes = getSelectedIndizesY();
		Object[] selected_label = new Object[indizes.length];
		for (int idx = 0; idx < indizes.length; idx++) {
			selected_label[idx] = labels_y_to_draw[indizes[idx]];
		}
		return selected_label;
	}

	private void clearSelection() {
		selected_x = new boolean[labels_x_to_draw.length];
		selected_y = new boolean[labels_y_to_draw.length];

	}

	public void setZoom(int start_idx_x, int end_idx_x, int start_idx_y, int end_idx_y) {
		setLabelXFirst(Math.max(start_idx_x, 0));
		setLabelXLast(Math.min(end_idx_x, data_points_to_draw.length));
		setLabelYFirst(Math.max(start_idx_y, 0));
		setLabelYLast(Math.min(end_idx_y, data_points_to_draw[0].length));
	}
}
