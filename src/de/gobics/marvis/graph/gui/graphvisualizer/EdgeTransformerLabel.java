package de.gobics.marvis.graph.gui.graphvisualizer;

import org.apache.commons.collections15.Transformer;

import de.gobics.marvis.graph.Relation;

public class EdgeTransformerLabel<E> implements Transformer<E, String> {

	private boolean show_label = true;

	public EdgeTransformerLabel() {
	}

	public EdgeTransformerLabel(boolean show_label) {
		this.show_label = show_label;
	}

	public void setShowLabel(boolean selected){
		show_label = selected;
	}

	@Override
	public String transform(E edge) {
		if( ! show_label )
			return "";
		
		if(!(edge instanceof Relation)){
			return edge.toString();
		}
		
		switch (((Relation)edge).getType()) {
		case MARKER_ANNOTATION_COMPOUND:
			return "annotates";
		case REACTION_HAS_SUBSTRATE:
			return "has substrate";
		case REACTION_HAS_PRODUCT:
			return "has product";
		case REACTION_NEEDS_ENZYME:
			return "needs enzyme";
		case GENE_ENCODES_ENZYME:
			return "encodes for";
		case TRANSCRIPT_ISFROM_GENE:
			return "is from";
		case REACTION_HAPPENSIN_PATHWAY:
			return "happens in";
		case ENZYME_USEDIN_PATHWAY:
			return "used in";
		}
		return ((Relation)edge).getType().toString();
	}
}
