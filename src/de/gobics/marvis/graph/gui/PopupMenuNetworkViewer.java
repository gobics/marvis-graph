/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.graphview.GraphView;
import de.gobics.marvis.graph.graphview.GraphViewDefault;
import de.gobics.marvis.graph.graphview.GraphViewCustomizable;
import de.gobics.marvis.utils.swing.DialogSaveGraphic;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author manuel
 */
public class PopupMenuNetworkViewer extends JPopupMenu {

	private final MarvisGraphMainWindow main_window;
	private final VisualizationViewerGraph graphview;
	private final GraphView graph;
	private GraphObject current_object = null;
	private JMenuItem item_node_info = new JMenuItem("Node information"),
			item_node_hide = new JMenuItem("Hide node");

	public PopupMenuNetworkViewer(final MarvisGraphMainWindow main_window, final VisualizationViewerGraph graphview, GraphView graph) {
		super("Network menu");
		this.main_window = main_window;
		this.graphview = graphview;
		this.graph = graph;

		add(item_node_info);
		item_node_info.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PopupMenuNetworkViewer.this.showNodeInformation();
			}
		});

		if (graph instanceof GraphViewCustomizable) {
			add(item_node_hide);
			item_node_hide.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					PopupMenuNetworkViewer.this.hideNode();
				}
			});
		}

		JMenuItem item = new JMenuItem("Export graphics...");
		add(item);
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PopupMenuNetworkViewer.this.exportGraphics();
			}
		});


		// Add the mouselistener to the menu to detect the popup request
		graphview.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					PopupMenuNetworkViewer.this.popup(e.getPoint());
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
		current_object = null;
		Object vertex = graphview.getPickSupport().getVertex(graphview.
				getGraphLayout(), point.getX(), point.getY());
		if (vertex instanceof GraphObject) {
			current_object = (GraphObject) vertex;
		}

		item_node_info.setEnabled(current_object != null);
		item_node_hide.setEnabled(current_object != null);

		show(graphview, (int) point.getX(), (int) point.getY());
	}

	private void showNodeInformation() {
		if (current_object == null) {
			return;
		}
		main_window.createGraphobjectVisualization(current_object);
	}

	private void exportGraphics() {
		DialogSaveGraphic d = new DialogSaveGraphic(main_window, graphview);
		if (d.showDialog()) {
			d.writeToFileTried();
		}

	}

	private void hideNode() {
		if (current_object != null && graph instanceof GraphViewCustomizable) {
			((GraphViewCustomizable) graph).hideGraphobject(current_object);
		}
	}
}
