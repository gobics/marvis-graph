package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.swing.filechooser.ChooserImage;
import de.gobics.marvis.utils.swing.filechooser.ChooserSvg;
import de.gobics.marvis.utils.swing.filechooser.FileFilterImageGif;
import de.gobics.marvis.utils.swing.filechooser.FileFilterImageJpg;
import de.gobics.marvis.utils.swing.filechooser.FileFilterImagePng;
import de.gobics.marvis.utils.swing.filechooser.FileFilterSVG;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.filechooser.FileFilter;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Utility class to export some drawable AWT/Swing component into an SVG file.
 *
 * @author manuel
 */
public class ExportGraphic2D {

	public enum GraphicType {

		SVG, JPEG, GIF, PNG
	}

	public static void export(Component drawable) throws IOException {
		ChooserImage chooser = ChooserImage.getInstance();
		File destination = chooser.doChooseFileSave(drawable);
		if (destination == null) {
			return;
		}

		FileFilter ff = chooser.getFileFilter();

		// Check for SVG
		if (ff instanceof FileFilterSVG) {
			exportSVG(drawable, destination);
			return;
		}

		// Raster images
		String type = "png";
		if (ff instanceof FileFilterImageJpg) {
			type = "jpeg";
		}
		else if (ff instanceof FileFilterImageGif) {
			type = "gif";
		}

		exportRaster(drawable, type, destination);

	}

	public static void exportSVG(Component drawable, File destination) throws IOException {
		// Get the implementation
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);

		// Create an instance of the SVG Generator.
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		// Ask the test to render into the SVG Graphics2D implementation.
		drawable.paint(svgGenerator);

		// Finally, write to file
		boolean useCSS = true; // we want to use CSS style attributes
		try {
			FileWriter out = new FileWriter(destination);
			svgGenerator.stream(out, useCSS);
		}
		catch (Exception e) {
			Logger.getLogger(ExportGraphic2D.class.getName()).log(Level.FINE, "Can not export to " + destination + ": ", e);
			throw e;
		}
		Logger.getLogger(ExportGraphic2D.class.getName()).log(Level.FINE, "Exported graphic to: " + destination);
	}

	public static void exportRaster(Component drawable, String type, File destination) throws IOException {
		BufferedImage image = new BufferedImage(drawable.getSize().width, drawable.getSize().height, BufferedImage.TYPE_INT_ARGB);
		drawable.paint(image.getGraphics());
		ImageIO.write(image, type, destination);
	}
}
