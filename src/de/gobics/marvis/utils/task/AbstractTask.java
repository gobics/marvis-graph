package de.gobics.marvis.utils.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract task to perform some operation. This can also be performed in the
 * background while the main thread is doing something different.
 *
 * @author manuel
 */
public abstract class AbstractTask<RS, R> {

	public enum State {

		PENDING, RUNNING, CANCELED, DONE
	}
	private static NotifierThread notifier = null;
	private static LinkedBlockingDeque<NotifyEvent> events = new LinkedBlockingDeque<>();
	private static LinkedBlockingQueue<AbstractTask> running_tasks = new LinkedBlockingQueue<>();
	private final HashSet<TaskListener> listener = new HashSet<>();
	/**
	 * Contains the maximum number of steps to process.
	 */
	private int progress_max = 100;
	/**
	 * Contains the current steps that have been performed so far.
	 */
	private int progress_current = -1;
	/**
	 * Contains the current percentage (1 ... 100).
	 */
	private int progress_percent = 0;
	/**
	 * Description of the current step of the task.
	 */
	private String task_description = null;
	/**
	 * A general title for the task.
	 */
	private String task_title = getClass().getSimpleName();
	private State state = State.PENDING;
	private RS result = null;

	public RS perform() throws Exception {
		controlNotifier(true);
		setState(State.RUNNING);
		running_tasks.add(this);
		Exception exception = null;
		result = null;

		try {
			result = doTask();
		}
		catch (Throwable e) {
			NotifyEvent event = new NotifyEvent(listener);
			event.error = e;
			events.add(event);
			exception = new Exception("Exception during execution", e);
		}

		running_tasks.remove(this);
		controlNotifier(false);

		if (state.equals(State.RUNNING)) {
			setState(State.DONE);
		}

		if (exception != null) {
			throw exception;
		}

		// Remove listener for garbage collection
		listener.clear();
		return result;
	}

	synchronized private void controlNotifier(boolean start) {
		if (start && notifier == null) {
			notifier = new NotifierThread();
			notifier.start();
		}
		else if (notifier != null && running_tasks.isEmpty()) {
			notifier.stopThread();
			notifier = null;
		}
	}

	/**
	 * Abstract method to perform the given task.
	 *
	 * @return
	 */
	protected abstract RS doTask() throws Exception;

	/**
	 * Set the maximum progress value to this.
	 *
	 * @param max
	 */
	protected void setProgressMax(int max) {
		progress_max = max;
		if (progress_current < 0) {
			setProgress(0);
		}
	}

	/**
	 * Increments the progress. This is equal to
	 * <code>incrementProgress(1)</code>.
	 */
	protected void incrementProgress() {
		incrementProgress(1);
	}

	/**
	 * Increments the progress by adding the incrementing value.
	 *
	 * @param increment steps to increment
	 */
	protected void incrementProgress(int increment) {
		setProgress(progress_current + increment);
	}

	/**
	 * Sets the progress steps to the new value.
	 *
	 * @param new_progress the steps that have been performed so far (NOT the
	 * percentage)
	 */
	synchronized protected void setProgress(int new_progress) {
		progress_current = Math.abs(new_progress);

		// Calculate precentage
		int new_precentage = (int) Math.floor((((double) progress_current) / ((double) progress_max)) * 100);
		if (new_precentage > 100) {
			new_precentage = 100;
		}
		else if (new_precentage < 0) {
			new_precentage = 0;
		}


		if (new_precentage != progress_percent) {
			progress_percent = new_precentage;
			NotifyEvent event = new NotifyEvent(listener);
			event.progress = progress_percent;
			events.add(event);
		}
	}

	protected void setTaskDescription(String new_description) {
		task_description = new_description;

		NotifyEvent event = new NotifyEvent(listener);
		event.description = getTaskDescription();
		events.add(event);
	}

	/**
	 * Returns the current task descriptions.
	 *
	 * @return a string or {@code null} if no description available
	 */
	public String getTaskDescription() {
		return task_description;
	}

	/**
	 * Change the title for this task to the new value and notifies all
	 * listener.
	 *
	 * @param new_title
	 */
	protected void setTaskTitle(String new_title) {
		this.task_title = new_title;
		NotifyEvent event = new NotifyEvent(listener);
		event.title = getTaskDescription();
		events.add(event);
	}

	/**
	 * Returns the title of this task.
	 *
	 * @return the current title or {@code null} if no title is set.
	 */
	public String getTaskTitle() {
		return task_title;
	}

	/**
	 * Notifies all {@link TaskListener} of this task about the new result.
	 *
	 * @param result the result to publish
	 */
	protected void addTaskResult(R result) {
		NotifyEvent event = new NotifyEvent(listener);
		event.result = result;
		events.add(event);
	}

