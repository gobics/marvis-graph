/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.MetabolicNetwork;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author manuel
 */
public class AnnotateMarker extends SwingWorker<MetabolicNetwork, Void> {

	private static final Logger logger = Logger.getLogger(AnnotateMarker.class.
			getName());
	private final MetabolicNetwork network;
	private double mass_range = 0.005;

	public AnnotateMarker(MetabolicNetwork graph) {
		this.network = graph.clone();
	}

	public void setMassRange(double new_range) {
		mass_range = Math.abs(new_range);
	}

	public MetabolicNetwork annotate() throws Exception {
		Collection<Marker> markers = network.getMarkers();
		Collection<Compound> compounds = network.getCompounds();

		getPropertyChangeSupport().firePropertyChange("description", null, "Annotating marker");
		setProgress(0);

		if (markers.isEmpty()) {
			return null;
		}

		if (compounds.isEmpty()) {
			return null;
		}

		int current = 1;

		for (Marker m : markers) {
			boolean found_compound = false;
			for (Compound c : compounds) {
				if (Math.abs(m.getMass() - c.getMass()) <= mass_range) {
					network.annotates(m, c);
					found_compound = true;
				}
			}

			setProgress(current++ / markers.size());
		}

		return network;
	}

	@Override
	protected MetabolicNetwork doInBackground() throws Exception {
		return annotate();
	}
}
