package de.gobics.marvis.graph.gui.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 * The awesome new AbstractTaskListener
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public abstract class AbstractTaskListener implements PropertyChangeListener {

	private static final Logger logger = Logger.getLogger(AbstractTaskListener.class.
			getName());

	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		String name = pce.getPropertyName();
		if (name.equals("description")) {
			receiveDescription(pce.getNewValue().toString());
		}
		else if (name.equals("error")) {
			receiveError(pce.getNewValue().toString());
		}
		else if (name.equals("exception")) {
			Exception ec = (Exception) pce.getNewValue();
			if (ec instanceof InterruptedException) {
				logger.fine("Ignoring interrupted exception");
			}
			else {
				receiveException(ec);
			}
		}
		else if (name.equals("progress")) {
			receiveProgress((Integer) pce.getNewValue());
		}
		else if (name.equals("state")) {
			if (pce.getNewValue().equals(SwingWorker.StateValue.PENDING)) {
				receiveStatusPending();
			}
			if (pce.getNewValue().equals(SwingWorker.StateValue.STARTED)) {
				receiveStatusStarted();
			}
			if (pce.getNewValue().equals(SwingWorker.StateValue.DONE)) {
				receiveStatusDone();
			}
		}
	}

	public void receiveDescription(String desc) {
		//logger.finer(getClass().getName() + " received description but it is ignored");
	}

	abstract public void receiveError(String msg);

	/**
	 * This method is executed when the task has thrown an exception.
	 *
	 * @param ex
	 */
	abstract public void receiveException(Exception ex);

	public void receiveStatusPending() {
		//logger.finer(getClass().getName() + " received pending status but it is ignored");
	}

	public void receiveStatusStarted() {
		//logger.finer(getClass().getName() + " received started status but it is ignored");
	}

	/**
	 * This message is invoked when the process has completely performed its
	 * execution. The SwingWorkers {@code get()} method can immediately be used
	 * to receive the result.
	 */
	public void receiveStatusDone() {
		//logger.finer(getClass().getName() + " received done status but it is ignored");
	}

	/**
	 * The progress is a value between 0 and 100.
	 *
	 * @param integer
	 */
	public void receiveProgress(int progress) {
		//logger.finer(getClass().getName() + " received progress but it is ignored");
	}
}
