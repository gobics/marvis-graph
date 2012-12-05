package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.utils.swing.AbstractTask;
import java.io.*;

import org.jdom.*;
import org.jdom.output.*;

import de.gobics.marvis.graph.*;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.swing.SwingWorker;

public class SaveNetwork extends AbstractTask<Void, Void> {

	private final static Logger logger = Logger.getLogger(SaveNetwork.class.
			getName());
	public final static String uri = "http://www.gobics.de/marvis-graph";
	private final MetabolicNetwork network;
	private final File file;

	public SaveNetwork(MetabolicNetwork network, File filename) {
		super();
		this.network = network;
		this.file = filename;
	}

	public SaveNetwork(MetabolicNetwork network, String filename) {
		super();
		this.network = network;
		this.file = new File(filename);
	}

	@Override
	protected Void performTask() throws Exception {
		save();
		return null;
	}

	public void save() throws Exception {
		sendDescription("Save network to file");
		if (file.getParentFile() != null && !file.getParentFile().exists() && !file.
				getParentFile().mkdirs()) {
			throw new IOException("Can not create directory");
		}

		if (this.file.getAbsolutePath().toLowerCase().endsWith(".xml")) {
			create_xml(new FileOutputStream(file));
		}
		else if (this.file.getAbsolutePath().toLowerCase().endsWith("gz")) {
			this.createGZip(this.file);
		}
		else {
			throw new IllegalArgumentException("Can not determine filetype for file: " + file.
					getName());
		}
		logger.finer("MetabolicNetwork saved");
	}

	private void create_xml(OutputStream outstream) throws IOException {
		Document doc = new Document();
		Element root = new Element("graph");
		root.setAttribute("version", "0.2");
		root.setAttribute("hasMarkers", Boolean.toString(network.hasMarkers()));
		root.setAttribute("hasTranscripts", Boolean.toString(network.
				hasTranscripts()));
		doc.setRootElement(root);

		if (network.getName() != null) {
			root.setAttribute("name", network.getName());
		}

		logger.finer("Saving intensity configuration");

		logger.finer("Exporting objects");

		setProgressMax(network.size());
		for (GraphObject o : network.getAllObjects()) {
			Element child = this.create_element_from_object(o);
			root.addContent(child);
			for (Relation r : network.getRelations(o)) {
				if (r.getStart().equals(o)) {
					Element e = new Element("relation");
					e.setAttribute("type", r.getType().toString());
					e.setAttribute("to", r.getEnd().getId());
					child.addContent(e);
				}
			}

			if (this.isCancelled()) {
				return;
			}
			incrementProgress();
		}

		XMLOutputter fmt = new XMLOutputter();
		fmt.setFormat(Format.getPrettyFormat()); // only for nicer formatting
		fmt.output(doc, outstream);
	}

	private Element create_element_from_object(GraphObject o) {
		Element e = new Element(o.getClass().getSimpleName().replace("Impl", ""));
		e.setAttribute("id", o.getId());

		if (o.getUrl() != null) {
			Element elt = new Element("url");
			elt.addContent(o.getUrl());
			e.addContent(elt);
		}

		if (o instanceof InputObject) {
			InputObject inputObject = (InputObject) o;
			if (inputObject.getWeight() != null) {
				e.setAttribute("weight", inputObject.getWeight().toString());
			}

			// Store intensity
			float[] intensity_data = inputObject.getRawIntensities();
			String[] intensity_names = inputObject.getRawIntensityNames();
			String store_data = "";
			String store_name = "";
			for (int i = 0; i < intensity_data.length; i++) {
				store_data += ";" + intensity_data[i];
				store_name += ";" + intensity_names[i];
			}
			if (store_name.length() > 0) {
				Element elt = new Element("intensity_names");
				elt.addContent(store_name.substring(1));
				e.addContent(elt);

				elt = new Element("intensity_data");
				elt.addContent(store_data.substring(1));
				e.addContent(elt);
			}
		}

		if (o instanceof Marker) {
			if (((Marker) o).getMass() != null) {
				e.setAttribute("mass", ((Marker) o).getMass().toString());
			}
			if (((Marker) o).getRetentionTime() >= 0) {
				e.setAttribute("retentiontime", Float.toString(((Marker) o).
						getRetentionTime()));
			}

		}
		else if (o instanceof Compound) {
			Compound c = (Compound) o;
			e.setAttribute("mass", Float.toString(c.getMass()));


			if (c.getFormula() != null) {
				Element formula = new Element("formula");
				e.addContent(formula);
				formula.addContent(c.getFormula());
			}

			if (c.getName() != null) {
				Element name = new Element("name");
				e.addContent(name);
				name.addContent(c.getName());
			}

			if (c.getDescription() != null) {
				Element desc = new Element("description");
				e.addContent(desc);
				desc.addContent(c.getDescription());
			}

		}
		else if (o instanceof Reaction) {
			Reaction r = (Reaction) o;
			if (r.getName() != null) {
				Element desc = new Element("name");
				e.addContent(desc);
				desc.addContent(r.getName());
			}
			if (r.getEquation() != null) {
				Element desc = new Element("equation");
				e.addContent(desc);
				desc.addContent(r.getEquation());
			}
			if (r.getDescription() != null) {
				Element desc = new Element("description");
				e.addContent(desc);
				desc.addContent(r.getDescription());
			}
		}
		else if (o instanceof Enzyme) {
			Enzyme enz = (Enzyme) o;

			if (enz.getName() != null) {
				Element name = new Element("name");
				e.addContent(name);
				name.addContent(enz.getName());
			}
			if (enz.getDescription() != null) {
				Element desc = new Element("description");
				e.addContent(desc);
				desc.addContent(enz.getDescription());
			}
		}
		else if (o instanceof Gene) {
			Gene g = (Gene) o;

			if (g.getName() != null) {
				Element name = new Element("name");
				e.addContent(name);
				name.addContent(g.getName());
			}
			if (g.getDefinition() != null) {
				Element elt_definition = new Element("definition");
				e.addContent(elt_definition);
				elt_definition.addContent(g.getDefinition());
			}

		}
		else if (o instanceof Pathway) {
			Pathway p = (Pathway) o;
			if (p.getName() != null) {
				Element name = new Element("name");
				e.addContent(name);
				name.addContent(p.getName());
			}
			if (p.getDescription() != null) {
				Element desc = new Element("description");
				e.addContent(desc);
				desc.addContent(p.getDescription());
			}
		}
		else if (o instanceof Transcript) {
		}

		return e;
	}

	private void createGZip(File zip) throws Exception {
		logger.finer("Export " + network + " to gzip " + zip);

		FileOutputStream dest = new FileOutputStream(zip);
		GZIPOutputStream out = new GZIPOutputStream(new BufferedOutputStream(dest));

		create_xml(out);

		out.close();
	}
}
