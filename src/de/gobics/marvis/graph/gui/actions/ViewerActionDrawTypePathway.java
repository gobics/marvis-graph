/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.Enzyme;
import de.gobics.marvis.graph.Pathway;
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
public class ViewerActionDrawTypePathway extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionDrawTypePathway(final VisualizationViewerGraph viewer, final GraphViewCustomizable view) {
		super("Draw pathways", "pathway.png");
		this.viewer = viewer;
		this.view = view;
		putValue(SELECTED_KEY, Boolean.TRUE);
		putValue(LONG_DESCRIPTION, "Select this to display pathway nodes");
		
		
		view.addGraphViewListener(new GraphViewListener() {
			@Override
			public void graphChanged(GraphView parent, GraphChangeType type) {
				putValue(SELECTED_KEY, view.getDisplayType(Pathway.class).equals(GraphViewCustomizable.DisplayType.All));
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (getValue(SELECTED_KEY) != null && ((Boolean) getValue(SELECTED_KEY)).booleanValue()) {
			view.hideClass(Pathway.class, false);
		}
		else {
			view.hideClass(Pathway.class, true);
		}
	}
}