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
public class ViewerActionDrawTypeEnzyme extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionDrawTypeEnzyme(final VisualizationViewerGraph viewer, final GraphViewCustomizable gview) {
		super("Draw enzymes", "enzyme.png");
		this.viewer = viewer;
		this.view = gview;
		putValue(LONG_DESCRIPTION, "Select this to display enzyme nodes");
		if (!view.getMetabolicNetwork().hasTranscripts()) {
			putValue(SELECTED_KEY, false);
			view.hideClass(Enzyme.class, true);
		}
		else {
			putValue(SELECTED_KEY, true);
		}

		view.addGraphViewListener(new GraphViewListener() {
			@Override
			public void graphChanged(GraphView parent) {
				putValue(SELECTED_KEY, view.drawsType(Enzyme.class));
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (getValue(SELECTED_KEY) != null && ((Boolean) getValue(SELECTED_KEY)).
				booleanValue()) {
			view.hideClass(Enzyme.class, false);
		}
		else {
			view.hideClass(Enzyme.class, true);
		}
	}
}