package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.MetabolicNetwork;
import java.util.*;
import java.util.logging.Logger;

public abstract class AbstractGraphSort implements Comparator<MetabolicNetwork> {

	private MetabolicNetwork parent = null;
	protected static final Logger logger = Logger.getLogger(AbstractGraphSort.class.getName());
	private HashMap<MetabolicNetwork, Comparable<Object>> cache = new HashMap<MetabolicNetwork, Comparable<Object>>();

	public AbstractGraphSort(){
	}
	
	public AbstractGraphSort(MetabolicNetwork parent) {
		setParent(parent);
	}

	protected MetabolicNetwork getParent() {
		return parent;
	}

	public void setParent(MetabolicNetwork parent) {
		this.parent = parent;
		clearCache();
	}

	public abstract Comparable calculateScore(MetabolicNetwork graph);

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int compare(MetabolicNetwork t1, MetabolicNetwork t2) {
		return getScore(t1).compareTo(getScore(t2));
	}

	@SuppressWarnings("unchecked")
	public Comparable<Object> getScore(MetabolicNetwork graph) {
		if (!cache.containsKey(graph)) {
			cache.put(graph, calculateScore(graph));
		}
		return cache.get(graph);
	}

	public void clearCache() {
		this.cache.clear();
	}

	public abstract String getName();
	public abstract String getDescription();
	
}
