package de.gobics.marvis.graph.gui.graphvisualizer;

import org.apache.commons.collections15.Transformer;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.graphview.GraphViewAbstract;
import de.gobics.marvis.utils.ColorUtils;


import java.awt.*;

public class VertexTransformerFill implements Transformer<GraphObject, Paint> {
	private final GraphViewAbstract view;
	
	public VertexTransformerFill(GraphViewAbstract parent_graph){
		this.view = parent_graph;
	}


	@Override
	public Paint transform(GraphObject o) {
		
		if( o instanceof Marker)
			return Color.LIGHT_GRAY;
		if( o instanceof Compound )
			return getColor(Color.RED, view.isExplainable(o));
		if( o instanceof Reaction)
			return getColor(Color.BLUE, view.isExplainable(o));
		if( o instanceof Enzyme)
			return getColor(Color.GREEN, view.isExplainable(o));
		if( o instanceof Transcript)
			return getColor(Color.MAGENTA, view.isExplainable(o));
		if( o instanceof Gene )
			return getColor(Color.YELLOW, view.isExplainable(o));
		if( o instanceof Pathway )
			return getColor(Color.CYAN, view.isExplainable(o));
		
		return Color.WHITE;
	}

	private Color getColor(Color base, boolean is_explainable){
		double val = 1;
		if( ! is_explainable )
			val = 0.3;
		return ColorUtils.getScaledColor(val, 0, 1, Color.WHITE, base, 1);
	}
}
