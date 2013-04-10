/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.gui.tasks.AbstractNetworkCalculation;
import de.gobics.marvis.graph.sort.*;
import java.awt.Component;
import javax.swing.*;


/**
 *
 * @author manuel
 */
public class ComboBoxGraphSort extends JComboBox {

	public ComboBoxGraphSort() {

		setRenderer(new GraphSortListCellRenderer());

		addItem(new NetworkSorterSumOfWeights());
		addItem(new NetworkSorterDiameter());
		addItem(new NetworkSorterSEA());
		//addItem(new ModularityScore());
		addItem(new NetworkSorterName());
		addItem(new NetworkSorterSize());
		addItem(new NetworkSorterRelativeSize());
		//addItem(new pValueScore());
		//addItem(new BetweennessCentrality());
		//addItem(new RelativeBetweennessCentrality());
		//addItem(new SpearmanSort());
		setSelectedIndex(0);

	}

	public AbstractGraphScore getSorterFor(MetabolicNetwork parent, AbstractNetworkCalculation subnet_process) {
		AbstractGraphScore sorter = (AbstractGraphScore) getSelectedItem();
		if( sorter instanceof NetworkSorterDiameter && subnet_process != null ){
			((NetworkSorterDiameter)sorter).setCofactorThreshold( subnet_process.getCofactorThreshold());
		}
		sorter.setParent(parent);
		return sorter;
	}
}
class GraphSortListCellRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component top = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (top instanceof JLabel && value instanceof AbstractGraphScore) {
			((JLabel) top).setText(((AbstractGraphScore) value).getName());
			((JLabel) top).setToolTipText(((AbstractGraphScore) value).
					getDescription());
		}
		return top;
	}
}