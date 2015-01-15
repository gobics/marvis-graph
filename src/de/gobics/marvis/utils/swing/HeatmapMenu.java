package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.ColorUtils;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class HeatmapMenu extends JPopupMenu implements ActionListener {

	private static final Logger logger = Logger.getLogger(HeatmapMenu.class.getName());
	private final Point relative_location;
	private final Heatmap parent;
	private final JMenuItem type_normal = new JMenuItem("Raw"),
			type_x_mean = new JMenuItem("X Mean"),
			type_y_mean = new JMenuItem("Y Mean"),
			color_high = new JMenuItem("Select maximum color..."),
			color_low = new JMenuItem("Select minimum color..."),
			scale_normalize_local = new JCheckBoxMenuItem("Normalize vectors local"),
			scale_lin = new JMenuItem("Linear"),
			scale_quad = new JMenuItem("Quadratic"),
			scale_log = new JMenuItem("Logarithmic"),
			scale_free = new JMenuItem("Userdefined ..."),
			value_max = new JMenuItem("Set maximum value ..."),
			value_min = new JMenuItem("Set minimum value ..."),
			value_default = new JMenuItem("Reset max/min values"),
			zoom_x_first = new JMenuItem("Select first X label..."),
			zoom_x_last = new JMenuItem("Select last X label..."),
			zoom_y_first = new JMenuItem("Select first Y label..."),
			zoom_y_last = new JMenuItem("Select last Y label..."),
			zoom_reset = new JMenuItem("Reset"),
			write_to_file = new JMenuItem("Export graphic..."),
			cluster_som = new JMenuItem("Cluster datapoints...");

	public HeatmapMenu(Heatmap parent, Point location) {
		this.parent = parent;

		JMenu sub = new JMenu("Display");
		add(sub);
		sub.add(type_normal);
		type_normal.addActionListener(this);
		sub.add(type_x_mean);
		type_x_mean.addActionListener(this);
		sub.add(type_y_mean);
		type_y_mean.addActionListener(this);

		sub = new JMenu("Scaling");
		add(sub);
		sub.add(scale_normalize_local);
		scale_normalize_local.addActionListener(this);
		scale_normalize_local.setSelected(parent.getNormalizeData());
		sub.addSeparator();
		sub.add(scale_lin);
		scale_lin.addActionListener(this);
		sub.add(scale_log);
		scale_log.addActionListener(this);
		sub.add(scale_quad);
		scale_quad.addActionListener(this);
		sub.add(scale_free);
		scale_free.addActionListener(this);

		sub = new JMenu("Max/Min Values");
		add(sub);
		sub.add(value_max);
		value_max.addActionListener(this);
		sub.add(value_min);
		value_min.addActionListener(this);
		sub.add(value_default);
		value_default.addActionListener(this);

		sub = new JMenu("Zoom");
		add(sub);
		sub.add(zoom_x_first);
		zoom_x_first.addActionListener(this);
		sub.add(zoom_x_last);
		zoom_x_last.addActionListener(this);
		sub.add(zoom_y_first);
		zoom_y_first.addActionListener(this);
		sub.add(zoom_y_last);
		zoom_y_last.addActionListener(this);
		sub.add(zoom_reset);
		zoom_reset.addActionListener(this);

		sub = new JMenu("Colormap");
		add(sub);
		sub.add(color_high);
		color_high.addActionListener(this);
		sub.add(color_low);
		color_low.addActionListener(this);


		add(write_to_file);
		write_to_file.addActionListener(this);

		add(cluster_som);
		cluster_som.addActionListener(this);

		this.relative_location = location;
	}

	public void actionPerformed(ActionEvent e) {
		if (type_normal.equals(e.getSource())) {
			parent.setHistogramType(Heatmap.HistogramType.Raw);
		} else if (type_x_mean.equals(e.getSource())) {
			parent.setHistogramType(Heatmap.HistogramType.LabelXMean);
		} else if (type_y_mean.equals(e.getSource())) {
			parent.setHistogramType(Heatmap.HistogramType.LabelYMean);
		} else if (scale_normalize_local.equals(e.getSource())) {
			parent.setNormalizeData(scale_normalize_local.isSelected());
		} else if (scale_lin.equals(e.getSource())) {
			parent.setColorScale(ColorUtils.ScalingType.Normal);
		} else if (scale_quad.equals(e.getSource())) {
			parent.setColorScale(ColorUtils.ScalingType.Quadratic);
		} else if (scale_log.equals(e.getSource())) {
			parent.setColorScale(ColorUtils.ScalingType.Logarithm);
		} else if (scale_free.equals(e.getSource())) {
			DialogSpinner d = new DialogSpinner(null, parent.getColorScale(), Double.MIN_VALUE, Double.MAX_VALUE, 0.1);
			if (d.showDialog()) {
				parent.setColorScale(((Number) d.getValue()).doubleValue());
			}
		} else if (value_max.equals(e.getSource())) {
			DialogSpinner d = new DialogSpinner(null, parent.getMaxValue(), -1 * Double.MAX_VALUE, Double.MAX_VALUE, 0.1);
			if (d.showDialog()) {
				parent.setMaxValue(((Number) d.getValue()).doubleValue());
			}
		} else if (value_min.equals(e.getSource())) {
			DialogSpinner d = new DialogSpinner(null, parent.getMinValue(), -1 * Double.MAX_VALUE, Double.MAX_VALUE, 0.1);
			if (d.showDialog()) {
				parent.setMinValue(((Number) d.getValue()).doubleValue());
			}
		} else if (value_default.equals(e.getSource())) {
			parent.calculateMaxMinValues();
		} else if (color_high.equals(e.getSource())) {
			Color new_color = JColorChooser.showDialog(parent, "Select maximum color", parent.getColorHigh());
			parent.setColorHigh(new_color);
		} else if (color_low.equals(e.getSource())) {
			Color new_color = JColorChooser.showDialog(parent, "Select minimum color", parent.getColorLow());
			parent.setColorLow(new_color);

		} else if (write_to_file.equals(e.getSource())) {
			DialogSaveGraphic d = new DialogSaveGraphic(null, parent);
			if (d.showDialog()) {
				d.writeToFileTried();
			}
		} else if (zoom_x_first.equals(e.getSource())) {
			DialogSelectLabel sl = new DialogSelectLabel(null, parent.getLabelsX(), parent.getLabelX(relative_location));
			if (sl.showDialog() && sl.getSelectedLabel() != null) {
				parent.setLabelXFirst(sl.getSelectedLabel());
			}
		} else if (zoom_x_last.equals(e.getSource())) {
			DialogSelectLabel sl = new DialogSelectLabel(null, parent.getLabelsX(), parent.getLabelX(relative_location));
			if (sl.showDialog() && sl.getSelectedLabel() != null) {
				parent.setLabelXLast(sl.getSelectedLabel());
			}
		} else if (zoom_y_first.equals(e.getSource())) {
			DialogSelectLabel sl = new DialogSelectLabel(null, parent.getLabelsY(), parent.getLabelY(relative_location));
			if (sl.showDialog() && sl.getSelectedLabel() != null) {
				parent.setLabelYFirst(sl.getSelectedLabel());
			}
		} else if (zoom_y_last.equals(e.getSource())) {
			DialogSelectLabel sl = new DialogSelectLabel(null, parent.getLabelsY(), parent.getLabelY(relative_location));
			if (sl.showDialog() && sl.getSelectedLabel() != null) {
				parent.setLabelYLast(sl.getSelectedLabel());
			}
		} else if (zoom_reset.equals(e.getSource())) {
			parent.resetZoom();
		} else if (cluster_som.equals(e.getSource())) {
			DialogSpinner d = new DialogSpinner(null, parent.countLabelsToDrawX(), 1, parent.countLabelsToDrawX(), 1);
			if (d.showDialog()) {
				parent.clusterData(((Number) d.getValue()).intValue());
			}
		} else {
			logger.severe("Not yet implemented: " + e);
		}
		parent.repaint();
	}
}
