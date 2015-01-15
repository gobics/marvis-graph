/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author manuel
 */
public class HistogramMenu extends JPopupMenu implements ActionListener {

	private static final Logger logger = Logger.getLogger(HistogramMenu.class.getName());
	private final Histogram parent;
	private final Point relative_location;
	private final JMenuItem type_normal = new JMenuItem("Normalized"),
			type_label_mean = new JMenuItem("Label Mean"),
			colors = new JMenuItem("Select bar colors..."),
			zoom_first_label = new JMenuItem("First bar..."),
			zoom_last_label = new JMenuItem("Last bar..."),
			zoom_reset = new JMenuItem("Reset zoom"),
			write_to_file = new JMenuItem("Write to file...");

	public HistogramMenu(Histogram parent, Point location) {
		this.parent = parent;

		add(type_normal);
		type_normal.addActionListener(this);
		add(type_label_mean);
		type_label_mean.addActionListener(this);

		addSeparator();

		add(zoom_first_label);
		zoom_first_label.addActionListener(this);
		add(zoom_last_label);
		zoom_last_label.addActionListener(this);
		add(zoom_reset);
		zoom_reset.addActionListener(this);

		addSeparator();

		add(colors);
		colors.addActionListener(this);

		addSeparator();

		add(write_to_file);
		write_to_file.addActionListener(this);
		this.relative_location = location;
	}

	public void actionPerformed(ActionEvent e) {
		if (type_normal.equals(e.getSource())) {
			parent.setHistogramType(Histogram.HistogramType.Normalized);
		} else if (type_label_mean.equals(e.getSource())) {
			parent.setHistogramType(Histogram.HistogramType.LabelMean);
		} else if (colors.equals(e.getSource())) {
			Color[] former_color = parent.getBarColors();
			LinkedList<Color> new_color = new LinkedList<Color>();

			Color selected = null;
			int new_size = -1;

			while (new_size < new_color.size()) {
				new_size = new_color.size();
				if (new_size < former_color.length) {
					selected = former_color[new_size];
				}
				logger.finer("Display chooser to selected color " + (new_size + 1) + " with default: " + selected);
				selected = JColorChooser.showDialog(parent, "Select color " + (new_size + 1) + " (abort with cancel)", selected);

				if (selected != null) {
					new_color.add(selected);
				}

			}

			parent.setBarColors(new_color.toArray(new Color[new_color.size()]));
		} else if (zoom_first_label.equals(e.getSource())) {
			DialogSelectLabel select_label = new DialogSelectLabel(null, parent.getLabel(), parent.getLabel(getLocation()));
			if (select_label.showDialog() && select_label.getSelectedLabel() != null) {
				parent.setFirstLabel(select_label.getSelectedLabel());
			}

		} else if (zoom_last_label.equals(e.getSource())) {
			DialogSelectLabel select_label = new DialogSelectLabel(null, parent.getLabel(), parent.getLabel(getLocation()));
			if (select_label.showDialog() && select_label.getSelectedLabel() != null) {
				parent.setLastLabel(select_label.getSelectedLabel());
			}
		} else if( zoom_reset.equals(e.getSource())){
			parent.resetZoom();
		} else if (write_to_file.equals(e.getSource())) {
			DialogSaveGraphic d = new DialogSaveGraphic(null, parent);
			logger.finer("Showing save graphic dialog");
			if (d.showDialog()) {
				logger.finer("DialogSaveGraphic returned success");
				d.writeToFileTried();
			} else {
				logger.finer("DialogSaveGraphic returned fail");
			}
		}
		parent.repaint();
	}
}
