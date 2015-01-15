/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author manuel
 */
public abstract class SwingworkerListener implements PropertyChangeListener {

	private static final Logger logger = Logger.getLogger(SwingworkerListener.class.
			getName());

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("state")) {
			if (evt.getNewValue().equals(SwingWorker.StateValue.STARTED)) {
				processStarted();
			}
			else if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
				processEnded();
			}
		}
		else if (evt.getPropertyName().equals("title")) {
		}
		else if (evt.getPropertyName().equals("description")) {
			setDescription(evt.getNewValue().toString());
		}
		else if (evt.getPropertyName().equals("progress")) {
			setProgress((Integer) evt.getNewValue());
		}
		else {
			logger.warning("Can not handle property change for: " + evt.
					getPropertyName());
		}
	}

	protected void setDescription(String toString) {
	}

	protected void processStarted() {
	}

	protected void processEnded() {
	}

	protected void setProgress(int progress) {
	}
}
