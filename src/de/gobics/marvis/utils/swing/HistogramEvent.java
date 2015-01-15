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
class HistogramEvent {
	private final Histogram source;
	private final double value;
	private final Object label;
	private final Point location;

	public HistogramEvent(Histogram Source, double value, Object label, Point location){
		this.source = Source;
		this.value = value;
		this.label = label;
		this.location = location;
	}

	public Object getLabel() {
		return label;
	}

	public Point getLocation() {
		return location;
	}

	public Histogram getSource() {
		return source;
	}

	public double getValue() {
		return value;
	}

}
