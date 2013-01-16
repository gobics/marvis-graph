package de.gobics.marvis.graph.gui.graphvisualizer;

import java.awt.BasicStroke;
import java.awt.Stroke;
import org.apache.commons.collections15.Transformer;
import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.graphview.GraphView;
import de.gobics.marvis.graph.graphview.FullGraph;

public class VertexTransformerStroke implements Transformer<GraphObject, Stroke> {
	private GraphView graph;

	public VertexTransformerStroke(GraphView g){
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
