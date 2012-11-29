/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.downloader;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author manuel
 */
public abstract class AbstractDownloaderAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(AbstractDownloaderAction.class.getName());

	private final NetworkDownloaderDialog parent;

	public AbstractDownloaderAction(NetworkDownloaderDialog parent) {
		this(parent, "Busy");
	}

	public AbstractDownloaderAction(NetworkDownloaderDialog parent, String name) {
		this(parent, name, null);
	}

	public AbstractDownloaderAction(NetworkDownloaderDialog parent, String name, Icon icon) {
		super(name, icon);
		this.parent = parent;
	}

	public NetworkDownloaderDialog getMainWindow() {
		return parent;
	}

	protected void setIcon(String filename) {
		URL path = getClass().getResource("/de/gobics/marvis/graph/gui/ressources/" + filename);
		if (path != null) {
			Image img = Toolkit.getDefaultToolkit().getImage(path);
			ImageIcon icon = new ImageIcon(img.getScaledInstance(24, 24, Image.SCALE_SMOOTH));
			putValue(SMALL_ICON, icon);
		}
		else {
			logger.warning("Can not find icon: " + filename);
		}
	}
}
