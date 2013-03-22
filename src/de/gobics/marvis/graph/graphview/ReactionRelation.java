package de.gobics.marvis.graph.graphview;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.Reaction;
import de.gobics.marvis.graph.Relation;
import de.gobics.marvis.graph.RelationshipType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Specialized relation between two reactions used by the {@link ReactionGraph}.
 * Two reactions are consider to be related if they share a compound that
 * matches the requirements specified in the {@link ReactionGraph}.
 *
 * This special class is needed because it overwrites the {@code equals()} and
 * {@code compareTo()} methods to ensure symmetry between two relations: <ul>
 * <li> r1 ---- r2 </li> <li> r2 ---- r1 <li> </ul>
 *
 * @author manuel
 */
public class ReactionRelation extends Relation {

	private final Set<Compound> shares;

	public ReactionRelation(Reaction r1, Reaction r2) {
		super(RelationshipType.REACTION_SHARECOMPOUND_REACTION, r1, r2);
		shares = null;
	}

	public ReactionRelation(Reaction r1, Reaction r2, Compound[] shares) {
		super(RelationshipType.REACTION_SHARECOMPOUND_REACTION, r1, r2);
		this.shares = new TreeSet<Compound>(Arrays.asList(shares));
	}

	public ReactionRelation(Reaction r1, Reaction r2, Collection<Compound> shares) {
		super(RelationshipType.REACTION_SHARECOMPOUND_REACTION, r1, r2);
		this.shares = new TreeSet<Compound>(shares);
	}

	@Override
	public int compareTo(Relation other) {
		int c = getClass().getName().compareToIgnoreCase(other.getClass().getName());
		if (c != 0) {
			return c;
		}

		if (equals(other)) {
			return 0;
		}

		c = getStart().compareTo(other.getStart());
		if (c != 0) {
			return c;
		}
		return getEnd().compareTo(other.getEnd());
	}

	/**
	 * Implements comparison to other {@link ReactionRelation}. This is needed
	 * to ensure symmetry in the comparison.
	 *
	 * @param other
	 * @return
	 */
	public boolean equals(ReactionRelation other) {
		return (getStart().equals(other.getStart()) && getEnd().equals(other.getEnd()))
				|| (getEnd().equals(other.getStart()) && getStart().equals(other.getEnd()));
	}

	/**
	 * Returns the compounds that are shared by the two reactions related via
	 * this relation. The collection might be empty if no shares have been given
	 * in the constructor.
	 *
	 * @return the shared compounds
	 */
	public Collection<Compound> getShares() {
		if (shares == null) {
			return new ArrayList<Compound>(0);
		}
		return new ArrayList<Compound>(shares);
	}
}
