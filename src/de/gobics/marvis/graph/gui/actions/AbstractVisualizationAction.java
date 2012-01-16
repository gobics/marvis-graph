/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.InternalFrameGraph;
import javax.swing.AbstractAction;
import javax.swing.Icon;

/**
 *
 * @author manuel
 */
public abstract class AbstractVisualizationAction extends AbstractAction {
	private final InternalFrameGraph parent;
	
	public AbstractVisualizationAction(InternalFrameGraph parent) {
		this(parent, "Busy");
	}

	public AbstractVisualizationAction(InternalFrameGraph parent, String name) {
		this(parent, name, null);
	}

	public AbstractVisualizationAction(InternalFrameGraph parent, String name, Icon icon) {
		super(name, icon);
		this.parent = parent;
	}
	
	public InternalFrameGraph getInternalFrameGraph(){
		return parent;
	}

}
