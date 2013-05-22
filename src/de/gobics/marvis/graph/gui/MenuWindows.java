package de.gobics.marvis.graph.gui;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Subclassing of a {@link JMenu} to display a dynamic list of opened windows
 * in the {@link MarvisGraphMainWindow}. 
 * 
 * The menu uses the {@link MenuListener} to get a notification when the menu is
 * selected (a.k.a. opened). The menu is then dynamicaly generated via the
 * {@link #menuSelected(javax.swing.event.MenuEvent) menuSelected() method}.
 *
 * @author manuel
 */
public class MenuWindows extends JMenu implements MenuListener, ActionListener {

	private MarvisGraphMainWindow parent;

	public MenuWindows(MarvisGraphMainWindow parent) {
		super("Windows");
		this.parent = parent;
		addMenuListener(this);
	}

	@Override
	public void menuSelected(MenuEvent me) {
		JInternalFrame[] frames = parent.getDesktop().getAllFrames();
		if (frames == null || frames.length < 1) {
			JMenuItem i = new JMenuItem("No windows opened");
			i.setEnabled(false);
			add(i);
			return;
		}

		// Sort Arrays by the name of their class
		Arrays.sort(frames, new Comparator<JInternalFrame>() {
			@Override
			public int compare(JInternalFrame t, JInternalFrame t1) {
				int c = t.getClass().getName().compareTo(t1.getClass().getName());
				if (c != 0) {
					return c;
				}
				if (t.getTitle() == null) {
					return 1;
				}
				return t.getTitle().compareTo(t1.getTitle());
			}
		});

		Class<? extends JInternalFrame> old_class = null;
		JMenu submenu = null;

		for (JInternalFrame iframe : frames) {
			if (!iframe.getClass().equals(old_class)) {
				old_class = iframe.getClass();
				submenu = new JMenu(toName(iframe));
				add(submenu);
			}
			InternalFrameMenuItem item = new InternalFrameMenuItem(iframe);
			submenu.add(item);
			item.addActionListener(this);
		}
	}

	@Override
	public void menuDeselected(MenuEvent me) {
		removeAll();
	}

	@Override
	public void menuCanceled(MenuEvent me) {
		removeAll();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() instanceof InternalFrameMenuItem) {
			parent.getDesktop().moveToFront(((InternalFrameMenuItem) ae.getSource()).getFrame());
		}
	}

	/**
	 * Transforms a {@link JInternalFrame} instance into a name depending on the
	 * instances true class, e.g. {@link InternalFrameGraph}.
	 *
	 * @param frame
	 * @return the name of the type
	 */
	private String toName(JInternalFrame frame) {
		if (frame instanceof InternalFrameGraph) {
			return "Network";
		}
		if (frame instanceof InternalFrameNodeInformation) {
			return "Entity";
		}
		return frame.getClass().getSimpleName();
	}

	private static class InternalFrameMenuItem extends JMenuItem implements ActionListener {

		private JInternalFrame view;

		public InternalFrameMenuItem(JInternalFrame view) {
			super(view.getTitle());
			this.view = view;
			this.addActionListener(this);

			if (view.getTitle().length() > 100) {
				setText(view.getTitle().substring(0, 100) + "...");
			}
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			if (ae.getSource().equals(this)) {
				view.toFront();
			}
		}

		public JInternalFrame getFrame() {
			return this.view;
		}
	}
}