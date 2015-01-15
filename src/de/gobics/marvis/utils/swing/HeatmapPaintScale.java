/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.ArrayUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import javax.print.attribute.standard.MediaSize;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import org.jfree.chart.renderer.PaintScale;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class HeatmapPaintScale implements PaintScale {

	private Bound[] bounds;

	public HeatmapPaintScale(double lower_bound, double upper_bound) {
		this(lower_bound, upper_bound, Color.BLUE, Color.RED);
	}

	public HeatmapPaintScale(double lower_bound, double upper_bound, Color lower_color, Color upper_color) {
		this(lower_bound, upper_bound, lower_color, upper_color, Color.YELLOW);
	}

	public HeatmapPaintScale(double lower_bound, double upper_bound, Color lower_color, Color upper_color, Color mean_color) {
		this(
				new double[]{lower_bound, (upper_bound + lower_bound) / 2, upper_bound},
				new Color[]{lower_color, mean_color, upper_color});
	}

	public HeatmapPaintScale(double[] positions, Color[] colors) {
		setBounds(positions, colors);
	}

	/**
	 * Automatically calculate bounds for a given set of colors.
	 *
	 * @param min
	 * @param max
	 * @param colors
	 */
	final public void setBounds(double min, double max, Color[] colors) {
		double[] new_bounds = new double[colors.length];
		new_bounds[0] = min;
		double step = (max - min) / (colors.length - 1);
		for (int i = 1; i < new_bounds.length - 1; i++) {
			new_bounds[i] = new_bounds[i - 1] + step;
		}
		new_bounds[new_bounds.length - 1] = max;
		setBounds(new_bounds, colors);
	}

	final public void setBounds(double[] positions, Color[] colors) {
		if (positions.length != colors.length) {
			throw new RuntimeException("Given arrays differ in length");
		}
		TreeSet<Bound> bound_set = new TreeSet<>();
		for (int i = 0; i < positions.length; i++) {
			bound_set.add(new Bound(positions[i], colors[i]));
		}
		if (bound_set.size() < 2) {
			throw new RuntimeException("Require at least two colored positions");
		}
		bounds = bound_set.toArray(new Bound[bound_set.size()]);
		Arrays.sort(bounds);
		//System.out.println(Arrays.toString(bounds));
	}

	@Override
	public double getLowerBound() {
		return bounds[0].value;
	}

	@Override
	public double getUpperBound() {
		return bounds[bounds.length - 1].value;
	}

	public double[] getBoundPositions() {
		double[] positions = new double[bounds.length];
		for (int i = 0; i < positions.length; i++) {
			positions[i] = bounds[i].value;
		}
		return positions;
	}

	public Color[] getColors() {
		Color[] colors = new Color[bounds.length];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = bounds[i].color;
		}
		return colors;
	}

	@Override
	public Paint getPaint(double value) {
		if (value <= bounds[0].value) {
			return bounds[0].color;
		}
		for (int i = 0; i < bounds.length - 1; i++) {
			if (value < bounds[i + 1].value) {
				return getPaint(value, bounds[i].value, bounds[i + 1].value, bounds[i].color, bounds[i + 1].color);
			}
		}
		return bounds[bounds.length - 1].color;
	}

	public Paint getPaint(double value, double min, double max, Color min_color, Color max_color) {
		int r1 = min_color.getRed();
		int g1 = min_color.getGreen();
		int b1 = min_color.getBlue();
		int r2 = max_color.getRed();
		int g2 = max_color.getGreen();
		int b2 = max_color.getBlue();

		int color_distance = Math.abs(r1 - r2);
		color_distance += Math.abs(g1 - g2);
		color_distance += Math.abs(b1 - b2);

		// What proportion of the way through the possible values is that.
		double position = value - min;
		double percentPosition = position / Math.abs(max - min);

		// Which colour group does that put us in.
		//int colourPosition = (int) Math.round(color_distance * Math.pow(percentPosition, scaling));
		int colourPosition = (int) Math.round(color_distance * percentPosition);

		int r = min_color.getRed();
		int g = min_color.getGreen();
		int b = min_color.getBlue();

		// Make n shifts of the colour, where n is the colourPosition.
		for (int i = 0; i < colourPosition; i++) {
			int rDistance = r - max_color.getRed();
			int gDistance = g - max_color.getGreen();
			int bDistance = b - max_color.getBlue();

			if ((Math.abs(rDistance) >= Math.abs(gDistance))
					&& (Math.abs(rDistance) >= Math.abs(bDistance))) {
				// Red must be the largest.
				r = changeColourValue(r, rDistance);
			}
			else if (Math.abs(gDistance) >= Math.abs(bDistance)) {
				// Green must be the largest.
				g = changeColourValue(g, gDistance);
			}
			else {
				// Blue must be the largest.
				b = changeColourValue(b, bDistance);
			}
		}

		return new Color(r, g, b);
	}

	private int changeColourValue(int colourValue, int colourDistance) {
		if (colourDistance < 0) {
			return colourValue + 1;
		}
		else if (colourDistance > 0) {
			return colourValue - 1;
		}
		else {
			// This shouldn't actually happen here.
			return colourValue;
		}
	}
	
	private static class Bound implements Comparable<Bound> {

		public final double value;
		public final Color color;

		public Bound(double value, Color color) {
			this.color = color;
			this.value = value;
		}

		@Override
		public int compareTo(Bound t) {
			return Double.compare(this.value, t.value);
		}

		@Override
		public String toString() {
			return "Bound[" + value + ": " + color + "]";
		}
	}

	public static HeatmapPaintScale showOptionDialog() {
		return showOptionDialog(0, 1);
	}

	public static HeatmapPaintScale showOptionDialog(final double min, final double max) {
		return showOptionDialog(min, max, new double[]{min, max}, new Color[]{Color.BLUE, Color.RED});
	}

	public static HeatmapPaintScale showOptionDialog(final double min, final double max, HeatmapPaintScale scale) {
		return showOptionDialog(min, max, scale.getBoundPositions(), scale.getColors());
	}

	public static HeatmapPaintScale showOptionDialog(final double min, final double max, double[] old_bounds, Color[] old_colors) {
		JPanel main_panel = new JPanel(new BorderLayout());

		JButton button_add = new JButton("Add bound and color");

		main_panel.add(button_add, BorderLayout.PAGE_START);


		final JPanel color_panel = new JPanel();
		color_panel.setLayout(new BoxLayout(color_panel, BoxLayout.PAGE_AXIS));

		for (int i = 0; i < old_bounds.length; i++) {
			color_panel.add(new ColorBound(color_panel, old_bounds[i], min, max, old_colors[i]));
		}

		button_add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				color_panel.add(new ColorBound(color_panel, (max + min) / 2, min, max, Color.YELLOW));
				color_panel.updateUI();
			}
		});
		JScrollPane spane = new JScrollPane(color_panel);
		spane.setPreferredSize(new Dimension(500, 500));
		main_panel.add(spane, BorderLayout.CENTER);


		int res = JOptionPane.showConfirmDialog(null, main_panel, "Select heatmap paint scales", JOptionPane.OK_CANCEL_OPTION);
		if (res != JOptionPane.OK_OPTION) {
			return null;
		}

		double[] bounds = new double[color_panel.getComponentCount()];
		Color[] colors = new Color[color_panel.getComponentCount()];
		int idx = 0;

		for (Component comp : color_panel.getComponents()) {
			if (!(comp instanceof ColorBound)) {
				throw new RuntimeException("Can not handle: " + comp.getClass().getName());
			}
			ColorBound cb = (ColorBound) comp;
			bounds[idx] = cb.getValue();
			colors[idx] = cb.getColor();
			idx++;
		}

		System.out.println(Arrays.toString(bounds));
		System.out.println(Arrays.toString(colors));
		return new HeatmapPaintScale(bounds, colors);
	}

	private static class ColorBound extends JPanel implements ActionListener {

		private final JPanel parent;
		private final SpinnerNumberModel sm;
		private final JButton button_color;

		public ColorBound(JPanel parent, double val, double min, double max, Color color) {
			super();
			this.parent = parent;

			this.sm = new SpinnerNumberModel(val, min, max, (max - min) / 10);
			JSpinner spinner = new JSpinner(sm);
			Dimension d = spinner.getPreferredSize();
			d.width = 300;
			spinner.setMaximumSize(d);
			spinner.setPreferredSize(d);
			add(spinner);
			this.button_color = new JButton("Choose color");
			add(button_color);
			button_color.setBackground(color);
			button_color.addActionListener(this);

			JButton remove = new JButton("-");
			remove.setActionCommand("remove");
			remove.addActionListener(this);
			add(remove);
		}

		public double getValue() {
			return sm.getNumber().doubleValue();
		}

		public Color getColor() {
			return button_color.getBackground();
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			try {
				if ("remove".equals(ae.getActionCommand())) {
					parent.remove(this);
					parent.updateUI();
				}
				else {
					Color c = JColorChooser.showDialog(null, "Choose color for bound " + getValue(), getColor());
					if (c != null) {
						button_color.setBackground(c);
					}
				}
			}
			catch (Throwable e) {
				e.printStackTrace();;
			}
		}
	}
}
