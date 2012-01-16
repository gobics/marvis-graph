/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
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
		// Sort Arrays by the name of their class
		JInternalFrame[] frames = parent.getDesktop().getAllFrames();
		if (frames.length < 1) {
			return;
		}

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

		String old_class_name = null;
		JMenu submenu = null;
		
		for (JInternalFrame iframe : frames) {
			if (!iframe.getClass().getName().equals(old_class_name)) {
				old_class_name = iframe.getClass().getSimpleName();
				submenu = new JMenu(old_class_name);
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
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() instanceof InternalFrameMenuItem) {
			parent.getDesktop().moveToFront(((InternalFrameMenuItem) ae.getSource()).getFrame());
		}
	}
}

class InternalFrameMenuItem extends JMenuItem implements ActionListener {

	private JInternalFrame view;

	public InternalFrameMenuItem(JInternalFrame view) {
		super(view.getTitle());
		this.view = view;
		this.addActionListener(this);
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
