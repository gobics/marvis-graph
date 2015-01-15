package de.gobics.marvis.utils.task;

import de.gobics.marvis.utils.task.AbstractTask.State;
import java.util.logging.Level;

/**
 * Listener for {@link AbstractTask}s status changes etc.
 *
 * @author manuel
 */
public interface TaskListener<R> {
	
	public void setTaskProgress(int percentage);
	
	public void addTaskResult(R result);
	
	public void setTaskDescription(String new_description);
	
	public void setTaskTitle(String new_title);

	public void throwsException(Throwable t);

	public void setTaskState(State state);
}
