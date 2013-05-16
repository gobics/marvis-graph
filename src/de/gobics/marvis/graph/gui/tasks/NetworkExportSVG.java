/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.GraphObject;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.gui.graphvisualizer.VisualizationViewerGraph;
import de.gobics.marvis.utils.ColorUtils;
import de.gobics.marvis.utils.StringUtils;
import de.gobics.marvis.utils.task.AbstractTask;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author manuel
 */
public class NetworkExportSVG extends AbstractTask<Void, Void> {

	private static final Logger logger = Logger.getLogger(NetworkExportSVG.class.getName());
	private static final String svgNS = "http://www.w3.org/2000/svg";
	private static final int font_size = 14;
	private final File destination;
	private final VisualizationViewerGraph visualization;
	private Document document;
	private final TreeMap<GraphObject, Point2D> position = new TreeMap<>();
	

	public NetworkExportSVG(VisualizationViewerGraph vv, File destination) {
		this.destination = destination;
		this.visualization = vv;
	}

	@Override
	protected Void doTask() throws Exception {
		position.clear();
		// Get a DOMImplementation.
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		// Create an instance of org.w3c.dom.Document.
		document = domImpl.createDocument(svgNS, "svg", null);

		// Get the root element (the 'svg' element).
		Element svgRoot = document.getDocumentElement();
		svgRoot.setAttributeNS(svgNS, "width", Integer.toString(visualization.getSize().width));
		svgRoot.setAttributeNS(svgNS, "height", Integer.toString(visualization.getSize().height));
		
		Element paths = document.createElementNS(svgNS, "g");
		svgRoot.appendChild(paths);

		for(Relation r : visualization.getGraphLayout().getGraph().getEdges()){
			paths.appendChild(createPath(r));
		}
		
		for (GraphObject o : visualization.getGraphLayout().getGraph().getVertices()) {
			svgRoot.appendChild(createElement(o));
		}

		document.normalize();
		logger.finer("Exporting to file: " + destination);
		Writer output = new FileWriter(destination);
		DOMUtilities.writeDocument(document, output);
		output.close();

		return null;
	}

	private Element createElement(GraphObject obj) {
		Element group = document.createElementNS(svgNS, "g");
		Element e;
		
		Element t = document.createElementNS(svgNS, "text");
		t.setTextContent(obj.getName() != null ? obj.getName() : obj.getId());
		t.setAttribute("style", "font-size: " + font_size + "px;");
		group.appendChild(t);

		// Calculate the general shape
		Shape shape = visualization.getRenderContext().getVertexShapeTransformer().transform(obj);
		logger.finer("Rendering shape: "+shape);
		if (shape instanceof Rectangle2D) {
			Rectangle2D rect = (Rectangle2D) shape;
			e = document.createElementNS(svgNS, "rect");
			double x = visualization.getGraphLayout().transform(obj).getX() - (rect.getWidth() / 2);
			double y = visualization.getGraphLayout().transform(obj).getY() - (rect.getHeight() / 2);
			e.setAttribute("x", Double.toString(x));
			e.setAttribute("y", Double.toString(y));
			e.setAttribute("width", Double.toString(rect.getWidth()));
			e.setAttribute("height", Double.toString(rect.getHeight()));
			
			t.setAttribute("x", Double.toString(x + rect.getWidth() + 5));
			t.setAttribute("y", Double.toString(y + rect.getHeight() + 5 + font_size));

		}
		else if (shape instanceof Ellipse2D) {
			Ellipse2D ellipse = (Ellipse2D) shape;
			if (ellipse.getWidth() == ellipse.getHeight()) {
				e = document.createElementNS(svgNS, "circle");
				e.setAttributeNS(svgNS, "r", Double.toString(ellipse.getWidth() / 2));
			}
			else {
				e = document.createElementNS(svgNS, "ellipse");
				e.setAttributeNS(svgNS, "rx", Double.toString(ellipse.getWidth() / 2));
				e.setAttributeNS(svgNS, "ry", Double.toString(ellipse.getHeight() / 2));
			}
			double x = visualization.getGraphLayout().transform(obj).getX();
			double y = visualization.getGraphLayout().transform(obj).getY();
			e.setAttributeNS(svgNS, "cx", Double.toString(x));
			e.setAttributeNS(svgNS, "cy", Double.toString(y));

			t.setAttribute("x", Double.toString(x + (ellipse.getWidth() / 2)));
			t.setAttribute("y", Double.toString(y + (ellipse.getHeight() / 2) + font_size));

		}
		else {
			e = document.createElementNS(svgNS, "circle");
			double x = visualization.getGraphLayout().transform(obj).getX();
			double y = visualization.getGraphLayout().transform(obj).getY();
			double radius = 10;
			e.setAttributeNS(svgNS, "cx", Double.toString(x - radius));
			e.setAttributeNS(svgNS, "cy", Double.toString(y - radius));
			e.setAttributeNS(svgNS, "r", Double.toString(radius));
			t.setAttribute("x", Double.toString(x + radius));
			t.setAttribute("y", Double.toString(y + radius + font_size));
		}

		group.appendChild(e);

		StringBuilder style = new StringBuilder();

		// Fill color
		Color fill_color = (Color) visualization.getRenderContext().getVertexFillPaintTransformer().transform(obj);
		String fill = ColorUtils.getHTMLColor(fill_color);
		style.append("fill:").append(fill).append(";");

		// Stroke
		style.append("stroke:#000000; stroke-opacity:1;");
		BasicStroke stroke = (BasicStroke) visualization.getRenderContext().getVertexStrokeTransformer().transform(obj);
		String stroke_width = Float.toString(stroke.getLineWidth());
		style.append("stroke-width: ").append(stroke_width).append(";");

		float[] dash_array = stroke.getDashArray();
		if (dash_array != null) {
			style.append("stroke-dasharray: ").append(StringUtils.join(",", dash_array));
		}

		// Set the attributes
		e.setAttributeNS(svgNS, "id", obj.getClass().getSimpleName() + "-" + obj.getId());
		e.setAttributeNS(svgNS, "style", style.toString());

		return group;
	}

	private Element createPath(Relation r) {
		Element line = document.createElementNS(svgNS, "path");
		
		Point2D start = visualization.getGraphLayout().transform(r.getStart());
		Point2D end = visualization.getGraphLayout().transform(r.getEnd());
		
		StringBuilder definition = new StringBuilder();
		definition.append("M").append(Double.toString(start.getX())).append(" ").append(Double.toString(start.getY()));
		definition.append("L").append(Double.toString(end.getX())).append(" ").append(Double.toString(end.getY()));
		line.setAttribute("d", definition.toString());
		
		BasicStroke stroke = (BasicStroke) visualization.getRenderContext().getEdgeStrokeTransformer().transform(r);
		String stroke_width = Float.toString(stroke.getLineWidth());
		StringBuilder style = new StringBuilder();
		style.append("stroke: #000000;");
		style.append("stroke-width: ").append(stroke_width).append(";");

		float[] dash_array = stroke.getDashArray();
		if (dash_array != null) {
			style.append("stroke-dasharray: ").append(StringUtils.join(",", dash_array));
		}
		
		line.setAttribute("style", style.toString());
		return line;
	}
}
