package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Annotates the metabolic marker of a network either by their external
 * annotation or on a mass comparison.
 *
 * @author manuel
 */
public class AnnotateMarker extends AnnotateAbstract<Marker> {

	private double mass_range = 0.005;
	private final Map<Integer, Set<Compound>> mass_cache = new TreeMap<>();

	public AnnotateMarker(MetabolicNetwork graph) {
		super(graph);
		setTaskDescription("Annotate compounds with marker candidates");
	}

	public void setMassRange(double new_range) {
		mass_range = Math.abs(new_range);
	}

	public double getMassRange() {
		return mass_range;
	}

	@Override
	protected void annotate(Marker marker) {
		if (marker.hasAnnotation()) {
			addAnnotations(RelationshipType.MARKER_ANNOTATION_COMPOUND, marker, getAnnotations(Compound.class, marker));
		}
		else {
			addAnnotations(RelationshipType.MARKER_ANNOTATION_COMPOUND, marker, getMassAnnotations(marker));
		}
	}

	private Set<Compound> getMassAnnotations(Marker m) {
		// Build a cache containing the rounded mass values as keys and the corresponding
		// compounds as values
		if (mass_cache.isEmpty()) {
			for (Compound c : getNetwork().getCompounds()) {
				Integer rounded = ((Float) c.getMass()).intValue();
				if (!mass_cache.containsKey(rounded)) {
					mass_cache.put(rounded, new TreeSet<Compound>());
				}
				mass_cache.get(rounded).add(c);
			}

			System.out.println(mass_cache);
		}

		Set<Compound> targets = new TreeSet<>();
		int from = (int) Math.floor(m.getMass() - mass_range - 1);
		int to = (int) Math.ceil(m.getMass() + mass_range + 1);

		for (int rounded_mass = from; rounded_mass <= to; rounded_mass++) {
			System.out.println("Search mass " + rounded_mass + ": " + mass_cache.get(rounded_mass));
			if (mass_cache.containsKey(rounded_mass)) {
				for (Compound c : mass_cache.get(rounded_mass)) {
					if (Math.abs(c.getMass() - m.getMass()) <= mass_range) {
						targets.add(c);
					}
				}
			}
		}
		return targets;
	}

	@Override
	protected Collection<Marker> getExperimentalMarker() {
		return getNetwork().getMarkers();
	}
}
