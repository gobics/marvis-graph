package de.gobics.marvis.graph.gui.graphvisualizer;

import org.apache.commons.collections15.Transformer;

import de.gobics.marvis.graph.*;

public class VertexTransformerLabel implements Transformer<GraphObject, String> {

	private boolean show_label = true;

	public VertexTransformerLabel() {
	}

	public VertexTransformerLabel(boolean show_label) {
		this.show_label = show_label;
	}

	public void setShowLabel(boolean selected){
		show_label = selected;
	}

	@Override
	public String transform(GraphObject o) {
		if (!show_label) {
			return "";
		}
		if (o.getName() != null) {
			if (o.getName().length() > 20) {
				return o.getName().substring(0, 17) + "...";
			}
			else {
				return o.getName();
			}
		}
		return o.getId();
	}
}
