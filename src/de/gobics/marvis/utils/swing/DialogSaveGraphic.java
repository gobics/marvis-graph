/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.swing.filechooser.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author manuel
 */
public class DialogSaveGraphic extends DialogOptions {

	private static final Logger logger = Logger.getLogger(DialogSaveGraphic.class.
			getName());
	private final FilechooserTextField filechooser = new FilechooserTextField(ChooserImage.
			getInstance(), true);
	private final PreviewPanel preview;
	private final JComponent base;
	
	public DialogSaveGraphic(final Window parent, final JComponent component) {
		super(parent);
		setTitle("Save graphic as image");
		base = component;

		// Image
		JPanel panel_options = getMainPanel();
		panel_options.setLayout(new BorderLayout());

		preview = new PreviewPanel(createImage());
		panel_options.add(preview, BorderLayout.CENTER);
		//panel_options.add(component, BorderLayout.CENTER);


		// Image options
		JPanel others = new JPanel(new SpringLayout());
		panel_options.add(others, BorderLayout.PAGE_END);

		// File
		JPanel filepanel = new JPanel(new BorderLayout());
		others.add(filepanel);
		filepanel.add(new JLabel("Save to:"), BorderLayout.LINE_START);
		filepanel.add(filechooser, BorderLayout.CENTER);

		SpringUtilities.makeCompactGrid(others);

		pack();
	}

	public BufferedImage createImage() {
		Color former_background = base.getBackground();
		Dimension fomer_size = base.getSize();

		base.setBackground(Color.WHITE);


		BufferedImage image = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics image_graphics = image.getGraphics();
		base.paint(image_graphics);

		base.setBackground(former_background);
		base.setSize(fomer_size);
		return image;
	}

	public void writeToFile() throws IOException {
		if (filechooser.getSelectedFile() == null) {
			filechooser.showChooserDialog();
		}
		if (filechooser.getSelectedFile() == null) {
			return;
		}
		writeToFile(filechooser.getSelectedFile());
	}

	public void writeToFile(File output) throws IOException {
		logger.fine("Writing image to output file: " + output);
		if (output.exists()) {
			logger.finer("File exists. Ask for confirmation");
			int result = JOptionPane.showConfirmDialog(this, "File exists. Overwrite?", "File exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result != JOptionPane.YES_OPTION) {
				return;
			}
			logger.warning("Overwriting existing output file: " + output);
		}



		OutputStream out = new FileOutputStream(output);
		BufferedImage image = createImage();

		if (new FileFilterImagePng().accept(output)) {
			javax.imageio.ImageIO.write(image, "png", out);
		}
		else if (new FileFilterImageJpg().accept(output)) {
			javax.imageio.ImageIO.write(image, "jpg", out);
		}
		else {
			throw new IOException("Can not detect output type for image file: " + output.
					getName());
		}

		out.close();
		JOptionPane.showMessageDialog(this, "Image saved to: " + output.
				getAbsoluteFile(), "Image saved", JOptionPane.INFORMATION_MESSAGE);

	}

	public void writeToFileTried() {
		if (filechooser.getSelectedFile() == null) {
			filechooser.showChooserDialog();
		}
		if (filechooser.getSelectedFile() == null) {
			return;
		}
		writeToFileTried(filechooser.getSelectedFile());
	}

	public void writeToFileTried(File file) {
		if (file == null) {
			return;
		}

		try {
			writeToFile(file);
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Can not write to file: ", ex);
			new DialogError(this,
					"Can not write to file " + file.getPath() + ": ", ex.
					getMessage()).setVisible(true);
		}
	}
}

class PreviewPanel extends JPanel {

	private final BufferedImage image;

	public PreviewPanel(BufferedImage image) {
		this.image = image;
		setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(image.getWidth(), image.getHeight());
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, null);
	}
}
