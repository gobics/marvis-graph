/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.Enzyme;
import de.gobics.marvis.graph.Marker;
import de.gobics.marvis.graph.Reaction;
import de.gobics.marvis.graph.graphview.GraphView;
import de.gobics.marvis.graph.graphview.GraphViewCustomizable;
import de.gobics.marvis.graph.graphview.GraphViewListener;
import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import java.awt.event.ActionEvent;

/**
 *
 * @author manuel
 */
public class ViewerActionDrawTypeReaction extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionDrawTypeReaction(final VisualizationViewerGraph viewer, final GraphViewCustomizable view) {
		super("Draw reactions", "reaction.png");
		this.viewer = viewer;
		this.view = view;
		putValue(SELECTED_KEY, Boolean.TRUE);
		putValue(LONG_DESCRIPTION, "Select this to display reaction nodes");
		
		view.addGraphViewListener(new GraphViewListener() {
			@Override
			public void graphChanged(GraphView parent, GraphChangeType type) {
				putValue(SELECTED_KEY, view.getDisplayType(Reaction.class).equals(GraphViewCustomizable.DisplayType.All));
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if( getValue(SELECTED_KEY) != null && ((Boolean)getValue(SELECTED_KEY)).booleanValue() ){
			view.hideClass(Reaction.class, false);
		} else {
			view.hideClass(Reaction.class, true);
		}
	}
}