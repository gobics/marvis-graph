package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.MetabolicNetwork;
import java.util.logging.Logger;

/**
 * An abstract class for sub-network scoring methods.
 *
 * @author manuel
 */
public abstract class AbstractGraphScore {

	private MetabolicNetwork parent = null;
	protected static final Logger logger = Logger.getLogger(AbstractGraphScore.class.
			getName());

	public AbstractGraphScore(MetabolicNetwork parent) {
		setParent(parent);
	}

	public AbstractGraphScore() {
	}

	protected MetabolicNetwork getParent() {
		return parent;
	}

	public void setParent(MetabolicNetwork parent) {
		this.parent = parent;
	}

	public abstract Comparable calculateScore(MetabolicNetwork graph);

	public abstract String getName();

	public abstract String getDescription();
}
