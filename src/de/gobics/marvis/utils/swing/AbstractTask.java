package de.gobics.marvis.utils.swing;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public abstract class AbstractTask<T, V> extends SwingWorker<T, V> {

	private static final Logger logger = Logger.getLogger(AbstractTask.class.
			getName());
	private int progress_max = -1;
	private int progress_current = 0;

	protected void sendTitle(String title) {
		getPropertyChangeSupport().firePropertyChange("title", null, title);
	}

	protected void sendDescription(String message) {
		getPropertyChangeSupport().firePropertyChange("description", null, message);
	}

	protected void sendError(String errstr) {
		getPropertyChangeSupport().firePropertyChange("error", null, errstr);
	}

	protected void sendException(Exception e) {
		getPropertyChangeSupport().firePropertyChange("exception", null, e);
	}

	protected void setProgressMax(int max) {
		progress_max = max;
	}

	synchronized protected void incrementProgress() {
		incrementProgress(1);
	}

	synchronized protected void incrementProgress(int increment) {
		if (progress_max < 1) {
			return;
		}
		progress_current += increment;
		int new_progress = (int) Math.floor((((double) progress_current) / ((double) progress_max)) * 100);
		if (new_progress > 100) {
			new_progress = 100;
		}
		else if (new_progress < 0) {
			new_progress = 0;
		}

		//logger.finer("New progress is now: " + new_progress);
		if (new_progress != getProgress()) {
			//logger.finer("New progress will be send: " + new_progress);
			setProgress(new_progress);
		}
	}

	@Override
	public T doInBackground() {
		progress_current = 0;
		try {
			return performTask();
		}
		catch (Exception e) {
			sendException(e);
			logger.log(Level.SEVERE, "Can not perform task", e);
		}
		return null;
	}

	abstract protected T performTask() throws Exception;
}
