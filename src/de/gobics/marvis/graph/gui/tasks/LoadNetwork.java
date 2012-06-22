/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.*;
import java.util.logging.Logger;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.SwingWorker;
import org.jdom.*;
import org.jdom.input.*;

/**
 *
 * @author manuel
 */
public class LoadNetwork extends SwingWorker<MetabolicNetwork, Void> {

	private final static Logger logger = Logger.getLogger(LoadNetwork.class.
			getName());
	public final static String uri = "http://www.gobics.de/marvis-graph";
	private File filename;
	private final MetabolicNetwork temporaryMetabolicNetwork = new MetabolicNetwork();

	public LoadNetwork(String filename) {
		this(new File(filename));
	}

	public LoadNetwork(File input_file) {
		// Runs on the EDT.  Copy GUI state that
		// doInBackground() depends on from parameters
		// to LoadNetwork fields, here.
		super();
		this.filename = input_file;
	}

	@Override
	protected MetabolicNetwork doInBackground() throws Exception {
		return this.load();
	}

	public MetabolicNetwork load() throws Exception {
		logger.info("Importing file " + filename);

		getPropertyChangeSupport().firePropertyChange("description", null, "Loading network");

		if (filename.getAbsolutePath().toLowerCase().endsWith(".zip")) {
			return this.loadZip(filename);
		}
		else if (filename.getAbsolutePath().toLowerCase().endsWith(".xml.gz")) {
			return this.loadGZip(filename);
		}
		else if (filename.getAbsolutePath().toLowerCase().endsWith(".xml")) {
			return this.loadFromInputstream(new FileInputStream(filename));
		}

		throw new IllegalArgumentException(
				"Can not determine filetype of file: " + filename.getName());
	}

	@SuppressWarnings("unchecked")
	private MetabolicNetwork loadFromInputstream(InputStream instream) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(instream);
		Element root = doc.getRootElement();

		setProgress(0);

		if (!root.getAttribute("version").getValue().equals("0.2")) {
			throw new Exception(
					"Version of the MarVis-MetabolicNetwork export file is not '0.2' but: "
					+ root.getAttribute("version").getValue());
		}

		if (root.getAttribute("name") != null) {
			temporaryMetabolicNetwork.setName(root.getAttributeValue("name"));
		}
		if (root.getAttribute("hasMarkers") != null && root.getAttribute("hasMarkers").
				getValue().equals(("true"))) {
			temporaryMetabolicNetwork.setHasMarkers();
		}
		if (root.getAttribute("hasMarkers") != null && root.getAttribute("hasTranscripts").
				getValue().equals("true")) {
			temporaryMetabolicNetwork.setHasTranscripts();
		}

		List<Element> children = root.getChildren();
		logger.fine("Importing "+ children.size()+" objects");
		int counter = 1;
		int max = children.size() / 100 + 1; // Like to get percentages
		if (max < 1) { // prevent NullPointerException if child count is < 100
			max = 1;
		}
		for (Element e : children) {
			if (e.getName().equals("IntensityConfiguration")) {
				continue;
			}
			
			//logger.info("Importing object ("+ counter +"/"+ children.size()+"):" + e.getName());
			this.create_object_from_element(e);
			setProgress(Math.min(counter++ / max, 100));

			if (this.isCancelled()) {
				return null;
			}
		}

