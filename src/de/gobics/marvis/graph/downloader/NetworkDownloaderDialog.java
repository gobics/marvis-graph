package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.utils.swing.Statusdialog2;
import de.gobics.marvis.utils.swing.TaskWrapper;
import de.gobics.marvis.utils.task.AbstractTask;
import de.gobics.marvis.utils.task.AbstractTask.State;
import de.gobics.marvis.utils.task.AbstractTaskListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class NetworkDownloaderDialog extends JDialog {

	private final ComboBoxSource source;
	private final JPanel options_panel_wrapper = new JPanel();
	private AbstractOptionsPanel options_panel = null;

	public NetworkDownloaderDialog(final MarvisGraphMainWindow main_window) {
		super(main_window, "Create a new network");
		setLayout(new BorderLayout());

		source = new ComboBoxSource(main_window);
		add(source, BorderLayout.PAGE_START);
		source.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		source.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				NetworkDownloaderDialog.this.updateOptions();
			}
		});

		add(options_panel_wrapper, BorderLayout.CENTER);
		
		JPanel panel_buttons = new JPanel();
		panel_buttons.setLayout(new BoxLayout(panel_buttons, BoxLayout.LINE_AXIS));
		panel_buttons.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		panel_buttons.add(Box.createHorizontalGlue());
		panel_buttons.add(new JButton(new ActionCloseDialog(this)));
		panel_buttons.add(Box.createRigidArea(new Dimension(10, 0)));
		panel_buttons.add(new JButton(new ActionCreateNetwork(this)));
		add(panel_buttons, BorderLayout.PAGE_END);
				
		updateOptions();
	}

	public void updateOptions() {
		final AbstractOptionsPanel panel = source.getOptionsPanel();
		options_panel = panel;
		options_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		options_panel_wrapper.removeAll();
		options_panel_wrapper.add(options_panel);

		final AbstractTask<Void, Void> process = new AbstractTask<Void, Void>() {
			@Override
			protected Void doTask() throws Exception {
				setTaskTitle("Loading");
				setTaskDescription("Fetching information");
				panel.updateOptions();
				NetworkDownloaderDialog.this.pack();
				NetworkDownloaderDialog.this.setMinimumSize(NetworkDownloaderDialog.this.getSize());
				NetworkDownloaderDialog.this.setLocationRelativeTo(NetworkDownloaderDialog.this.getOwner());

				return null;
			}
		};
		new Statusdialog2(this).monitorTask(process);
		new TaskWrapper<>(process).execute();
	}

	public void performDownload() {
		final AbstractNetworkCreator process = options_panel.getProcess();
		process.addTaskListener(new AbstractTaskListener<Void>() {
			@Override
			public void setTaskState(State state) {
				if (process.isDone()) {
					MetabolicNetwork network = process.getTaskResult();
					if (network != null) {
						getMainWindow().setNetwork(network);
						NetworkDownloaderDialog.this.dispose();
						getMainWindow().displayNetworkReport(network);
					}
				}
			}
		});


		dispose();
		getMainWindow().executeTask(process);
	}

	private MarvisGraphMainWindow getMainWindow() {
		return (MarvisGraphMainWindow) getParent();
	}
}
