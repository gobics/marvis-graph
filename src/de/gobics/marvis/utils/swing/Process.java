package de.gobics.marvis.utils.swing;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

public abstract class Process<R> extends SwingWorker<R, ProcessStatus> {

	private static final Logger logger = Logger.getLogger(Process.class.getName());
	private ArrayList<ProcessListener> processListener = new ArrayList<ProcessListener>();
	private R result = null;

	public void addProcessListener(ProcessListener l) {
		if (!this.processListener.contains(l)) {
			this.processListener.add(l);
		}
	}

	@Override
	final protected R doInBackground() {
		logger.finer("Starting process: " + this.getClass().getName());
		try {
			result = this.start();
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Can not complete process " + getClass().getName() + ": ", e);
			for (ProcessListener l : this.processListener) {
				l.processError(e);
			}
			return null;
		}

		return result;
	}

	@Override
	public void done() {
		for (ProcessListener pl : processListener) {
			pl.processDone(result);
		}
	}

	public abstract R start() throws Exception;

	@Override
	protected void process(List<ProcessStatus> stati) {
		for (int idx = 0; idx < stati.size(); idx++) {
			// Publish only in case of an error or if it is the last message
			ProcessStatus status = stati.get(idx);
			if (status.hasWarningMessage() || status.hasDescription() || idx == stati.size() - 1) {
				for (ProcessListener l : this.processListener) {
					l.processStatus(status);
				}
			}
		}
	}

	protected void publishStatus(int cur, int max) {
		publish(new ProcessStatus(cur, max));
	}

	protected void publishStatus(int cur, int max, String description) {
		publish(new ProcessStatus(description, cur, max));
	}

	protected void publishStatus(String description) {
		publish(new ProcessStatus(description, 0, 0));
	}

	protected void publishStatus(int cur, int max, String description, String warning) {
		publish(new ProcessStatus(description, cur, max, warning));
	}

	
}
