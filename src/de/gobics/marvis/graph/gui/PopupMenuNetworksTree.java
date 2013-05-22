/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.gui.actions.ActionDisplayNetworkInKegg;
import de.gobics.marvis.graph.gui.actions.ActionDrawNetwork;
import de.gobics.marvis.graph.gui.actions.ActionReportOnNetwork;
import de.gobics.marvis.graph.gui.actions.ActionSaveNetwork;
import de.gobics.marvis.utils.swing.TextPopupMenu;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

/**
 *
 * @author manuel
 */
public class PopupMenuNetworksTree extends JPopupMenu {

	private final MarvisGraphMainWindow main_window;
	private final JTree tree;
	private final TreeModelNetworks model;
	private MetabolicNetwork current_network = null;

	public PopupMenuNetworksTree(final MarvisGraphMainWindow main_window, final JTree tree) {
		super("Network menu");
		this.main_window = main_window;
		this.tree = tree;
		this.model = (TreeModelNetworks) tree.getModel();

		JMenuItem menuitem = new JMenuItem("Rename");
		add(menuitem);
		menuitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				renameNetwork();
			}
		});
		add(new JMenuItem(new ActionSaveNetwork(main_window, tree)));
		add(new JMenuItem(new ActionDrawNetwork(main_window, tree)));

		menuitem = new JMenuItem("Open in a new window");
		add(menuitem);
		menuitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openNewWindow();
			}
		});
		add(new JMenuItem(new ActionReportOnNetwork(main_window, tree)));
		add(new JMenuItem(new ActionDisplayNetworkInKegg(main_window, tree)));


		// Add the mouselistener to the menu to detect the popup request
		tree.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup(e.getPoint());
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup(e.getPoint());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup(e.getPoint());
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// ignore
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// ignore
			}
		});

	}

	private void popup(final Point point) {
		current_network = null;
		TreePath selPath = tree.getPathForLocation((int) point.getX(), (int) point.
				getY());
		if (selPath == null) {
			return;
		}
		Object last = selPath.getLastPathComponent();
		if (last == null) {
			return;
		}
		if (!(last instanceof MetabolicNetwork)) {
			return;
		}
		current_network = (MetabolicNetwork) last;

		tree.getSelectionModel().setSelectionPath(selPath);

		setLocation(point);
		show(tree, (int) point.getX(), (int) point.getY());
	}

	/**
	 * Renames the network
	 */
	private void renameNetwork() {
		if (current_network == null) {
			return;
		}

		JTextField field = new JTextField(current_network.getName());
		TextPopupMenu.create(field);

		int res = JOptionPane.showConfirmDialog(tree, field, "Rename network", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (res != JOptionPane.OK_OPTION) {
			return;
		}

		String new_name = field.getText();
		if (new_name != null && !new_name.isEmpty() && !new_name.equals(current_network.getName())) {
			current_network.setName(new_name);
		}
		tree.updateUI();
	}

	private void openNewWindow() {
		if (current_network != null) {
			final MetabolicNetwork new_network = current_network.clone();
			new_network.detachFromParent();

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					MarvisGraphMainWindow main = new MarvisGraphMainWindow();
					main.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					main.setNetwork(new_network);
					main.setVisible(true);
				}
			});
		}
	}
}
