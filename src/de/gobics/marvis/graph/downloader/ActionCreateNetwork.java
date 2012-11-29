package de.gobics.marvis.graph.downloader;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/**
 * Simple action to close the downloader dialog window.
 *
 * @author manuel
 */
public class ActionCreateNetwork extends AbstractDownloaderAction {

	public ActionCreateNetwork(final NetworkDownloaderDialog window) {
		super(window, "Create network");
		putValue(SHORT_DESCRIPTION, "Create metabolic network");
		putValue(MNEMONIC_KEY, KeyEvent.VK_C);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().performDownload();
	}
}
