package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.task.AbstractTask;
import de.gobics.marvis.utils.task.AbstractTask.State;
import de.gobics.marvis.utils.task.TaskListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author manuel
 */
public class TaskWrapper<RS, R> extends SwingWorker<RS, R> implements TaskListener<R> {

	private final AbstractTask<RS, R> task;

	public TaskWrapper(de.gobics.marvis.utils.task.AbstractTask task) {
		this.task = task;
		task.addTaskListener(this);
	}

	public AbstractTask<RS, R> getTask() {
		return task;
	}

	@Override
	protected RS doInBackground() throws Exception {
		try {
			task.perform();
		}
		catch (Throwable ex) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception in task: ", ex);
		}
		return null;
	}

	@Override
	public void setTaskProgress(int percentage) {
		//System.out.println("Sending event for task progress: "+percentage);
		setProgress(percentage);
	}

	@Override
	public void addTaskResult(R result) {
		publish(result);
	}

	@Override
	public void setTaskDescription(String new_description) {
		//System.out.println("Sending event for task description: "+new_description);
		getPropertyChangeSupport().firePropertyChange("description", null, new_description);
	}

	@Override
	public void setTaskTitle(String new_title) {
		//System.out.println("Sending event for task title: "+new_title);
		getPropertyChangeSupport().firePropertyChange("title", null, new_title);
	}

	@Override
	public void setTaskState(State state) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void throwsException(Throwable t) {
		//ignore
	}
}
