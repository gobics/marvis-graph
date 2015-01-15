package de.gobics.marvis.utils.task;

import de.gobics.marvis.utils.task.AbstractTask.State;

/**
 * Implements all methods of the {@link TaskListener} interface but adds a new abstract method
 * <code>taskDone()</code> that is called if and only if the task terminated 
 * successfully and the result is now available.
 *
 * @author manuel
 */
public abstract class TaskResultListener<R> implements TaskListener<R> {

	@Override
	public void setTaskProgress(int percentage) {
		//ignore
	}

	@Override
	public void addTaskResult(R result) {
		// ignore
	}

	@Override
	public void setTaskDescription(String new_description) {
		// ignore
	}

	@Override
	public void setTaskTitle(String new_title) {
		//ignore
	}

	@Override
	public void throwsException(Throwable t) {
		//ignore
	}

	@Override
	public void setTaskState(State state) {
		if(state.equals(State.DONE)){
			taskDone();
		}
	}

	public abstract void taskDone();

}
