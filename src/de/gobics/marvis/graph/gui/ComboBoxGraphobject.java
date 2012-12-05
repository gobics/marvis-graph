/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.Pathway;
import java.util.Collection;
import javax.swing.JComboBox;

/**
 *
 * @author manuel
 */
public class ComboBoxGraphobject<T extends GraphObject> extends JComboBox {

	public ComboBoxGraphobject(T[] objects) {
		for (GraphObject o : objects) {
			addItem(new GraphObjectItem(o));
		}
		setSelectedIndex(0);
	}

	public ComboBoxGraphobject(Collection<T> objects) {
		for (GraphObject o : objects) {
			addItem(new GraphObjectItem(o));
		}
		setSelectedIndex(0);
	}

	@SuppressWarnings("unchecked")
	public T getSelectGraphObject() {
		return (T) ((GraphObjectItem) getSelectedItem()).object;
	}

	private class GraphObjectItem {

		private final GraphObject object;

		public GraphObjectItem(GraphObject o) {
			this.object = o;
		}

		@Override
		public String toString() {
			return object.getId() + " - " + object.getName();
		}
	}
}