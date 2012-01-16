/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingWorker;

/**
 *
 * @author manuel
 */
public abstract class ProcessListener implements PropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("state") && evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
			processDone();
		}
		else if (evt.getPropertyName().equals("exception") && evt.getNewValue() instanceof Exception) {
			processError((Exception) evt.getNewValue());
		}
	}

	public abstract void processDone();

	public abstract void processError(Exception exeption);
}
