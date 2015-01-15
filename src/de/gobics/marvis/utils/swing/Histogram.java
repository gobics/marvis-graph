package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.ArrayUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.swing.JPanel;

public class Histogram extends JPanel implements MouseListener, MouseMotionListener {

	private static final Logger logger = Logger.getLogger(Histogram.class.getName());
	private static int offset_x_left = 30;
	private static int offset_y_top = 0;
	private static int offset_y_bottom = 15;
	private static int offset_y_right = 0;
	private Point2D tooltip_point = null;

	Object getLabel(Point location) {
		int idx = getIdxByPoint(location.x);
		if (idx == -1) {
			return null;
		}
		return labels_to_draw[idx];
	}

	public static enum HistogramType {

		Normalized, LabelMean
	}
	private HistogramType type = HistogramType.Normalized;
	private double[] data_points;
	private double[] data_points_to_draw;
	private Object[] labels;
	private Object[] labels_to_draw;
	private Color[] bar_color;
	private Color[] bar_color_to_draw;
	private double y_max;
	private double y_min;
	private int x_label_first;
	private int x_label_last;
	private HashSet<HistogramListener> listener = new HashSet<HistogramListener>();

	public Histogram(double[] data) {
		setDatapoints(data);

		Integer[] ls = new Integer[data.length];
		for (int i = 0; i < data.length; i++) {
			ls[i] = new Integer(i + 1);
		}

		setLabel(ls);
		setBarColor(Color.YELLOW);

		addMouseListener(this);
		addMouseMotionListener(this);
		setMinimumSize(new Dimension(50, 50));
		setPreferredSize(new Dimension(200, 100));
	}

	public void setHistogramType(HistogramType t) {
		data_points_to_draw = null;
		type = t;
	}

	public void setDatapoints(double[] data) {
		this.data_points = data.clone();
		this.data_points_to_draw = null;
		calculateMaxMinValues();
	}

	public double getMaxValue() {
		return y_max;
	}

	public void setMaxValue(double new_max) {
		if (new_max < 0) {
			new_max = 0;
		}
		y_max = new_max;
	}

	public double getMinValue() {
		return y_min;
	}

	public void setMinValue(double new_min) {
		if (new_min > 0) {
			new_min = 0;
		}
		y_min = new_min;
	}

	public void calculateMaxMinValues() {
		y_max = 0;
		y_min = 0;
		for (double d : data_points) {
			if (d > y_max) {
				y_max = d;
			}
			if (d < y_min) {
				y_min = d;
			}
		}
	}

	public double getIntervallSize() {
		return getMaxValue() + (-1 * getMinValue());
	}

	public double[] getDatapoints() {
		return data_points.clone();
	}

	public int countDatapoints() {
		return data_points.length;
	}

	public void setLabel(Object[] label) {
		if (label.length != data_points.length) {
			throw new RuntimeException("Length of label array (" + label.length + ") has to equal data array (" + data_points.length + ")");
		}
		labels = label;
	}

	public Object[] getLabel() {
		return labels.clone();
	}

	public void setBarColor(Color c) {
		setBarColors(new Color[]{c});
	}

	public void setBarColors(Color[] colors) {
		if (colors == null || colors.length == 0) {
			colors = new Color[]{Color.YELLOW};
		}
		bar_color = colors.clone();
		data_points_to_draw = null;
	}

	public Color[] getBarColors() {
		return bar_color.clone();
	}

	public void setFirstLabel(Object label) {
		setFirstLabel(ArrayUtils.indexOf(labels_to_draw, label));
	}

	public void setFirstLabel(int index) {
		if (index >= 0 && index < labels_to_draw.length) {
			x_label_first = index;
			x_label_last = Math.max(x_label_first, x_label_last);
		}
	}

	public void setLastLabel(Object label) {
		setLastLabel(ArrayUtils.lastIndexOf(labels_to_draw, label));
	}

