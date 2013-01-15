package de.gobics.marvis.graph.gui.graphvisualizer;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.graphview.GraphViewAbstract;

public class EdgeTransformerStroke<E> implements Transformer<E, Stroke> {

	private GraphViewAbstract graph;

	public EdgeTransformerStroke(GraphViewAbstract parent) {
		graph = parent;
	}

	@Override
	public Stroke transform(E edge) {
		float dash[] = {5f};
		
		if( ! (edge instanceof Relation)){
			return new BasicStroke();
		}

		Relation relation = (Relation)edge;
		
		boolean explainable = graph.isExplainable(relation.getStart()) && graph.isExplainable(relation.getEnd());
		if (!explainable) {
			return new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 10.0f, dash, 0.0f);
		}
		return new BasicStroke();
	}
}
