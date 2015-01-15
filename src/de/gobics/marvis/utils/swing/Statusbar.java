/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.task.AbstractTask.State;
import de.gobics.marvis.utils.task.TaskListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 *
 * @author manuel
 */
public class Statusbar extends JPanel {

	private static final Logger logger = Logger.getLogger(Statusbar.class.getName());
	private static final int timeout_default = 5000;
	private final JLabel status_text = new JLabel("Started");
	private final JProgressBar progressbar = new JProgressBar(0, 100);
	private Timer timer = null;

	public Statusbar() {
		super(new BorderLayout(), true);
		setBorder(BorderFactory.createLoweredBevelBorder());

		add(status_text, BorderLayout.CENTER);
		add(progressbar, BorderLayout.LINE_END);
		progressbar.setIndeterminate(true);
		progressbar.setVisible(false);

		// Calculate size
		Dimension size = status_text.getMaximumSize();
		size.height += 5;
		setPreferredSize(size);
	}

	public void display(String message) {
		display(message, timeout_default);
	}

	public void display(final String message, final int timeout) {
		if (timer != null) {
			timer.stop();
		}
		status_text.setText(message);
		final Timer private_timer = new Timer(timeout, null);
		private_timer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				status_text.setText("");
				private_timer.stop();
			}
		});
		timer = private_timer;
		timer.setRepeats(false);
		timer.start();
	}

	private void monitoredTaskEnded(de.gobics.marvis.utils.task.AbstractTask task) {
		if (task.isCanceled()) {
			display("Aborted: " + task.getClass().getSimpleName());
		}
		else if (task.isDone()) {
			display("Done: " + task.getClass().getSimpleName());
		}
		progressbar.setVisible(false);
	}

	public void monitorTask(final de.gobics.marvis.utils.task.AbstractTask task) {

		// This timer will check from time to time if the process is done. This
		// may happen if the propertychangelistener missses to detect the termination
		final Timer failover_timer = new Timer(1000, null);
		failover_timer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (task.isCanceled() || task.isDone()) {
					monitoredTaskEnded(task);
					failover_timer.stop();
				}
			}
		});
		failover_timer.setRepeats(true);
		failover_timer.start();

		task.addTaskListener(new TaskListener() {
			@Override
			public void setTaskProgress(int percentage) {

				progressbar.setIndeterminate(false);
				progressbar.setValue(percentage);
			}

			@Override
			public void addTaskResult(Object result) {
				//ignore
			}

			@Override
			public void setTaskDescription(String new_description) {
				display(new_description);
			}

			@Override
			public void setTaskTitle(String new_title) {
				//ignore
			}


			@Override
			public void setTaskState(State state) {
				if (state.equals(State.RUNNING)) {
					progressbar.setIndeterminate(true);
					progressbar.setVisible(true);
				}
				else if (task.isTerminated()) {
					monitoredTaskEnded(task);
				}
			}

			@Override
			public void throwsException(Throwable t) {
				//ignore
			}
		});
	}
}
