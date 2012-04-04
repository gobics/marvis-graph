package de.gobics.marvis.graph;

/**
 * A relation is the association of one graph object to another. There are are
 * only some relations allowed: {@link RelationshipType}
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class Relation implements Comparable<Relation> {

	protected RelationshipType relation_type;
	protected GraphObject start;
	protected GraphObject end;

	/**
	 * Create a new relation object of the given type to the start and end
	 * points. It is not necessary to construct relations by one self. It is
	 * prefered to call functions in the {@link MetabolicNetwork}, e.g {@link MetabolicNetwork#annotates(de.gobics.marvis.graph.Marker, de.gobics.marvis.graph.Compound)}.
	 *
	 * @see RelationshipType
	 * @see GraphObject
	 * @param type the type of relation
	 * @param start a corresponding start point
	 * @param end a corresponding end point
	 */
	public Relation(RelationshipType type, GraphObject start, GraphObject end) {
		this.relation_type = type;
		this.start = start;
		this.end = end;
	}

	@Override
	public String toString() {
		return Relation.class.getSimpleName() + "[" + this.getStart().getId() + " =" + this.relation_type.
				toString() + "=> " + this.getEnd().getId() + "]";
	}

	/**
	 * Returns the type of the relation.
	 *
	 * @return the type
	 */
	public RelationshipType getType() {
		return this.relation_type;
	}

	/**
	 * Returns the start object of this relation
	 *
	 * @return the graph object from with this relation starts
	 */
	public GraphObject getStart() {
		return this.start;
	}

	/**
	 * Returns the end point of this relation.
	 *
	 * @return the graph object that this relation ends in
	 */
	public GraphObject getEnd() {
		return this.end;
	}

	/**
	 * Returns true if the given object is either the start or the end point of
	 * this relation.
	 *
	 * @param object an object to test
	 * @return true iff object equals the start or end
	 */
	public boolean contains(GraphObject object) {
		return getStart().equals(object) || getEnd().equals(object);
	}

	/**
	 * If the given object is the start of this relation it will return the end
	 * and vice-versa.
	 *
	 * @throws IllegalArgumentException if this relation does not contain the
	 * given object
	 * @param first the object that should not be returned
	 * @return an object in this relation that is not {@code first}
	 */
	public GraphObject getOther(GraphObject first) {
		if (this.start.equals(first)) {
			return this.end;
		}

		if (this.end.equals(first)) {
			return this.start;
		}

		throw new IllegalArgumentException("Given node is not part of this relation");
	}

	/**
	 * Returns true if this relation equals the other. That is if the type, start
	 * and end equal.
	 * @param other the testee
	 * @return true if this and other relation are equal
	 */
	public boolean equals(Relation other) {
		if (!this.getType().equals(other.getType())) {
			return false;
		}
		if (!this.getStart().equals(other.getStart())) {
			return false;
		}
		if (!this.getEnd().equals(other.getEnd())) {
			return false;
		}

		return true;

	}

	/**
	 * Returns true if the given type, start and end equal the internal type, start
	 * end end.
	 */
	public boolean equals(RelationshipType type, GraphObject start, GraphObject end) {
		if (!this.getType().equals(type)) {
			return false;
		}
		if (!this.getStart().equals(start)) {
			return false;
		}
		if (!this.getEnd().equals(end)) {
			return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public int compareTo(Relation arg0) {
		int c = getType().compareTo(arg0.getType());
		if (c != 0) {
			return c;
		}

		c = getStart().compareTo(arg0.getStart());
		if (c != 0) {
			return c;
		}

		return getEnd().compareTo(arg0.getEnd());
	}
}
