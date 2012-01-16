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
public class ActionVisualizationHeatmapTranscript extends AbstractVisualizationAction {

	public ActionVisualizationHeatmapTranscript(final InternalFrameGraph frame){
		super(frame, "Draw transcript heatmap");
		putValue(SHORT_DESCRIPTION, "Creates a new tab containing the a heatmap of transcript intensities");
		
		setEnabled(frame.getMetabolicNetwork().hasTranscripts());
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		getInternalFrameGraph().drawHeatmapTranscript();
	}	
}