	/**
	 * Adds the given {@link TaskListener} to the listeners for this task.
	 *
	 * @param tl
	 */
	public void addTaskListener(TaskListener<R> tl) {
		listener.add(tl);
	}

	/**
	 * Removes the given {@link TaskListener} from the listeners for this task.
	 *
	 * @param tl
	 */
	public void removeTaskListener(TaskListener<R> tl) {
		listener.remove(tl);
	}

	/**
	 * Notifies this task to terminate its work without a valid result. This is
	 * only useful in multi-threaded environments.
	 */
	public void cancel() {
		setState(State.CANCELED);
	}

	/**
	 * While the task performs its (long-term) job this method can be used to
	 * check if the task is canceled. This is of special interest in
	 * multi-threaded environments.
	 *
	 * @return true if the task need to be aborted
	 */
	public boolean isCanceled() {
		return state.equals(State.CANCELED);
	}

	/**
	 * After the task has been performed, this method return a status whether
	 * this task ended successfully.
	 *
	 * @return true if the task terminated successfully.
	 */
	public boolean isDone() {
		return state.equals(State.DONE);
	}

	public boolean isRunning() {
		return state.equals(State.RUNNING);
	}

	/**
	 * Returns if this task has been terminated (equals not running anymore).
	 * The termination might have occurred through a call to
	 * <code>cancel()</code> or after successful calculation of the result.
	 *
	 * @return true if the task is done
	 */
	public boolean isTerminated() {
		return !running_tasks.contains(this) && (state.equals(State.DONE) || state.equals(State.CANCELED));
	}

	public RS getTaskResult() {
		return result;
	}

	private void setState(State new_state) {
		if (state.equals(new_state)) {
			return;
		}
		this.state = new_state;
		NotifyEvent event = new NotifyEvent(listener);
		event.state = new_state;
		events.add(event);
	}

	private static class NotifierThread extends Thread {

		private boolean stop = false;

		public NotifierThread() {
			super("Background-Task-Notifier");
		}

		@Override
		public void run() {
			stop = false;
			while (!stop || !events.isEmpty()) {
				NotifyEvent event = null;
				try {
					event = events.poll(2, TimeUnit.SECONDS);
					if (event != null) {
						notify(event);
					}
				}
				catch (InterruptedException ex) {
					Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Can not peek event from queue: ", ex);
					continue;
				}
			}

			notifier = null;
		}

		private void notify(NotifyEvent event) {
			for (TaskListener tl : event.listener) {
				try {
					//System.out.println("Notifying " + tl + " with " + event);
					notifyListener(tl, event);
				}
				catch (UnsupportedOperationException ex) {
					// ignore
				}
				catch (Throwable something_weired) {
					Logger.getLogger(NotifierThread.class.getName()).log(Level.SEVERE, "Can not not notify listener " + tl + ": ", something_weired);
				}
			}
		}

		private void notifyListener(TaskListener tl, NotifyEvent event) {
			if (event.isProgressEvent()) {
				tl.setTaskProgress(event.getProgress());
			}
			if (event.isDescriptionEvent()) {
				tl.setTaskDescription(event.getDescription());
			}
			if (event.isTitleEvent()) {
				tl.setTaskTitle(event.getTitle());
			}
			if (event.isResultEvent()) {
				tl.addTaskResult(event.getResult());
			}
			if (event.isStateEvent()) {
				tl.setTaskState(event.getState());
			}
			if (event.isErrorEvent()) {
				tl.throwsException(event.error);
			}
		}

		public void stopThread() {
			stop = true;
		}
	}

	private static class NotifyEvent {

		public final Collection<TaskListener> listener;
		private int progress = -1;
		private String description = null;
		private String title = null;
		private Object result = null;
		private State state = null;
		private Throwable error = null;

		public NotifyEvent(Collection<TaskListener> listener) {
			this.listener = new ArrayList<>(listener);
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "{progress=" + progress + ";description=" + description + ";title=" + title + ";result=" + result + ";state=" + state + "}";
		}

		public boolean isProgressEvent() {
			return progress >= 0;
		}

		public Integer getProgress() {
			return progress;
		}

		public boolean isDescriptionEvent() {
			return description != null;
		}

		public String getDescription() {
			return description;
		}

		public String getTitle() {
			return title;
		}

		public boolean isTitleEvent() {
			return title != null;
		}

		public Object getResult() {
			return result;
		}

		public boolean isResultEvent() {
			return result != null;
		}

		public State getState() {
			return state;
		}

		public boolean isStateEvent() {
			return state != null;
		}

		private boolean isErrorEvent() {
			return error != null;
		}
	}
}
