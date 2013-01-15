/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.Marker;
import de.gobics.marvis.graph.graphview.GraphViewCustomizable;
import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import java.awt.event.ActionEvent;

/**
 *
 * @author manuel
 */
public class ViewerActionDrawTypeMolecule extends AbstractViewerAction {

	private final VisualizationViewerGraph viewer;
	private final GraphViewCustomizable view;

	public ViewerActionDrawTypeMolecule(final VisualizationViewerGraph viewer, GraphViewCustomizable view) {
		super("Draw compounds", "compound.png");
		this.viewer = viewer;
		this.view = view;
		putValue(SELECTED_KEY, Boolean.TRUE);
		putValue(LONG_DESCRIPTION, "Select this to display molecule nodes");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if( getValue(SELECTED_KEY) != null && ((Boolean)getValue(SELECTED_KEY)).booleanValue() ){
			view.showType(Compound.class);
		} else {
			view.hideType(Compound.class);
		}
		viewer.updateGraphLayout();
	}
}