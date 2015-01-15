/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing;

import java.awt.Point;

/**
 *
 * @author manuel
 */
public class HeatmapEvent {

	private final double value;
	private final Object label_x;
	private final Object label_y;
	private final Heatmap source;
	private final Point location;

	public HeatmapEvent(Heatmap source, double v, Object x, Object y, Point location) {
		this.source = source;
		value = v;
		label_x = x;
		label_y = y;
		this.location = location;
	}

	public Heatmap getSource() {
		return source;
	}

	public Object getLabelX() {
		return label_x;
	}

	public Object getLabelY() {
		return label_y;
	}

	public double getValue() {
		return value;
	}

	public Point getLocation(){
		return location;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{value=" + value + ";label_x=" + label_x + ";label_y=" + label_y + "}";
	}
}
