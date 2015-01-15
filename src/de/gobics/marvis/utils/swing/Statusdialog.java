package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.task.AbstractTask;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
public class Statusdialog extends JDialog {

	/**
	 * A logging instance.
	 */
	private static final Logger logger = Logger.getLogger(Statusdialog.class.
			getName());

	public Statusdialog(final Window owner) {
		super(owner, "Progress working", owner == null ? ModalityType.APPLICATION_MODAL : ModalityType.DOCUMENT_MODAL);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		setLocationRelativeTo(owner);
		pack();
	}

	public void monitorTask(final SwingWorker task) {
		add(new TaskPanel(this, task));
		setLocationRelativeTo(getParent());
		pack();
	}

	private void taskEnded(TaskPanel panel) {
		remove(panel);
		if (getContentPane().getComponentCount() == 0) {
			setVisible(false);
		}
	}

	void monitorTask(AbstractTask task) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private class TaskPanel extends JPanel implements ActionListener, PropertyChangeListener {

		/**
		 * The worker that will display its data in this panel.
		 */
		private final SwingWorker task;
		/**
		 * The parental dialog that this panel will be displayed in.
		 */
		private final Statusdialog parent_dialog;
		/**
		 * Area to display text messages from the thread.
		 */
		private final JTextArea text = new JTextArea("Started");
		/**
		 * Bar to display the progress for the task.
		 */
		private final JProgressBar progress = new JProgressBar(0, 100);
		/**
		 * A button to cancel the current running thread.
		 */
		private final JButton button_cancel = new JButton("Abort");
		private final Timer failover_timer = new Timer(1000, this);

		/**
		 * Creates a new panel to display the informations for a given task.
		 *
		 * @param parent the statusdialog parent
		 * @param task the task to monitor
		 */
		public TaskPanel(Statusdialog parent, SwingWorker task) {
			this.parent_dialog = parent;
			this.task = task;
			progress.setIndeterminate(true);
			progress.setStringPainted(false);

			setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBorder(BorderFactory.createTitledBorder(task.getClass().getSimpleName()));

			JScrollPane text_spane = new JScrollPane(text);
			text_spane.setPreferredSize(new Dimension(300, 100));
			text_spane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			add(text_spane);
			add(progress);
			text.setEditable(false);

			JPanel button_panel = new JPanel();
			button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.LINE_AXIS));
			button_panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			add(button_panel);
			button_panel.add(Box.createHorizontalGlue());
			button_panel.add(button_cancel);
			button_panel.add(Box.createHorizontalStrut(50));
			button_cancel.setActionCommand("abort-task");
			button_cancel.addActionListener(this);
			button_cancel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			// Setup timer to check every second if the task may have been ended anormal.
			failover_timer.setRepeats(true);
			failover_timer.start();

			task.getPropertyChangeSupport().addPropertyChangeListener("progress", this);
			task.getPropertyChangeSupport().addPropertyChangeListener("state", this);
			task.getPropertyChangeSupport().addPropertyChangeListener("description", this);
			task.getPropertyChangeSupport().addPropertyChangeListener("title", this);
			

			pack();
		}

		/**
		 * Display the given message in the area of the dialog.
		 *
		 * @param message the string to display
		 */
		public void display(final String message) {
			text.setText(message);
		}

		/**
		 * A method handling the termination of the given thread.
		 *
		 * @param task
		 */
		private void monitoredTaskEnded() {
			failover_timer.stop();
			parent_dialog.taskEnded(this);
		}

		/**
		 * Method reacts to actions fired by the button or the fail-over timer.
		 *
		 * @param ae
		 */
		@Override
		public void actionPerformed(ActionEvent ae) {
			if (ae.getSource().equals(button_cancel)) {
				if (ae.getActionCommand().equals("abort-task")) {
					if (task instanceof TaskWrapper) {
						((TaskWrapper) task).getTask().cancel();
					}
					else {
						task.cancel(false);
					}
					button_cancel.setEnabled(false);
					Timer timer = new Timer(2000, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent ae) {
							button_cancel.setEnabled(true);
							button_cancel.setActionCommand("kill-task");
							button_cancel.setText("Kill");
						}
					});
					timer.setRepeats(false);
					timer.start();
				}
				else if (ae.getActionCommand().equals("kill-task")) {
					task.cancel(true);
					button_cancel.setEnabled(false);
				}
				else {
					logger.log(Level.WARNING, "Can not handle action command {0} of button", ae.getActionCommand());
				}
			}
			else if (ae.getSource().equals(failover_timer)) {
				if (task.isDone()) {
					monitoredTaskEnded();
				}
			}
		}

		/**
		 * Method to listen to the background tasks properties.
		 *
		 * @param evt
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// State change
			logger.log(Level.FINER, "Background task {1} fired property change for property: {0}", new Object[]{evt.getPropertyName(), task});
			if (evt.getPropertyName().equals("state")) {
				logger.log(Level.FINE, "Detecting statechange of worker to: {0}", evt.getNewValue());
				if (evt.getNewValue().equals(SwingWorker.StateValue.STARTED)) {
					parent_dialog.setVisible(true);
				}
				else if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
					monitoredTaskEnded();
				}
			}
			// Title change
			else if (evt.getPropertyName().equals("title")) {
				logger.log(Level.FINER, "Detecting title change event to: {0}", evt.getNewValue());
				setBorder(BorderFactory.createTitledBorder(evt.getNewValue().toString()));
			}
			// Description change
			else if (evt.getPropertyName().equals("description")) {
				display(evt.getNewValue().toString());
			}
			// Progress
			else if (evt.getPropertyName().equals("progress")) {
				if (evt.getNewValue() instanceof Number) {
					int percent = ((Integer) evt.getNewValue()).intValue();
					progress.setIndeterminate(false);
					progress.setValue(percent);
					progress.setStringPainted(true);
				}
			}
			else {
				logger.log(Level.WARNING, "Can not handle property change for ''{0}'' with value: {1}", new Object[]{evt.
							getPropertyName(), evt.
							getNewValue()});
			}
		}
	}
}
