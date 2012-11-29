package de.gobics.marvis.graph.downloader;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/**
 * Simple action to close the downloader dialog window.
 *
 * @author manuel
 */
public class ActionCloseDialog extends AbstractDownloaderAction {

	public ActionCloseDialog(final NetworkDownloaderDialog window) {
		super(window, "Close dialog");
		putValue(SHORT_DESCRIPTION, "Closes this dialog");
		putValue(MNEMONIC_KEY, KeyEvent.VK_W);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getMainWindow().dispose();
	}
}