	public void setLastLabel(int index) {
		if (index >= 0 && index < labels_to_draw.length) {
			x_label_last = index;
			x_label_first = Math.min(x_label_first, x_label_last);
		}
	}

	private int countBarsToDraw() {
		return x_label_last - x_label_first + 1;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Ensure the calculated values are available
		calcDtd();

		// Calculate sizes of the historgram
		int width = getWidth() - offset_x_left - offset_y_right;
		int height = getHeight() - offset_y_top - offset_y_bottom;
		int no_datapoints = data_points_to_draw.length > 0 ? countBarsToDraw() : 1;
		int barWidth = (int) Math.floor((width - 2) / no_datapoints);

		// Fill the background of the histogram
		g.setColor(Color.WHITE);
		g.fillRect(offset_x_left, offset_y_top, barWidth * no_datapoints, height);
		g.setColor(Color.BLACK);

		g.drawString(Integer.toString(new Double(getMaxValue()).intValue()), 2, 10);
		g.drawString(Integer.toString(new Double(getMinValue()).intValue()), 2, getHeight() - 15);

		if (countBarsToDraw() < 1) {
			g.drawString("No data given", offset_x_left + 5, offset_y_top + 20);
			return;
		}

		// Calculate the scaling factor and the position of 0-line
		double scale_factor = height / (-1 * getMinValue() + getMaxValue());
		double null_offset = 0;
		if (getMinValue() < 0) {
			null_offset = scale_factor * (-1 * getMinValue());
		}
		g.drawLine(offset_x_left - 5, (int) (height - null_offset), offset_x_left + barWidth * no_datapoints, (int) (height - null_offset));


		// x is the x-coord of the current bar
		int x = offset_x_left;
		Object old_label = labels_to_draw[0];
		for (int i = x_label_first; i <= x_label_last; i++) {
			// Create label separator on-step-ahead to ensure the bar-lines are
			// colored black.
			if (i + 1 < labels_to_draw.length && !labels_to_draw[i + 1].equals(old_label)) {
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine(x + barWidth, 0, x + barWidth, height);
				old_label = labels_to_draw[i + 1];
			}
			int barHeight = (int) (data_points_to_draw[i] * scale_factor);
			int barStartY = (int) (height - barHeight - null_offset);
			if (data_points_to_draw[i] < 0) { // If datapoint is neg adapt start and height
				barStartY += barHeight;
				barHeight *= -1;
			}
			g.setColor(bar_color_to_draw[i]);
			g.fillRect(x, barStartY, barWidth, barHeight);
			g.setColor(Color.BLACK);
			g.drawRect(x, barStartY, barWidth, barHeight);
			x += barWidth;
		}

		g.drawRect(offset_x_left, offset_y_top, barWidth * countBarsToDraw(), height);

		// Display names and speparators
		x = offset_x_left;
		g.setColor(Color.BLACK);
		old_label = null;
		for (int i = x_label_first; i <= x_label_last; i++) {
			if (!labels_to_draw[i].equals(old_label)) {
				g.drawString(labels_to_draw[i].toString(), x + 2, getHeight() - 2);
				old_label = labels_to_draw[i];
			}
			x += barWidth;
		}

		if (tooltip_point != null) {
			int bar_with_tooltip = getIdxByPoint(tooltip_point.getX());
			if (bar_with_tooltip != -1) {
				String tooltip_text = labels_to_draw[bar_with_tooltip] +": "+Double.toString(data_points_to_draw[bar_with_tooltip]);
				FontMetrics metrics = g.getFontMetrics();
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect((int) tooltip_point.getX(), (int) tooltip_point.getY() - metrics.getHeight(), metrics.stringWidth(tooltip_text)+5, metrics.getHeight());
				g.setColor(Color.BLACK);
				g.drawString(tooltip_text, (int) (tooltip_point.getX() + 2), (int) (tooltip_point.getY() - 2));
				g.drawRect((int) tooltip_point.getX(), (int) tooltip_point.getY() - metrics.getHeight(), metrics.stringWidth(tooltip_text)+5, metrics.getHeight());
			}

		}
	}

