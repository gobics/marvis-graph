/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.InternalFrameGraph;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/**
 *
 * @author manuel
 */
public class ActionVisualizationClose extends AbstractVisualizationAction {

	public ActionVisualizationClose(InternalFrameGraph frame){
		super(frame, "Close");
		putValue(SHORT_DESCRIPTION, "Closes the window");
		putValue(MNEMONIC_KEY, KeyEvent.VK_C);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		getInternalFrameGraph().dispose();
	}	
}
