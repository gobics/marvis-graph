/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.utils.task.AbstractTask;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import javax.swing.JComponent;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 * @author manuel
 */
public class GraphicExportSVG extends AbstractTask<Void, Void> {

	private final File destination;
	private final JComponent visualization;

	public GraphicExportSVG(JComponent vv, File destination) {
		this.destination = destination;
		this.visualization = vv;
	}

	@Override
	protected Void doTask() throws Exception {
		// Get a DOMImplementation.
		DOMImplementation domImpl =
				GenericDOMImplementation.getDOMImplementation();
		// Create an instance of org.w3c.dom.Document.
		Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);

		// Create an instance of the SVG Generator.
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		// Ask the test to render into the SVG Graphics2D implementation.
		visualization.paint(svgGenerator);
		
		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		Writer out = new FileWriter(destination);
		svgGenerator.stream(out);

		return null;
	}
}
