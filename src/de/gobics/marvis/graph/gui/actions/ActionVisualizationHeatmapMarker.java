/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.InternalFrameGraph;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/**
 *
 * @author manuel
 */
public class ActionVisualizationHeatmapMarker extends AbstractVisualizationAction {

	public ActionVisualizationHeatmapMarker(final InternalFrameGraph frame){
		super(frame, "Draw marker heatmap");
		putValue(SHORT_DESCRIPTION, "Creates a new tab containing the a heatmap of marker intensities");
		
		setEnabled(frame.getMetabolicNetwork().hasMarkers());
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		getInternalFrameGraph().drawHeatmapMarker();
	}	
}
