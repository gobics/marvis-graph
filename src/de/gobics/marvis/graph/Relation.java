package de.gobics.marvis.graph;
import java.io.*;

public class Relation implements Comparable<Relation>, Externalizable {
	public enum RelationshipType {
		MARKER_ANNOTATION_COMPOUND, 
		REACTION_HAS_SUBSTRATE, 
		REACTION_HAS_PRODUCT, 
		REACTION_NEEDS_ENZYME, 
		GENE_ENCODES_ENZYME, 
		TRANSCRIPT_ISFROM_GENE, 
		REACTION_HAPPENSIN_PATHWAY, 
		ENZYME_USEDIN_PATHWAY
	}

	protected RelationshipType relation_type;
	protected GraphObject start;
	protected GraphObject end;

	public Relation(){
		
	}

	public Relation(RelationshipType type, GraphObject start, GraphObject end) {
		this.relation_type = type;
		this.start = start;
		this.end = end;
	}

	@Override
	public String toString() {
		return Relation.class.getSimpleName() + "[" + this.getStart().getId() +" =" + this.relation_type.toString() +"=> " + this.getEnd().getId() +"]";
	}

	public RelationshipType getType() {
		return this.relation_type;
	}

	public GraphObject getStart() {
		return this.start;
	}

	public GraphObject getEnd() {
		return this.end;
	}

	public boolean contains(GraphObject object){
		return getStart().equals(object) || getEnd().equals(object);
	}

	public GraphObject getOther(GraphObject other) {
		if (this.start.equals(other))
			return this.end;
		
		if (this.end.equals(other))
			return this.start;
		
		throw new IllegalArgumentException("Given node is not part of this relation");
	}

	@Override
	public boolean equals(Object other){
		if( !(other instanceof Relation))
			return false;
		
		if( ! this.getType().equals( ((Relation)other).getType()))
			return false;
		if( ! this.getStart().equals( ((Relation)other).getStart()))
			return false;
		if( ! this.getEnd().equals( ((Relation)other).getEnd()))
			return false;
		
		return true;
		
	}
	public boolean equals(RelationshipType type, GraphObject from, GraphObject to){		
		if( ! this.getType().equals(type))
			return false;
		if( ! this.getStart().equals(from) )
			return false;
		if( ! this.getEnd().equals(to) )
			return false;
		
		return true;
	}

	@Override
	public int compareTo(Relation arg0) {
		int c = getType().compareTo( arg0.getType() );
		if( c != 0 )
			return c;
		
		c = getStart().compareTo(arg0.getStart());
		if( c!=0)
			return c;
		
		return getEnd().compareTo(arg0.getEnd());
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeObject(relation_type);
		oo.writeObject(start);
		oo.writeObject(end);
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
		relation_type = (RelationshipType) oi.readObject();
		start = (GraphObject) oi.readObject();
		end = (GraphObject) oi.readObject();
	}

	
}
