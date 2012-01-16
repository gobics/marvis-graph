package de.gobics.marvis.graph.gui;

import java.awt.BasicStroke;
import java.awt.Stroke;
import org.apache.commons.collections15.Transformer;
import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.graphview.GraphViewAbstract;

public class VertexTransformerStroke implements Transformer<GraphObject, Stroke> {
	private GraphViewAbstract graph;

	public VertexTransformerStroke(GraphViewAbstract g){
		this.graph = g;
	}
	
	@Override
	public Stroke transform(GraphObject gobject) {
		float dash[] = {10.0f};
		
		boolean explainable = graph.isExplainable(gobject);
		if( ! explainable )
			return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		
		return new BasicStroke();
	}

}
