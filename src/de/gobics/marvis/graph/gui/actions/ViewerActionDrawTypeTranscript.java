/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.Enzyme;
import de.gobics.marvis.graph.Transcript;
import de.gobics.marvis.graph.graphview.GraphView;
import de.gobics.marvis.graph.graphview.GraphViewCustomizable;
import de.gobics.marvis.graph.graphview.GraphViewListener;
import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import java.awt.event.ActionEvent;

/**
 *
 * @author manuel
 */
public class ViewerActionDrawTypeTranscript extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionDrawTypeTranscript(final VisualizationViewerGraph viewer, final GraphViewCustomizable view) {
		super("Draw transcripts", "transcript.png");
		this.viewer = viewer;
		this.view = view;
		putValue(LONG_DESCRIPTION, "Select this to display transcript nodes");

		if (!view.getMetabolicNetwork().hasTranscripts()) {
			putValue(SELECTED_KEY, false);
			view.hideClass(Transcript.class, true);
		}
		else {
			putValue(SELECTED_KEY, true);
		}

		view.addGraphViewListener(new GraphViewListener() {
			@Override
			public void graphChanged(GraphView parent) {
				putValue(SELECTED_KEY, view.getDisplayType(Transcript.class).equals(GraphViewCustomizable.DisplayType.All));
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (getValue(SELECTED_KEY) != null && ((Boolean) getValue(SELECTED_KEY)).booleanValue()) {
			view.hideClass(Transcript.class, false);
		}
		else {
			view.hideClass(Transcript.class, true);
		}
	}
}