package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.utils.task.AbstractTask;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public abstract class AnnotateAbstract<M extends ExperimentalMarker> extends AbstractTask<MetabolicNetwork, Void> {

	private static final Logger logger = Logger.getLogger(AnnotateAbstract.class.
			getName());
	private final MetabolicNetwork network;
	private TreeMap<String, GraphObject> map_cache;

	public AnnotateAbstract(MetabolicNetwork graph) {
		this.network = graph.clone();
		setTaskDescription("Annotate experimental marker");
		setTaskTitle("Annotation process");
	}

	/**
	 * Returns the metabolic network in which the annotations are inserted.
	 * @return metabolic network
	 */
	protected MetabolicNetwork getNetwork() {
		return network;
	}

	/**
	 * Adds edges into the metabolic network as specified here
	 * @param type
	 * @param from
	 * @param targets 
	 */
	final protected void addAnnotations(RelationshipType type, ExperimentalMarker from, Set<? extends GraphObject> targets) {
		for (GraphObject to : targets) {
			network.addRelation(type, from, to);
		}
	}

	@Override
	final protected MetabolicNetwork doTask() throws Exception {
		Collection<M> marker = getExperimentalMarker();
		setProgressMax(marker.size());
		for (M m : marker) {
			annotate(m);

			if (isCanceled()) {
				return null;
			}
			incrementProgress();
		}
		return network;
	}

	/**
	 * Search for annotation of the marker based on the
	 * {@link ExperimentalMarker#getAnnotation() annotation} of the
	 * {@link ExperimentalMarker}.
	 *
	 * If no annotations were given, the method will return
	 * <code>null</code>.
	 *
	 * @param target_class the target class, e.g. {@link Compound}s or
	 * {@link Gene}s
	 * @param marker the object to fetch the annotations from
	 * @return a set of {@link  GraphObject} which ids matched the annotations
	 * from the marker, or {@code null} if the {@link  ExperimentalMarker} does
	 * not has an annotation.
	 */
	protected Set<GraphObject> getAnnotations(Class<? extends GraphObject> target_class, M marker) {
		if (!marker.hasAnnotation()) {
			return null;
		}
		Set<GraphObject> result = new TreeSet<>();

		Set<String> annotations = new TreeSet<>(Arrays.asList(marker.getAnnotation().split("\\s+")));
		for (GraphObject go : network.getAllObjects(target_class)) {
			if (annotations.contains(go.getId())) {
				result.add(go);
			}
		}
		return result;
	}

	/**
	 * Performs an annotation of the object.
	 *
	 * @param object
	 */
	protected abstract void annotate(M object);

	protected abstract Collection<M> getExperimentalMarker();
}
