package de.gobics.marvis.utils.task;

import de.gobics.marvis.utils.task.AbstractTask.State;

/**
 * An abstract implementation of the {@link TaskListener} that ignores
 * all changes. This decreases the amount of methods to be implemented, e.g. when an 
 * anonymous class is created.
 *
 * @author manuel
 */
public abstract class AbstractTaskListener<V> implements TaskListener<V> {

	@Override
	public void setTaskProgress(int percentage) {
		// ignore
	}

	@Override
	public void addTaskResult(V result) {
		// ignore
	}

	@Override
	public void setTaskDescription(String new_description) {
		// ignore
	}

	@Override
	public void setTaskTitle(String new_title) {
		// ignore
	}

	@Override
	public void throwsException(Throwable t) {
		// ignore
	}

	@Override
	public void setTaskState(State state) {
		// ignore
	}
	
}
