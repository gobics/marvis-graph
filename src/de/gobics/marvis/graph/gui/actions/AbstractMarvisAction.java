/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
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
public abstract class AbstractMarvisAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(AbstractMarvisAction.class.getName());

	private final MarvisGraphMainWindow parent;

	public AbstractMarvisAction(MarvisGraphMainWindow parent) {
		this(parent, "Busy");
	}

	public AbstractMarvisAction(MarvisGraphMainWindow parent, String name) {
		this(parent, name, null);
	}

	public AbstractMarvisAction(MarvisGraphMainWindow parent, String name, Icon icon) {
		super(name, icon);
		this.parent = parent;
	}

	public MarvisGraphMainWindow getMainWindow() {
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
