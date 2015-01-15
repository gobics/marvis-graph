/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.swing;

/**
 *
 * @author manuel
 */
public interface HeatmapListener {
	public void heatmapClicked(HeatmapEvent event);
	public void heatmapDoubleClicked(HeatmapEvent event);
	public void heatmapSelectionChanged(HeatmapEvent event);
}
