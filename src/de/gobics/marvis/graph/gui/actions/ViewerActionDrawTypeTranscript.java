/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.Transcript;
import de.gobics.marvis.graph.graphview.GraphViewCustomizable;
import de.gobics.marvis.graph.gui.VisualizationViewerGraph;
import java.awt.event.ActionEvent;

/**
 *
 * @author manuel
 */
public class ViewerActionDrawTypeTranscript extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionDrawTypeTranscript(final VisualizationViewerGraph viewer, GraphViewCustomizable view) {
		super("Draw transcripts", "transcript.png");
		this.viewer = viewer;
		this.view = view;
		putValue(LONG_DESCRIPTION, "Select this to display transcript nodes");
		
		if( ! view.parentContains(Transcript.class) ){
			putValue(SELECTED_KEY, false);
			view.hideType(Transcript.class);
		}
		else {
			putValue(SELECTED_KEY, true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if( getValue(SELECTED_KEY) != null && ((Boolean)getValue(SELECTED_KEY)).booleanValue() ){
			view.showType(Transcript.class);
		} else {
			view.hideType(Transcript.class);
		}
		viewer.updateGraphLayout();
	}
}