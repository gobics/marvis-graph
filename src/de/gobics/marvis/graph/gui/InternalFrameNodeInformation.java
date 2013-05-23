package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.utils.StringUtils;
import de.gobics.marvis.utils.swing.ButtonBrowseUri;
import de.gobics.marvis.utils.swing.SpringUtilities;
import de.gobics.marvis.utils.swing.TextPopupMenu;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.jfree.chart.ChartPanel;

public class InternalFrameNodeInformation extends JInternalFrame {

	private static Logger logger = Logger.getLogger(InternalFrameNodeInformation.class.
			getName());
	private static final long serialVersionUID = 1L;
	private static final Dimension defaultDimensionForDescription = new Dimension(200, 100);
	private final MetabolicNetwork graph;
	private final GraphObject displayObject;
	JPanel info_panel = new JPanel();
	private JPanel button_panel = new JPanel();
	private final MarvisGraphMainWindow main_window;

	public InternalFrameNodeInformation(MarvisGraphMainWindow main_window, MetabolicNetwork graph, GraphObject obj) {
		super("Node information",
				true, // resizable
				true, // closable
				true, // maximizable
				true);
		if (obj.getName() != null) {
			setTitle(obj.getClass().getSimpleName() + ": " + obj.getName());
		}
		else {
			setTitle(obj.getClass().getSimpleName() + ": " + obj.getId());
		}

		this.main_window = main_window;

		this.displayObject = obj;
		this.graph = graph;

		this.setLayout(new BorderLayout());
		this.info_panel.setLayout(new SpringLayout());

		this.add(info_panel, BorderLayout.PAGE_START);
		this.add(button_panel, BorderLayout.PAGE_END);

		this.displayInformation();
	}

	public GraphObject getGraphObject() {
		return displayObject;
	}

	public MetabolicNetwork getGraph() {
		return graph;
	}

