/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.MetabolicNetwork;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
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
				PopupMenuNetworksTree.this.renameNetwork();
			}
		});
		menuitem = new JMenuItem("Save network");
		add(menuitem);
		menuitem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PopupMenuNetworksTree.this.saveNetwork();
			}
		});
		menuitem = new JMenuItem("Draw network");
		add(menuitem);
		menuitem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PopupMenuNetworksTree.this.drawNetwork();
			}
		});

		menuitem = new JMenuItem("Open in a new window");
		add(menuitem);
		menuitem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PopupMenuNetworksTree.this.openNewWindow();
			}
		});


		// Add the mouselistener to the menu to detect the popup request
		tree.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					PopupMenuNetworksTree.this.popup(e.getPoint());
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// ignore
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// ignore
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

		tree.getSelectionModel().setSelectionPath(selPath);

		current_network = (MetabolicNetwork) last;

		setLocation(point);
		show(tree, (int) point.getX(), (int) point.getY());
	}

	private void renameNetwork() {
		if (current_network == null) {
			return;
		}

		String name = current_network.getName();
		String new_name = JOptionPane.showInputDialog(tree, "Please insert new name:", name);
		if (new_name != null && !new_name.isEmpty() && !new_name.equals(name)) {
			current_network.setName(new_name);
		}

		tree.updateUI();
	}

	private void saveNetwork() {
		if (current_network == null) {
			return;
		}
		main_window.saveNetwork(current_network);
	}

	private void drawNetwork() {
		if (current_network == null) {
			return;
		}
		main_window.createNetworkVisualization(current_network);
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