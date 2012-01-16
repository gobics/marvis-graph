/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.actions;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

/**
 *
 * @author manuel
 */
public abstract class AbstractViewerAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(AbstractViewerAction.class.
			getName());

	public AbstractViewerAction(String name) {
		this(name, null);
	}

	public AbstractViewerAction(String name, String iconname) {
		super(name);
		if (iconname != null) {
			setIcon(iconname);
		}
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