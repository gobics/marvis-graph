package de.gobics.marvis.utils.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.Window;
import java.util.logging.Logger;

public class ProcessListenerDialog extends DialogCentered implements ProcessListener, ActionListener {

	private static final Logger logger = Logger.getLogger(ProcessListenerDialog.class.getName());
	private JProgressBar statusBar = new JProgressBar(0, 100);
	private JTextArea warning_messages_area = new JTextArea();
	private JScrollPane warning_messages_scrollpane = new JScrollPane(warning_messages_area);
	private JLabel eta = new JLabel("");
	private long time_start = 0;
	@SuppressWarnings("unchecked")
	private Process process;
	private JButton abortButton = new JButton("Abort"),
			closeButton = new JButton("Close");

	@SuppressWarnings("unchecked")
	public ProcessListenerDialog(Window owner, Process p) {
		this(owner, p, false);
	}

	public ProcessListenerDialog(Window owner, Process p, boolean display_eta) {
		super(owner, ModalityType.DOCUMENT_MODAL);
		
		this.process = p;
		p.addProcessListener(this);

		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setTitle("Status");

		this.statusBar.setString("");
		this.statusBar.setStringPainted(true);
		this.statusBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		// Set preferred size to a width of 300
		Dimension d = this.statusBar.getPreferredSize();
		d.width = 300;
		this.statusBar.setPreferredSize(d);
		this.add(statusBar, BorderLayout.PAGE_START);
		statusBar.setIndeterminate(true);


		this.add(warning_messages_scrollpane, BorderLayout.CENTER);
		warning_messages_scrollpane.setVisible(false);
		warning_messages_scrollpane.setMaximumSize(new Dimension(300, 100));

		JPanel bottom = new JPanel(new BorderLayout());
		bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.add(bottom, BorderLayout.PAGE_END);

		this.eta.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		bottom.add(eta, BorderLayout.CENTER);
		eta.setVisible(false);

		JPanel panel_buttons = new JPanel();
		bottom.add(Box.createHorizontalGlue());
		bottom.add(panel_buttons, BorderLayout.LINE_END);

		panel_buttons.add(abortButton);
		abortButton.addActionListener(this);
		panel_buttons.add(closeButton);
		closeButton.addActionListener(this);
		closeButton.setVisible(false);

		setShowEta(display_eta);
		this.setMaximumSize(new Dimension(500, 600));
		this.pack();
	}

	public void setShowEta(boolean visible) {
		eta.setVisible(visible);
	}

	public void start() {
		if (this.process == null) {
			return;
		}
		this.process.execute();
		this.time_start = System.currentTimeMillis();
		if (!process.isDone() && !process.isCancelled()) {
			this.setVisible(true);
		}
	}

	@Override
	public void processDone(Object result) {
		if (warning_messages_scrollpane.isVisible()) {
			// Some error occured
			this.closeButton.setVisible(true);
			this.abortButton.setVisible(false);
			statusBar.setString(statusBar.getString() + " (done)");
			if (statusBar.isIndeterminate()) {
				statusBar.setValue(1);
				statusBar.setMaximum(1);
			}
		} else {
			this.dispose();
		}
	}

	@Override
	public void processError(Exception e) {
		String s = e.getClass().getSimpleName() + ": " + e.getMessage();
		if (getParent() instanceof Window) {
			new DialogError((Window) getParent(), s).setVisible(true);
		} else {
			new DialogError((Window) null, s).setVisible(true);
		}

		this.dispose();
	}

	@Override
	public void processStatus(ProcessStatus s) {
		if (s.description != null && !s.description.equals(getTitle())) {
			this.setTitle(s.description);
		}

		if (s.max <= 0) {
			this.statusBar.setIndeterminate(true);
		} else {
			this.statusBar.setIndeterminate(false);
			this.statusBar.setString(s.current + " of " + s.max +" ("+s.percent()+"%)");
			this.statusBar.setValue(s.percent());

			if (eta.isVisible()) {
				double millis_till_now = (double)System.currentTimeMillis() - time_start;
				double millis_per_job = millis_till_now / s.current;
				double millis_till_done = millis_per_job *(s.max - s.current);
				long seconds = (long)Math.ceil(millis_till_now/1000);
				long minutes = 0;
				long hours = 0;

				if (seconds >= 60) {
					minutes = (long) Math.floor(seconds / 60);
					seconds = seconds - (minutes * 60);
				}
				if (minutes >= 60) {
					hours = (long) Math.floor(minutes / 60);
					minutes = minutes - (hours * 60);
				}

				if (hours > 0) {
					eta.setText(hours + " hours " + minutes + " minutes " + seconds + " seconds");
				} else if (minutes > 0) {
					eta.setText(minutes + " minutes " + seconds + " seconds");
				} else {
					eta.setText(seconds + " seconds");
				}
			}
		}

		if (s.hasWarningMessage()) {
			logger.finer("Display warning message: " + s.getWarningMessage());
			if (!warning_messages_scrollpane.isVisible()) {
				warning_messages_scrollpane.setVisible(true);
				Dimension d = getSize();
				d.height += 200;
				this.setSize(d);
			}
			warning_messages_area.setText(warning_messages_area.getText() + "\n" + s.getWarningMessage());
			repaint();
		}

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(abortButton)) {
			logger.info("Canceling job: " + process);
			process.cancel(true);
			abortButton.setEnabled(false);

		} else if (arg0.getSource().equals(closeButton)) {
			this.dispose();
		} else {
			logger.warning("Can not handle action event from source: " + arg0);
		}
	}
}