	public void calcDtd() {
		// Check if a calculation update is needed
		if (data_points_to_draw != null) {
			return;
		}

		switch (type) {
			case Normalized:
				calcDtdNormalized();
				break;
			case LabelMean:
				calcDtdMean();
				break;
			default:
				throw new RuntimeException("Can not calculate for type: " + type);
		}

		Object old_label = null;
		int cidx = -1;
		bar_color_to_draw = new Color[data_points_to_draw.length];
		for (int i = 0; i < data_points_to_draw.length; i++) {
			if (!labels_to_draw[i].equals(old_label)) {
				cidx = (cidx + 1) % bar_color.length;
				old_label = labels_to_draw[i];
			}
			bar_color_to_draw[i] = bar_color[cidx];
		}

		resetZoom();
	}

	private void calcDtdNormalized() {
		logger.finer("Calculating normalized data points");
		data_points_to_draw = data_points;
		labels_to_draw = labels.clone();
	}

	private void calcDtdMean() {
		logger.finer("Calculation mean values");
		LinkedList<Object> names = new LinkedList<Object>();
		LinkedList<LinkedList<Double>> data_list = new LinkedList<LinkedList<Double>>();

		for (int i = 0; i < data_points.length; i++) {
			int idx = names.indexOf(labels[i]);
			LinkedList<Double> points = null;

			if (idx < 0) {
				points = new LinkedList<Double>();
				names.add(labels[i]);
				data_list.add(points);
			} else {
				points = data_list.get(idx);
			}

			points.add(new Double(data_points[i]));
		}

		data_points_to_draw = new double[names.size()];
		labels_to_draw = names.toArray(new Object[names.size()]);

		for (int idx = 0; idx < labels_to_draw.length; idx++) {
			double sum = 0;
			LinkedList<Double> points = data_list.get(idx);
			for (Double d : points) {
				sum += d.doubleValue();
			}
			data_points_to_draw[idx] = sum / points.size();
		}
	}

	private int getIdxByPoint(double x) {
		double true_x = x - offset_x_left;
		if (true_x < 0) {
			return -1;
		}
		int true_width = getWidth() - offset_x_left;
		if (true_x > true_width) {
			return -1;
		}
		double x_width = Math.floor(true_width / countBarsToDraw());
		int bucket = (int) Math.floor(true_x / x_width) + x_label_first;
		return bucket >= 0 && bucket < data_points_to_draw.length ? bucket : -1;
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			int idx = getIdxByPoint(e.getX());
			if (idx >= 0 && idx < data_points_to_draw.length) {
				fireHistogramClickedEvent(data_points_to_draw[idx], labels_to_draw[idx], e.getPoint());
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			new HistogramMenu(this, e.getPoint()).show(this, e.getX(), e.getY());
		}
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		; // ignore
	}

	public void mouseReleased(MouseEvent e) {
		; // ignore
	}

	public void mouseEntered(MouseEvent e) {
		; // ignore
	}

	public void mouseExited(MouseEvent e) {
		tooltip_point = null;
	}

	public void mouseDragged(MouseEvent e) {
		; // Ignore
	}

	public void mouseMoved(MouseEvent e) {
		tooltip_point = e.getPoint();
		repaint();
	}

	public void addHistogramListener(HistogramListener l) {
		listener.add(l);
	}

	public void removeHistogramListener(HistogramListener l) {
		listener.remove(l);
	}

	public void fireHistogramClickedEvent(double value, Object label, Point location) {
		HistogramEvent event = new HistogramEvent(this, value, label, location);
		for (HistogramListener l : listener) {
			l.histogramClicked(event);
		}
	}

	void resetZoom() {
		x_label_first = 0;
		x_label_last = labels_to_draw.length - 1;
	}
}