	public void displayInformation() {
		try {
			this.throwingDisplayInformation();
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Can not display information about " + getGraphObject() + ":", e);
			JOptionPane.showMessageDialog(this, "Can not display information about: " + getGraphObject(), "Exception", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void throwingDisplayInformation() throws Exception {
		this.info_panel.removeAll();

		if (this.displayObject == null) {
			return;
		}

		logger.finer("Display information about: " + this.displayObject);
		// Draw the new informations
		if (this.displayObject instanceof Marker) {
			this.displayInformation((Marker) this.displayObject);
		}
		else if (this.displayObject instanceof Compound) {
			this.displayInformation((Compound) this.displayObject);
		}
		else if (this.displayObject instanceof Reaction) {
			this.displayInformation((Reaction) this.displayObject);
		}
		else if (this.displayObject instanceof Enzyme) {
			this.displayInformation((Enzyme) this.displayObject);
		}
		else if (this.displayObject instanceof Gene) {
			this.displayInformation((Gene) this.displayObject);
		}
		else if (this.displayObject instanceof Transcript) {
			this.displayInformation((Transcript) this.displayObject);
		}
		else if (this.displayObject instanceof Pathway) {
			this.displayInformation((Pathway) this.displayObject);
		}
		else {
			logger.log(Level.SEVERE, "Can not display information about " + getGraphObject() + ": Unkown class type");
			JOptionPane.showMessageDialog(this, "Can not display information about: " + getGraphObject(), "Unkown object", JOptionPane.ERROR_MESSAGE);
		}

		this.info_panel.add(new JLabel("Is Explainable:"));
		Boolean explainable = graph.isExplainable(displayObject);
		this.info_panel.add(new JLabel(explainable.toString()));

		SpringUtilities.makeCompactGrid(this.info_panel);

		// Add Buttons
		try {
			if (displayObject.getUrl() != null) {
				ButtonBrowseUri b = new ButtonBrowseUri(new URI(displayObject.
						getUrl()));
				button_panel.add(b);
			}
		}
		catch (URISyntaxException ex) {
			logger.log(Level.WARNING, "Can not parse url '" + displayObject.
					getUrl() + "': ", ex);
			JOptionPane.showMessageDialog(this, "Can not parse url: " + displayObject.
					getUrl(), "Warning", JOptionPane.WARNING_MESSAGE);
		}

		addIntensityProfile();

		this.pack();
		this.updateUI();
		this.setMinimumSize(this.getSize());
	}

	private void displayInformation(Marker m) {
		addTextfield("Id", m.getId());
		addTextfield("Mass", m.getMass().toString());
		addTextfield("Weight", m.getScore().toString());
		addTextfield("Retentiontime", Float.toString(m.getRetentionTime()));
		addTextfield("Annotations", Integer.toString(graph.getAnnotations(m).
				size()));
		if (m.getAdditionalData() != null) {
			addTextfield("Additional data", StringUtils.join(";", m.
					getAdditionalData()));
		}
		addRelationshipCombobox("Annotated metabolites", graph.getAnnotations(m));
	}

	private void displayInformation(Compound c) {
		addTextfield("Id", c.getId());
		addTextfield("Name", c.getName());
		addTextfield("Mass", Float.toString(c.getMass()));
		addTextfield("Formula", c.getFormula());
		addRelationshipCombobox("Metabolic marker", graph.getAnnotatingMarker(c));
		addRelationshipCombobox("Reactions", graph.getReactions(c));
		addTextArea("Description", c.getDescription());

	}

	private void displayInformation(Reaction r) {
		addTextfield("Id", r.getId());
		addTextfield("Name", r.getName());
		addTextArea("Equation", r.getEquation());
		addTextArea("Description", r.getDescription());
		addRelationshipCombobox("Substrates", graph.getSubstrates(r));
		addRelationshipCombobox("Products", graph.getProducts(r));
		addRelationshipCombobox("Enzymes", graph.getEnzymes(r));
		addRelationshipCombobox("Pathways", graph.getPathways(r));
	}

	private void displayInformation(Enzyme e) {
		addTextfield("Id", e.getId());
		addTextfield("Name", e.getName());
		addTextArea("Description", e.getDescription());
		addRelationshipCombobox("Reactions", graph.getReactions(e));
		addRelationshipCombobox("Genes", graph.getEncodingGenes(e));
	}

	private void displayInformation(Gene g) {
		addTextfield("Id", g.getId());
		addTextfield("Name", g.getName());
		addTextArea("Definition", g.getDefinition());
		addRelationshipCombobox("Transcripts", graph.getTranscripts(g));
		addRelationshipCombobox("Enzymes", graph.getEncodedEnzymes(g));
	}

	private void displayInformation(Pathway p) {
		addTextfield("Id", p.getId());
		addTextfield("Name", p.getName());
		addTextArea("Description", p.getDescription());
		addRelationshipCombobox("Reactions", graph.getReactions(p));
	}

	private void displayInformation(Transcript t) {
		addTextfield("Id", t.getId());
		addTextfield("Weight", "" + t.getScore());
		addRelationshipCombobox("Genes", graph.getGenes(t));
	}

	public GraphObject getObject() {
		return this.displayObject;
	}

	private void addIntensityProfile() {
		IntensityProfile ip = null;
		Color[] colors = new Color[]{Color.RED, Color.ORANGE};
		boolean display_conditions = true;


		if (displayObject instanceof ExperimentalMarker) {
			ip = ((ExperimentalMarker) displayObject).getIntensityProfile();
			colors[0] = Color.YELLOW;
			display_conditions = false;
		}
		else if (displayObject instanceof Gene && graph.getTranscripts((Gene) displayObject).
				size() > 0) {
			Gene g = (Gene) displayObject;
			ip = new IntensityProfile();
			for (Transcript t : graph.getTranscripts(g)) {
				ip.add(t.getIntensityProfile());
			}
		}
		else if (displayObject instanceof Compound && graph.getAnnotatingMarker((Compound) displayObject).
				size() > 0) {
			Compound g = (Compound) displayObject;
			ip = new IntensityProfile();
			for (Marker m : graph.getAnnotatingMarker(g)) {
				ip.add(m.getIntensityProfile(), " " + m.getId());
			}
		}

		if (ip != null && ip.sizeRaw() > 0) {
			ChartPanel panel = IntensityProfileHistogram.createHistogram(ip, display_conditions);
			this.add(panel, BorderLayout.CENTER);

		}
	}

	private void addTextArea(String label, String content) {
		info_panel.add(new JLabel(label));

		JTextArea editorPane = new JTextArea(content);
		editorPane.setLineWrap(true);
		editorPane.setWrapStyleWord(true);
		editorPane.setEditable(false);

		TextPopupMenu.create(editorPane);
		JScrollPane spane = new JScrollPane(editorPane);
		spane.setPreferredSize(defaultDimensionForDescription);
		info_panel.add(spane);
	}

	private void addTextfield(String label, String content) {
		info_panel.add(new JLabel(label));

		JTextField field = new JTextField(content);
		Dimension dimension = field.getPreferredSize();
		dimension.width = defaultDimensionForDescription.width;
		field.setPreferredSize(dimension);
		field.setScrollOffset(0);
		field.setCaretPosition(0);
		TextPopupMenu.create(field);
		info_panel.add(field);
	}

	@Override
	public boolean equals(Object other) {
		if (!getClass().equals(other.getClass())) {
			return false;
		}
		InternalFrameNodeInformation other_frame = getClass().cast(other);
		return getGraph().equals(other_frame.getGraph())
				&& getGraphObject().equals(other_frame.getGraphObject());
	}

	private <T extends GraphObject> void addRelationshipCombobox(String label, Collection<T> neighbors) {
		final JComboBox<GraphObjectItem> cb = new JComboBox<>();
		for (T n : neighbors) {
			cb.addItem(new GraphObjectItem(n));
		}

		cb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				GraphObjectItem item = (GraphObjectItem) cb.getSelectedItem();
				main_window.createGraphobjectVisualization(item.object);
			}
		});

		info_panel.add(new JLabel(label + ":"));
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(cb, BorderLayout.CENTER);
		panel.add(new JLabel("(" + neighbors.size() + ")"), BorderLayout.LINE_END);
		info_panel.add(panel);
	}

	private static class GraphObjectItem {

		public final GraphObject object;

		public GraphObjectItem(GraphObject o) {
			this.object = o;
		}

		@Override
		public String toString() {
			if (object.getName() != null && !object.getName().isEmpty()) {
				String name = object.getName();
				if (name.length() > 50) {
					name = name.substring(0, 50) + "...";
				}

				return name + " (" + object.getId() + ")";
			}
			return object.getId();
		}
	}
}
