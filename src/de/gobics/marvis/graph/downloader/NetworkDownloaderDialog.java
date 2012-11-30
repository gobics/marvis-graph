package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.graph.gui.tasks.AbstractTaskListener;
import de.gobics.marvis.utils.swing.Statusdialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class NetworkDownloaderDialog extends JDialog {

	private static final Logger logger = Logger.getLogger(NetworkDownloaderDialog.class.
			getName());
	private final ComboBoxSource source;
	private final JPanel options_panel_wrapper = new JPanel();
	private AbstractOptionsPanel options_panel = null;

	public NetworkDownloaderDialog(MarvisGraphMainWindow main_window) {
		super(main_window, "Download metabolic network", ModalityType.APPLICATION_MODAL);
		if (main_window == null) {
			throw new NullPointerException("Main window can not be null");
		}
		source = new ComboBoxSource(main_window);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		getContentPane().add(source);
		source.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		source.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				NetworkDownloaderDialog.this.updateOptions();
			}
		});

		getContentPane().add(options_panel_wrapper);

		JPanel panel_buttons = new JPanel();
		getContentPane().add(panel_buttons);
		panel_buttons.setLayout(new BoxLayout(panel_buttons, BoxLayout.LINE_AXIS));
		panel_buttons.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		panel_buttons.add(Box.createHorizontalGlue());
		panel_buttons.add(new JButton(new ActionCloseDialog(this)));
		panel_buttons.add(Box.createRigidArea(new Dimension(10, 0)));
		panel_buttons.add(new JButton(new ActionCreateNetwork(this)));

		setLocationRelativeTo(getOwner());
		updateOptions();
	}

	public void updateOptions() {
		final AbstractOptionsPanel panel = source.getOptionsPanel();
		options_panel = panel;
		options_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		options_panel_wrapper.removeAll();
		options_panel_wrapper.add(options_panel);
		pack();

		SwingWorker<Void, Void> process = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() {
				panel.updateOptions();
				NetworkDownloaderDialog.this.pack();
				return null;
			}
		};
		new Statusdialog(this).monitorTask(process);
		process.execute();
	}

	public void performDownload() {
		final AbstractNetworkCreator process = options_panel.getProcess();
		getMainWindow().monitorTask(process);
		process.addPropertyChangeListener(new AbstractTaskListener() {

			@Override
			public void receiveError(String msg) {
				getMainWindow().display_error(msg);
			}

			@Override
			public void receiveException(Exception ex) {
				getMainWindow().display_error("Exception during download", ex);
			}

			@Override
			public void receiveStatusDone() {
				if (process.isCancelled()) {
					return;
				}
				try {
					MetabolicNetwork network = process.get();
					if (network != null) {
						getMainWindow().setNetwork(network);
						NetworkDownloaderDialog.this.dispose();
						getMainWindow().displayNetworkReport(network);
					}
				}
				catch (Exception ex) {
					logger.log(Level.SEVERE, "Can not download network: ", ex);
					getMainWindow().display_error("Can not download network", ex);
				}
			}
		});

		dispose();
		process.execute();
	}

	private MarvisGraphMainWindow getMainWindow() {
		return (MarvisGraphMainWindow) getParent();
	}
}