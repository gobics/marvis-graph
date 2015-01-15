package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.task.AbstractTask.State;
import de.gobics.marvis.utils.task.TaskListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 * A {@link Statusdialog} is used to monitor a {@link SwingWorker}. The
 * StatusDialog will attach listeners to the {@link SwingWorker} to detect
 * status changed (e.g. starting, terminating) of the background process.
 * Depending on the status change, the {@link Statusdialog} will change its
 * layout and behavior.
 *
 *
 * @author manuel
 */
public class Statusdialog2 extends JDialog implements ActionListener, TaskListener {

	/**
	 * A logging instance.
	 */
	private static final Logger logger = Logger.getLogger(Statusdialog.class.
			getName());
	private static final String AC_ABORT = "abort-task";
	/**
	 * Area to display text messages from the thread.
	 */
	private final JPanel text = new JPanel(new SpringLayout());
	/**
	 * Bar to display the progress for the task.
	 */
	private final JProgressBar progress = new JProgressBar(0, 100);
	/**
	 * A button to cancel the current running thread.
	 */
	private final JButton button_abort = new JButton("Abort");
	/**
	 * A timer to fire every second. This is used to test every second if the
	 * task may have ended and the dialog missed the state-change signal.
	 */
	private final Timer failover_timer = new Timer(1000, this);
	/**
	 * The worker that will display its data in this panel.
	 */
	private de.gobics.marvis.utils.task.AbstractTask task;

	public Statusdialog2(final Window owner) {
		super(owner, "Progress working", owner == null ? ModalityType.APPLICATION_MODAL : ModalityType.DOCUMENT_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		add(text, BorderLayout.CENTER);

		JPanel bottom_panel = new JPanel();
		add(bottom_panel, BorderLayout.PAGE_END);
		bottom_panel.setLayout(new BoxLayout(bottom_panel, BoxLayout.PAGE_AXIS));
		bottom_panel.add(progress);
		progress.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		bottom_panel.add(new JSeparator());

		JPanel button_panel = new JPanel();
		bottom_panel.add(button_panel);
		button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.LINE_AXIS));
		button_panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		button_panel.add(Box.createHorizontalGlue());

		// Initialize the abort button
		button_panel.add(button_abort);
		button_panel.add(Box.createHorizontalGlue());
		button_abort.setActionCommand(AC_ABORT);
		button_abort.addActionListener(this);
		getRootPane().setDefaultButton(button_abort);

		setMinimumSize(new Dimension(300, 100));
		pack();
	}

	public void monitorTask(TaskWrapper process) {
		monitorTask(process.getTask());
	}

	public void monitorTask(final de.gobics.marvis.utils.task.AbstractTask task) {
		this.task = task;
		task.addTaskListener(this);
	}

	synchronized private void taskEnded() {
		failover_timer.stop();
		setVisible(false);
	}

	synchronized private void taskStarted() {
		button_abort.setEnabled(true);
		setTitle(task.getTaskTitle());
		setText(task.getTaskDescription());
		pack();
		setLocationRelativeTo(getParent());
		
		progress.setValue(0);
		progress.setIndeterminate(true);
		progress.setString("");

		failover_timer.setRepeats(true);
		failover_timer.start();
		setVisible(true);
	}

	synchronized private void setText(String message) {
		// Remove old text
		text.removeAll();

		if (message == null || message.isEmpty()) {
			return;
		}

		for (String line : message.split("\n")) {
			int idx = line.indexOf(" ", 50);
			while (idx > 0) {
				text.add(new JLabel(line.substring(0, idx)));
				line = line.substring(idx + 1);
				idx = line.indexOf(" ", 50);
			}
			text.add(new JLabel(line));
		}

		SpringUtilities.makeCompactGrid(text, text.getComponentCount(), 1);
		pack();
		setLocationRelativeTo(getParent());
	}

	/**
	 * Method reacts to actions fired by the button or the fail-over timer.
	 *
	 * @param ae
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource().equals(button_abort)) {
			if (ae.getActionCommand().equals(AC_ABORT)) {
				task.cancel();
				button_abort.setEnabled(false);
			}
			else {
				logger.log(Level.WARNING, "Can not handle action command {0} of button", ae.getActionCommand());
			}
		}
		else if (ae.getSource().equals(failover_timer)) {
			if (task.isTerminated()) {
				taskEnded();
			}
		}
	}

	@Override
	public void setTaskProgress(final int percentage) {
		//System.err.println("Detect progress change: " + percentage);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progress.setIndeterminate(false);
				progress.setStringPainted(true);
				progress.setString(percentage + " %");
				progress.setValue(percentage);
			}
		});
	}

	@Override
	public void addTaskResult(Object result) {
		//ignore
	}

	@Override
	public void setTaskDescription(final String new_description) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setText(new_description);
			}
		});
	}

	@Override
	public void setTaskTitle(final String new_title) {
		//System.err.println("Detect title change: " + new_title);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setTitle(new_title);
			}
		});
	}

	@Override
	public void setTaskState(final State state) {
		if (state.equals(State.RUNNING)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					taskStarted();
				}
			});
		}
		else if (task.isTerminated()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					taskEnded();
				}
			});
		}
	}

	@Override
	public void throwsException(Throwable t) {
		// ignore - we can not do anything
	}
}