		setProgress(100);
		logger.info("Loaded MetabolicNetwork ready");
		return temporaryMetabolicNetwork;

	}

	private MetabolicNetwork loadZip(File file) throws Exception {
		logger.finer("Import zip archive " + file);
		ZipFile zip = new ZipFile(file);
		ZipEntry entry = zip.entries().nextElement();
		return loadFromInputstream(zip.getInputStream(entry));
	}

	private MetabolicNetwork loadGZip(File file) throws Exception {
		logger.finer("Load MetabolicNetwork from gzip " + file);
		GZIPInputStream zis = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
		return this.loadFromInputstream(zis);
	}

	private MetabolicNetwork loadMgd(File file) throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		MetabolicNetwork n = (MetabolicNetwork) ois.readObject();
		ois.close();
		return n;
	}

	@SuppressWarnings("unchecked")
	private GraphObject create_object_from_element(Element o) throws Exception {
		GraphObject object = null;

		String name = o.getName();
		String id = o.getAttributeValue("id");

		if (name.equals(Marker.class.getSimpleName())) {
			Marker m = this.temporaryMetabolicNetwork.createMarker(id);
			if (o.getAttributeValue("mass") != null) {
				m.setMass(new Double(o.getAttributeValue("mass")));
			}

			if (o.getAttribute("weight") != null) {
				m.setWeight(new Double(o.getAttributeValue("weight")).
						doubleValue());
			}
			if (o.getAttribute("retentiontime") != null) {
				m.setRetentionTime(new Double(o.getAttributeValue("retentiontime")).
						floatValue());
			}

			List<Element> childs = o.getChildren();
			String[] intensity_names = null;
			for (int i = 0; i < childs.size(); i++) {
				Element child = childs.get(i);
				if (child.getName().equals("intensity_names")) {
					intensity_names = child.getTextTrim().split(";");
				}
				else if (child.getName().equals("intensity_data")) {
					float[] intensity = new float[intensity_names.length];
					String[] serialized_intensities = child.getTextTrim().split(";");
					for (int col = 0; col < intensity.length; col++) {
						intensity[col] = new Float(serialized_intensities[col]);
					}
					// logger.finer("Setting intensity for "+m+": "+Arrays.toString(intensity_names)+Arrays.toString(intensity));
					m.setIntensity(intensity_names, intensity);
				}
			}
			object = m;
		}
		else if (name.equals(Compound.class.getSimpleName())) {
			Compound c = this.temporaryMetabolicNetwork.createCompound(id);
			if (o.getAttributeValue("mass") != null) {
				c.setMass(new Float(o.getAttributeValue("mass")));
			}
			if (o.getAttributeValue("formula") != null) {
				c.setFormula(o.getAttributeValue("formula"));
			}

			List<Element> childs = o.getChildren();
			for (int i = 0; i < childs.size(); i++) {
				Element child = childs.get(i);
				if (child.getName().equals("description")) {
					c.setDescription(child.getTextTrim());
				}
				else if (child.getName().toLowerCase().equals("name")) {
					c.setName(child.getTextTrim());
				}
				else if (child.getName().toLowerCase().equals("formula")) {
					c.setFormula(child.getTextTrim());
				}
			}

			object = c;
		}
		else if (name.equals(Reaction.class.getSimpleName())) {
			Reaction r = this.temporaryMetabolicNetwork.createReaction(id);

			List<Element> childs = o.getChildren();
			for (int i = 0; i < childs.size(); i++) {
				Element child = childs.get(i);
				if (child.getName().equals("description")) {
					r.setDescription(child.getTextTrim());
				}
				else if (child.getName().toLowerCase().equals("name")) {
					r.setName(child.getTextTrim());
				}
				else if (child.getName().toLowerCase().equals("equation")) {
					r.setEquation(child.getTextTrim());
				}

			}

			object = r;
		}
		else if (name.equals(Enzyme.class.getSimpleName())) {
			Enzyme enz = this.temporaryMetabolicNetwork.createEnzyme(id);

			List<Element> childs = o.getChildren();
			for (int i = 0; i < childs.size(); i++) {
				Element child = childs.get(i);
				if (child.getName().equals("description")) {
					enz.setDescription(child.getTextTrim());
				}
				else if (child.getName().toLowerCase().equals("name")) {
					enz.setName(child.getTextTrim());
				}
			}

			object = enz;
		}
		else if (name.equals(Gene.class.getSimpleName())) {
			Gene g = this.temporaryMetabolicNetwork.createGene(id);

			List<Element> childs = o.getChildren();
			for (int i = 0; i < childs.size(); i++) {
				Element child = childs.get(i);
				if (child.getName().toLowerCase().equals("name")) {
					g.setName(child.getTextTrim());
				}
				else if (child.getName().toLowerCase().equals("definition")) {
					g.setDefinition(child.getTextTrim());
				}
			}

			object = g;
		}
		else if (name.equals(Pathway.class.getSimpleName())) {
			Pathway p = this.temporaryMetabolicNetwork.createPathway(id);

			List<Element> childs = o.getChildren();
			for (int i = 0; i < childs.size(); i++) {
				Element child = childs.get(i);
				if (child.getName().equals("description")) {
					p.setDescription(child.getTextTrim());
				}
				else if (child.getName().toLowerCase().equals("name")) {
					p.setName(child.getTextTrim());
				}
			}

			object = p;
		}
		else if (name.equals(Transcript.class.getSimpleName())) {
			Transcript transcript = this.temporaryMetabolicNetwork.
					createTranscript(id);

			if (o.getAttribute("weight") != null) {
				transcript.setWeight(new Double(o.getAttributeValue("weight")).
						doubleValue());
			}


			List<Element> childs = o.getChildren();
			String[] intensity_names = null;
			for (int i = 0; i < childs.size(); i++) {
				Element child = childs.get(i);
				if (child.getName().equals("intensity_names")) {
					intensity_names = child.getTextTrim().split(";");
				}
				else if (child.getName().equals("intensity_data")) {
					float[] intensity = new float[intensity_names.length];
					String[] serialized_intensities = child.getTextTrim().split(";");
					for (int col = 0; col < intensity.length; col++) {
						intensity[col] = new Float(serialized_intensities[col]);
					}
					transcript.setIntensity(intensity_names, intensity);
				}
			}

			object = transcript;
		}
		else {
			throw new Exception("Unkown object: " + name);
		}

		if (o.getChild("url") != null) {
			object.setUrl(o.getChildText("url"));
		}

		List<Element> childs = o.getChildren("relation");
		for (int i = 0; i < childs.size(); i++) {
			this.create_relation_from_element(object, childs.get(i));
		}

		return object;
	}

	private void create_relation_from_element(GraphObject start, Element o)
			throws Exception {
		String type = o.getAttributeValue("type");
		String end = o.getAttributeValue("to");

		if (type.equals(RelationshipType.MARKER_ANNOTATION_COMPOUND.toString())) {
			temporaryMetabolicNetwork.annotates((Marker) start, temporaryMetabolicNetwork.
					createCompound(end));
		}
		else if (type.equals(RelationshipType.REACTION_HAS_PRODUCT.toString())) {
			temporaryMetabolicNetwork.hasProduct((Reaction) start, temporaryMetabolicNetwork.
					createCompound(end));
		}
		else if (type.equals(RelationshipType.REACTION_HAS_SUBSTRATE.toString())) {
			temporaryMetabolicNetwork.hasSubstrate((Reaction) start, temporaryMetabolicNetwork.
					createCompound(end));
		}
		else if (type.equals(RelationshipType.REACTION_NEEDS_ENZYME.toString())) {
			temporaryMetabolicNetwork.needsEnzyme((Reaction) start, temporaryMetabolicNetwork.
					createEnzyme(end));
		}
		else if (type.equals(RelationshipType.GENE_ENCODES_ENZYME.toString())) {
			temporaryMetabolicNetwork.encodesFor((Gene) start, temporaryMetabolicNetwork.
					createEnzyme(end));
		}
		else if (type.equals(RelationshipType.TRANSCRIPT_ISFROM_GENE.toString())) {
			temporaryMetabolicNetwork.isFrom((Transcript) start, temporaryMetabolicNetwork.
					createGene(end));
		}
		else if (type.equals(RelationshipType.REACTION_HAPPENSIN_PATHWAY.
				toString())) {
			temporaryMetabolicNetwork.happensIn((Reaction) start, temporaryMetabolicNetwork.
					createPathway(end));
		}
		else if (type.equals(RelationshipType.ENZYME_USEDIN_PATHWAY.toString())) {
			// ((Reaction)start).happensInPathway( MetabolicNetwork.createPathway(end) );
		}
		else {
			throw new Exception("Unkown relation type: " + type);
		}
	}
}