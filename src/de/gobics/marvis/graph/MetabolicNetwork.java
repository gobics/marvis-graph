package de.gobics.marvis.graph;

import java.util.*;
import java.util.logging.Logger;

public class MetabolicNetwork {

	protected static final Logger logger = Logger.getLogger(MetabolicNetwork.class.
			getName());
	private static int COUNTER = 0;
	/**
	 * Contains the threshold for compounds to be a "cofactor". A compound
	 * occur in at least {@code cofactor_threshold} reactions will be treated
	 * as a cofactor.
	 */
	private MetabolicNetwork parent = null;
	private String name = null;
	private final int graph_id;
	private ExplainablePredicate explainer = new SimpleExplainer();
	protected HashMap<Class<? extends GraphObject>, TreeMap<String, GraphObject>> vertices = new HashMap<Class<? extends GraphObject>, TreeMap<String, GraphObject>>();
	protected EnumMap<RelationshipType, TreeSet<Relation>> relations = new EnumMap<RelationshipType, TreeSet<Relation>>(RelationshipType.class);
	protected HashMap<Class<? extends GraphObject>, TreeMap<GraphObject, LinkedList<Relation>>> object_to_relation_mapping = new HashMap<Class<? extends GraphObject>, TreeMap<GraphObject, LinkedList<Relation>>>();

	public MetabolicNetwork() {
		this(null);
	}

	public MetabolicNetwork(MetabolicNetwork parent) {
		this.parent = parent;
		graph_id = COUNTER++;
		for (RelationshipType t : RelationshipType.values()) {
			relations.put(t, new TreeSet());
		}
	}

	public boolean isSubnetwork() {
		return parent != null;
	}

	public MetabolicNetwork getParent() {
		return parent;
	}

	public void detachFromParent() {
		parent = null;
	}

	@Override
	public int hashCode() {
		return graph_id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MetabolicNetwork other = (MetabolicNetwork) obj;
		if (this.graph_id != other.graph_id) {
			return false;
		}
		return true;
	}

	public void printDump() {
		for (TreeSet bts : relations.values()) {
			for (Object r : bts) {
				System.out.println(r);
			}
		}
	}

	synchronized public boolean addRelation(RelationshipType type, GraphObject from, GraphObject to) {
		return addRelation(new Relation(type, from, to));
	}

	synchronized public boolean addRelation(Relation relation) {
		if (!relations.containsKey(relation.getType())) {
			relations.put(relation.getType(), new TreeSet());
		}

		GraphObject v1 = addObject(relation.getStart());
		GraphObject v2 = addObject(relation.getEnd());
		relations.get(relation.getType()).add(relation);

		try {
			if (object_to_relation_mapping.get(v1.getClass()) == null) {
				throw new Exception("object_to_relation_mapping does not known class: " + v1.
						getClass().getSimpleName());
			}
			if (object_to_relation_mapping.get(v1.getClass()).get(v1) == null) {
				throw new Exception("object_to_relation_mapping.get(" + v1.
						getClass().getSimpleName() + " does not known object: " + v1);
			}
			if (object_to_relation_mapping.get(v2.getClass()) == null) {
				throw new Exception("object_to_relation_mapping does not known class: " + v2.
						getClass().getSimpleName());
			}
			if (object_to_relation_mapping.get(v2.getClass()).get(v2) == null) {
				throw new Exception("object_to_relation_mapping.get(" + v2.
						getClass().getSimpleName() + " does not known object: " + v2);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (!object_to_relation_mapping.get(v1.getClass()).get(v1).contains(relation)) {
			object_to_relation_mapping.get(v1.getClass()).get(v1).push(relation);
		}
		if (!object_to_relation_mapping.get(v2.getClass()).get(v2).contains(relation)) {
			object_to_relation_mapping.get(v2.getClass()).get(v2).push(relation);
		}
		return true;
	}

	synchronized public GraphObject addObject(GraphObject o) {

		if (!object_to_relation_mapping.containsKey(o.getClass())) {
			object_to_relation_mapping.put(o.getClass(), new TreeMap<GraphObject, LinkedList<Relation>>());
		}
		if (!object_to_relation_mapping.get(o.getClass()).containsKey(o)) {
			object_to_relation_mapping.get(o.getClass()).put(o, new LinkedList<Relation>());
		}

		Class c = o.getClass();
		if (!vertices.containsKey(c)) {
			vertices.put(c, new TreeMap<String, GraphObject>());
		}
		TreeMap<String, GraphObject> class_objects = vertices.get(c);

		GraphObject o2 = (GraphObject) class_objects.get(o.getId());
		if (o2 == null) {
			class_objects.put(o.getId(), o);
			o2 = o;
		}
		return o2;
	}

	synchronized public void annotates(Marker marker, Compound compound) {
		addRelation(RelationshipType.MARKER_ANNOTATION_COMPOUND, marker, compound);
	}

	synchronized public LinkedList<Reaction> catalysesReactions(Enzyme enzyme) {
		LinkedList<Reaction> ret = new LinkedList<Reaction>();
		for (Object o : object_to_relation_mapping.get(enzyme.getClass()).get(enzyme)) {
			Relation r = (Relation) o;
			if (r.getEnd().equals(enzyme)) {
				ret.add((Reaction) r.getStart());
			}
		}
		return ret;
	}

	@Override
	synchronized public MetabolicNetwork clone() {
		MetabolicNetwork clone = new MetabolicNetwork(getParent());
		clone.setName(getName());
		for (Relation r : getRelations()) {
			clone.addRelation(r.getType(), r.getStart(), r.getEnd());
		}
		for (GraphObject o : getAllObjects()) {
			clone.addObject(o);
		}
		if (hasMarkers()) {
			clone.setHasMarkers();
		}
		if (hasTranscripts()) {
			clone.setHasTranscripts();
		}
		return clone;
	}

	synchronized public boolean containsRelation(RelationshipType type, GraphObject from, GraphObject to) {
		return containsRelation(new Relation(type, from, to));
	}

	synchronized public boolean containsRelation(Relation arg0) {
		if (!relations.containsKey(arg0.getType())) {
			return false;
		}
		return relations.get(arg0.getType()).contains(arg0);
	}

	synchronized public boolean containsObject(GraphObject o) {
		if (!vertices.containsKey(o.getClass())) {
			return false;
		}
		TreeMap<String, GraphObject> class_objects = vertices.get(o.getClass());
		return class_objects.containsKey(o.getId());
	}

	synchronized public int countExplainable() {
		int counter = 0;
		for (GraphObject o : getAllObjects()) {
			if (explainer.evaluate(this, o)) {
				counter++;
			}
		}
		return counter;
	}

	synchronized public Compound createCompound(String compound_id) {
		return (Compound) addObject(new Compound(compound_id));
	}

	synchronized public Enzyme createEnzyme(String enzyme_id) {
		return (Enzyme) addObject(new Enzyme(enzyme_id));
	}

	synchronized public Gene createGene(String id) {
		return (Gene) addObject(new Gene(id));
	}

	synchronized public Marker createMarker(String marker_id) {
		return (Marker) addObject(new Marker(marker_id));
	}

	synchronized public Pathway createPathway(String id) {
		return (Pathway) addObject(new Pathway(id));
	}

	synchronized public Reaction createReaction(String reaction_id) {
		return (Reaction) addObject(new Reaction(reaction_id));
	}

	synchronized public Transcript createTranscript(String transcript_id) {
		return (Transcript) addObject(new Transcript(transcript_id));
	}

	synchronized public LinkedList<Gene> encodedByGenes(Enzyme enzyme) {
		LinkedList<Gene> ret = new LinkedList<Gene>();
		for (Object o : object_to_relation_mapping.get(enzyme.getClass()).get(enzyme)) {
			Relation r = (Relation) o;
			if (r.getType().equals(RelationshipType.GENE_ENCODES_ENZYME)) {
				ret.add((Gene) r.getStart());
			}
		}
		return ret;
	}

	synchronized public void encodesFor(Gene gene, Enzyme enzyme) {
		addRelation(RelationshipType.GENE_ENCODES_ENZYME, gene, enzyme);
	}

	/*	public boolean expand(GraphObject graphobject) throws Exception {
	logger.finer("Try to expand object: " + graphobject);
	if (graphobject.isExpanded()) {
	return false;
	}
	
	int old_size = size();
	for (Expander e : expander) {
	e.expand(graphobject);
	}
	graphobject.setExpanded(true);
	return old_size != size();
	}
	 */
	synchronized public LinkedList<GraphObject> getAllObjects() {
		LinkedList<GraphObject> ret = new LinkedList<GraphObject>();
		for (TreeMap<String, GraphObject> tset : vertices.values()) {
			ret.addAll(tset.values());
		}
		return ret;
	}

	synchronized public LinkedList<GraphObject> getAllObjects(Class<? extends GraphObject> classtype) {
		if (vertices.containsKey(classtype)) {
			return new LinkedList<GraphObject>(vertices.get(classtype).values());
		}
		return new LinkedList<GraphObject>();
	}

	synchronized public LinkedList<Marker> getAnnotatingMarker(Compound compound) {
		LinkedList<Marker> ret = new LinkedList<Marker>();
		for (Object o : object_to_relation_mapping.get(compound.getClass()).get(compound)) {
			Relation r = (Relation) o;
			if (r.getType().equals(RelationshipType.MARKER_ANNOTATION_COMPOUND)) {
				ret.add((Marker) r.getStart());
			}
		}
		return ret;
	}

	synchronized public LinkedList<Compound> getAnnotations(Marker marker) {
		LinkedList<Compound> ret = new LinkedList<Compound>();
		for (Object o : relations.get(RelationshipType.MARKER_ANNOTATION_COMPOUND)) {
			Relation r = (Relation) o;
			if (r.getStart().equals(marker)) {
				ret.add((Compound) r.getEnd());
			}
		}
		return ret;
	}

	synchronized public Compound getCompound(String id) {
		return (Compound) getObject(Compound.class, id);
	}

	synchronized public Collection<Compound> getMolecules() {
		ArrayList<Compound> list = new ArrayList<Compound>();
		if (!vertices.containsKey(Compound.class)) {
			return list;
		}
		for (GraphObject o : vertices.get(Compound.class).values()) {
			list.add((Compound) o);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	synchronized public Collection<Relation> getRelations(RelationshipType type) {
		if (!relations.containsKey(type)) {
			return new ArrayList<Relation>();
		}
		return (Collection<Relation>) relations.get(type).clone();
	}

	synchronized public int countRelations() {
		return getRelations().size();
	}

	synchronized public Collection<Relation> getRelations() {
		LinkedList<Relation> rels = new LinkedList<Relation>();
		for (TreeSet grs : relations.values()) {
			rels.addAll(grs);
		}
		return rels;
	}

	synchronized public Collection<Relation> getRelationsBetween(GraphObject v1, GraphObject v2) {
		TreeSet<Relation> rels = new TreeSet<Relation>();
		for (Relation r : getRelations(v1)) {
			if (r.getOther(v1).equals(v2)) {
				rels.add(r);
			}
		}
		return rels;
	}

	synchronized public LinkedList<Enzyme> getEncodedEnzymes(Gene gene) {
		LinkedList<Enzyme> ret = new LinkedList<Enzyme>();
		for (Object o : object_to_relation_mapping.get(gene.getClass()).get(gene)) {
			Relation r = (Relation) o;
			if (r.getType().equals(RelationshipType.GENE_ENCODES_ENZYME)) {
				ret.add((Enzyme) r.getEnd());
			}
		}
		return ret;
	}

	synchronized public Enzyme getEnzyme(String id) {
		return (Enzyme) getObject(Enzyme.class, id);
	}

	synchronized public Collection<Enzyme> getEnzymes() {
		ArrayList<Enzyme> list = new ArrayList<Enzyme>();
		if (!vertices.containsKey(Enzyme.class)) {
			return list;
		}
		for (GraphObject o : vertices.get(Enzyme.class).values()) {
			list.add((Enzyme) o);
		}
		return list;
	}

	synchronized public ExplainablePredicate getExplainablePredicate() {
		return explainer;
	}

	synchronized public Gene getGene(String id) {
		return (Gene) getObject(Gene.class, id);
	}

	synchronized public Collection<Gene> getGenes() {
		ArrayList<Gene> list = new ArrayList<Gene>();
		if (!vertices.containsKey(Gene.class)) {
			return list;
		}
		for (GraphObject o : vertices.get(Gene.class).values()) {
			list.add((Gene) o);
		}
		return list;
	}

	synchronized public LinkedList<Gene> getGenes(Transcript transcript) {
		LinkedList<Gene> ret = new LinkedList<Gene>();
		for (Object o : object_to_relation_mapping.get(transcript.getClass()).
				get(transcript)) {
			Relation r = (Relation) o;
			if (r.getType().equals(RelationshipType.TRANSCRIPT_ISFROM_GENE)) {
				ret.add((Gene) r.getEnd());
			}
		}
		return ret;
	}

	synchronized public LinkedList<Gene> getEncodingGenes(Enzyme enzyme) {
		LinkedList<Gene> ret = new LinkedList<Gene>();
		for (Object o : object_to_relation_mapping.get(enzyme.getClass()).get(enzyme)) {
			Relation r = (Relation) o;
			if (r.getType().equals(RelationshipType.GENE_ENCODES_ENZYME)) {
				ret.add((Gene) r.getStart());
			}
		}
		return ret;
	}

	synchronized private double getExplainableReactionCount() {
		int counter = 0;
		for (Reaction r : getReactions()) {
			if (isExplainable(r)) {
				counter++;
			}
		}

		return (double) counter;
	}

	synchronized public Marker getMarker(String id) {
		return (Marker) getObject(Marker.class, id);
	}

	synchronized public Collection<Marker> getMarkers() {
		ArrayList<Marker> list = new ArrayList<Marker>();
		if (!vertices.containsKey(Marker.class)) {
			return list;
		}
		for (GraphObject o : vertices.get(Marker.class).values()) {
			list.add((Marker) o);
		}
		return list;
	}

	synchronized public String getName() {
		return this.name;
	}

	synchronized private GraphObject getObject(Class<? extends GraphObject> c, String id) {
		return (GraphObject) getStore(c).get(id);
	}

	synchronized public Collection<GraphObject> getObjects(Class<? extends GraphObject> c) {
		return getStore(c).values();
	}

	synchronized public Pathway getPathway(String id) {
		return (Pathway) getObject(Pathway.class, id);
	}

	synchronized public Collection<Pathway> getPathways() {
		ArrayList<Pathway> list = new ArrayList<Pathway>();
		if (!vertices.containsKey(Pathway.class)) {
			return list;
		}
		for (GraphObject o : vertices.get(Pathway.class).values()) {
			list.add((Pathway) o);
		}
		return list;
	}

	synchronized public LinkedList<Compound> getProducts(Reaction reaction) {
		LinkedList<Compound> ret = new LinkedList<Compound>();
		for (Object o : object_to_relation_mapping.get(reaction.getClass()).get(reaction)) {
			Relation r = (Relation) o;
			if (r.getType().equals(RelationshipType.REACTION_HAS_PRODUCT)) {
				ret.add((Compound) r.getEnd());
			}
		}
		return ret;
	}

	synchronized public Set<Compound> getCompounds() {
		TreeSet<Compound> list = new TreeSet<Compound>();
		if (!vertices.containsKey(Compound.class)) {
			return list;
		}
		for (GraphObject o : vertices.get(Compound.class).values()) {
			list.add((Compound) o);
		}
		return list;
	}

	synchronized public Set<Compound> getCompounds(Reaction reaction) {
		TreeSet<Compound> compounds = new TreeSet<Compound>(getSubstrates(reaction));
		compounds.addAll(getProducts(reaction));
		return compounds;
	}

	synchronized public LinkedList<Reaction> getProductToReaction(Compound compound) {
		LinkedList<Reaction> ret = new LinkedList<Reaction>();
		for (Object o : object_to_relation_mapping.get(compound.getClass()).get(compound)) {
			Relation r = (Relation) o;
			if (r.getType().equals(RelationshipType.REACTION_HAS_PRODUCT)) {
				ret.add((Reaction) r.getStart());
			}
		}
		return ret;
	}

	synchronized public Reaction getReaction(String id) {
		return (Reaction) getObject(Reaction.class, id);
	}

	synchronized public Collection<Reaction> getReactions() {
		ArrayList<Reaction> list = new ArrayList<Reaction>();
		if (!vertices.containsKey(Reaction.class)) {
			return list;
		}
		for (GraphObject o : vertices.get(Reaction.class).values()) {
			list.add((Reaction) o);
		}
		return list;
	}

	synchronized public LinkedList<Reaction> getReactions(Pathway pathway) {
		LinkedList<Reaction> ret = new LinkedList<Reaction>();
		for (Object o : relations.get(RelationshipType.REACTION_HAPPENSIN_PATHWAY)) {
			Relation r = (Relation) o;
			if (r.getEnd().equals(pathway)) {
				ret.add((Reaction) r.getStart());
			}
		}
		return ret;
	}

	synchronized public Set<Reaction> getReactions(Compound compound) {
		TreeSet<Reaction> reactions = new TreeSet<Reaction>(getSubstrateToReaction(compound));
		reactions.addAll(getProductToReaction(compound));
		return reactions;
	}

	synchronized private TreeMap<String, GraphObject> getStore(Class c) {
		if (!vertices.containsKey(c)) {
			return new TreeMap<String, GraphObject>();
		}
		return vertices.get(c);
	}

	synchronized public LinkedList<Compound> getSubstrates(Reaction reaction) {
		LinkedList<Compound> ret = new LinkedList<Compound>();
		for (Relation r : getRelations(reaction)) {
			if (r.getType().equals(RelationshipType.REACTION_HAS_SUBSTRATE)) {
				ret.add((Compound) r.getEnd());
			}
		}
		return ret;
	}

	synchronized public LinkedList<Reaction> getSubstrateToReaction(Compound compound) {
		LinkedList<Reaction> ret = new LinkedList<Reaction>();
		for (Relation r : getRelations(compound)) {
			if (r.getType().equals(RelationshipType.REACTION_HAS_SUBSTRATE)) {
				ret.add((Reaction) r.getStart());
			}
		}
		return ret;
	}

	synchronized public Transcript getTranscript(String id) {
		return (Transcript) getObject(Transcript.class, id);
	}

	synchronized public Collection<Transcript> getTranscripts() {
		ArrayList<Transcript> list = new ArrayList<Transcript>();
		if (!vertices.containsKey(Transcript.class)) {
			return list;
		}
		for (GraphObject o : vertices.get(Transcript.class).values()) {
			list.add((Transcript) o);
		}
		return list;
	}

	synchronized public LinkedList<Transcript> getTranscripts(Gene gene) {
		LinkedList<Transcript> ret = new LinkedList<Transcript>();
		for (Object o : object_to_relation_mapping.get(gene.getClass()).get(gene)) {
			Relation r = (Relation) o;
			if (r.getType().equals(RelationshipType.TRANSCRIPT_ISFROM_GENE)) {
				ret.add((Transcript) r.getStart());
			}
		}
		return ret;
	}

	synchronized public void happensIn(Reaction reaction, Pathway pathway) {
		addRelation(new Relation(RelationshipType.REACTION_HAPPENSIN_PATHWAY,
				reaction, pathway));
	}

	synchronized public void hasProduct(Reaction reaction, Compound compound) {
		addRelation(new Relation(RelationshipType.REACTION_HAS_PRODUCT, reaction,
				compound));
	}

	synchronized public void hasSubstrate(Reaction reaction, Compound compound) {
		addRelation(new Relation(RelationshipType.REACTION_HAS_SUBSTRATE, reaction,
				compound));
	}

	synchronized public boolean isExplainable(GraphObject object) {
		if (object == null) {
			throw new IllegalArgumentException("Given object is null");
		}
		if (getObject(object.getClass(), object.getId()) == null) {
			throw new IllegalArgumentException("Given object " + object + "is not part of: " + toString());
		}
		return explainer.evaluate(this, object);
	}

	synchronized public boolean isExplainableWithGap(GraphObject graphobject, int allowed_gaps) {
		return this.isExplainableWithGap(graphobject, allowed_gaps, new LinkedList<GraphObject>());
	}

	synchronized private boolean isExplainableWithGap(GraphObject graphobject,
			int allowed_gaps, LinkedList<GraphObject> former_path) {
		//logger.finer("Try to explain " + graphobject + " with max " + allowed_gaps + ". Path is: " + former_path);
		if (allowed_gaps <= 0) {
			return explainer.evaluate(this, graphobject);
		}

		if (explainer.evaluate(this, graphobject)) {
			return true;
		}

		if (former_path.contains(graphobject)) {
			return false;
		}

		former_path.add(graphobject);

		for (Relation r : getRelations(graphobject)) {
			GraphObject other = r.getOther(graphobject);
			if (isExplainableWithGap(other, allowed_gaps - 1, former_path)) {
				return true;
			}
		}
		return false;
	}

	synchronized public void isFrom(Transcript transcript, Gene gene) {
		addRelation(new Relation(RelationshipType.TRANSCRIPT_ISFROM_GENE, transcript, gene));
	}

	synchronized public void needsEnzyme(Reaction reaction, Enzyme enzyme) {
		addRelation(new Relation(RelationshipType.REACTION_NEEDS_ENZYME, reaction,
				enzyme));
	}

	synchronized public void remove(GraphObject o) {
		if (!vertices.containsKey(o.getClass())) {
			return;
		}
		// Remove all relations concerning this Vertex
		for (Relation r : getRelations(o)) {
			removeRelation(r);
		}
		logger.finer("Removing vertex " + o);
		vertices.get(o.getClass()).remove(o.getId());
	}

	synchronized public boolean removeRelation(Relation arg0) {
		if (!relations.containsKey(arg0.getType())) {
			logger.finer("Graph does not contain edges of type: " + arg0.getType());
			return true;
		}
		logger.finer("Removing edge: " + arg0);
		relations.get(arg0.getType()).remove(arg0);
		object_to_relation_mapping.get(arg0.getStart().getClass()).get(arg0.
				getStart()).remove(arg0);
		object_to_relation_mapping.get(arg0.getEnd().getClass()).get(arg0.getEnd()).
				remove(arg0);
		return true;
	}

	synchronized public void setExplainablePredicate(ExplainablePredicate predicate) {
		explainer = predicate;
	}

	synchronized public void setName(String new_name) {
		if (new_name != null) {
			this.name = new_name;
		}
	}

	synchronized public int size() {
		int count = 0;
		for (TreeMap<String, GraphObject> tmap : vertices.values()) {
			count += tmap.size();
		}
		return count;
	}

	@Override
	synchronized public String toString() {
		return getClass().getSimpleName() + "{name=" + getName() + "}";
	}

	synchronized public LinkedList<Relation> getRelations(GraphObject graphobject) {
		//try {throw new Exception("CROAK");} catch (Exception e) {e.printStackTrace();}
		if (graphobject == null) {
			throw new IllegalArgumentException("Can not fetch relations of: " + graphobject);
		}
		//logger.finer("Fetching relations of " + graphobject);
		LinkedList<Relation> rels = new LinkedList<Relation>();
		if (!object_to_relation_mapping.containsKey(graphobject.getClass())) {
			return rels;
		}
		if (!object_to_relation_mapping.get(graphobject.getClass()).containsKey(graphobject)) {
			return rels;
		}
		for (Object o : object_to_relation_mapping.get(graphobject.getClass()).
				get(graphobject)) {
			rels.add((Relation) o);
		}
		//logger.finer("Fetched " + rels.size() + " relations of " + graphobject + ": " + rels);
		return rels;
	}

	synchronized public LinkedList<Relation> getRelations(GraphObject object, RelationshipType type) {
		LinkedList<Relation> rels = new LinkedList<Relation>();
		for (Relation r : getRelations(object)) {
			if (r.getType().equals(type)) {
				rels.add(r);
			}
		}
		return rels;
	}

	synchronized public LinkedList<Enzyme> getEnzymes(Reaction reaction) {
		LinkedList<Enzyme> ret = new LinkedList<Enzyme>();
		for (Object o : object_to_relation_mapping.get(reaction.getClass()).get(reaction)) {
			Relation r = (Relation) o;
			if (r.getType().equals(RelationshipType.REACTION_NEEDS_ENZYME)) {
				ret.add((Enzyme) r.getEnd());
			}
		}
		return ret;
	}
	
	synchronized public LinkedList<Reaction> getReactions(Enzyme enzyme) {
		LinkedList<Reaction> ret = new LinkedList<Reaction>();
		for (Object o : object_to_relation_mapping.get(enzyme.getClass()).get(enzyme)) {
			Relation r = (Relation) o;
			if (r.getType().equals(RelationshipType.REACTION_NEEDS_ENZYME)) {
				ret.add((Reaction) r.getStart());
			}
		}
		return ret;
	}

	synchronized public LinkedList<Pathway> getPathways(Reaction reaction) {
		//try { throw new Exception("This is how we get here"); } catch(Exception e){ e.printStackTrace(); }
		Collection<Relation> rels = getRelations(reaction);
		LinkedList<Pathway> ret = new LinkedList<Pathway>();
		for (Object o : object_to_relation_mapping.get(reaction.getClass()).get(reaction)) {
			Relation r = (Relation) o;
			if (r.getType().equals(RelationshipType.REACTION_HAPPENSIN_PATHWAY)) {
				ret.add((Pathway) r.getEnd());
			}
		}
		return ret;
	}

	synchronized public String[] getConditionNames() {
		TreeSet<String> names = new TreeSet<String>();

		for (Marker m : getMarkers()) {
			names.addAll(Arrays.asList(m.getRawIntensityNames()));
		}
		for (Transcript m : getTranscripts()) {
			names.addAll(Arrays.asList(m.getRawIntensityNames()));
		}

		String[] names_array = new String[names.size()];
		int idx = 0;
		for (String s : names) {
			names_array[idx++] = s;
		}
		return names_array;
	}

	synchronized public void importData(MetabolicNetwork other) {
		for (Relation r : other.getRelations()) {
			addRelation(r);
		}
		for (GraphObject o : other.getAllObjects()) {
			addObject(o);
		}
	}

	synchronized public MetabolicNetwork merge(MetabolicNetwork other) {
		MetabolicNetwork merged = clone();
		merged.importData(other);
		return merged;
	}

	synchronized public void dump() {
		System.out.println("Graph '" + this.getName() + "' contains:");
		for (Class c : vertices.keySet()) {
			System.out.println("  " + c.getSimpleName() + ": " + vertices.get(c).
					size());
		}
	}

	synchronized public boolean hasTranscripts() {
		if( parent != null)
			return parent.hasTranscripts();
		return vertices.containsKey(Transcript.class);
	}

	synchronized public void setHasTranscripts() {
		if (!vertices.containsKey(Transcript.class)) {
			vertices.put(Transcript.class, new TreeMap<String, GraphObject>());
		}
	}

	synchronized public boolean hasMarkers() {
		if( parent != null)
			return parent.hasMarkers();
		return vertices.containsKey(Marker.class);
	}

	synchronized public void setHasMarkers() {
		if (!vertices.containsKey(Marker.class)) {
			vertices.put(Marker.class, new TreeMap<String, GraphObject>());
		}
	}

	synchronized public Collection<Relation> getAllRelations() {
		LinkedList<Relation> rels = new LinkedList<Relation>();
		for (RelationshipType rt : relations.keySet()) {
			rels.addAll(relations.get(rt));
		}
		return rels;
	}

	synchronized public Collection<Relation> getAllRelations(RelationshipType of_type) {
		return new LinkedList<Relation>(relations.get(of_type));
	}

	synchronized public MetabolicNetwork getSubgraph(Collection<Relation> relations) {
		return getSubgraph(relations, true);
	}

	synchronized public MetabolicNetwork getSubgraph(Collection<Relation> relations, boolean extract_environment) {
		MetabolicNetwork g = new MetabolicNetwork();
		for (Relation r : relations) {
			g.addRelation(r);
		}
		if (extract_environment) {
			for (Reaction r : g.getReactions()) {
				for (Relation rel : getEnvironment(r)) {
					g.addRelation(rel);
				}
			}
		}
		return g;
	}

	synchronized public boolean hasInputData() {
		if (hasMarkers() && getMarkers().size() > 0) {
			return true;
		}
		if (hasTranscripts() && getTranscripts().size() > 0) {
			return true;
		}
		return false;
	}

	synchronized public int countRelations(GraphObject obj, RelationshipType... types) {
		int c = 0;
		for (RelationshipType rt : types) {
			c += getRelations(obj, rt).size();
		}
		return c;
	}

	synchronized public void removeNodesWhichAreNotExplainable() {
		for (GraphObject o : getAllObjects()) {
			if (!isExplainable(o)) {
				remove(o);
			}
		}
	}

	synchronized public Set<Relation> getEnvironment(Reaction reaction) {
		if (!containsObject(reaction)) {
			throw new RuntimeException(this + " does not contain " + reaction);
		}
		Set<Relation> rels = new TreeSet<Relation>();

		// Extract compounds and corresponding marker
		for (Compound s : getSubstrates(reaction)) {
			rels.add(new Relation(RelationshipType.REACTION_HAS_SUBSTRATE, reaction, s));
			for (Marker m : getAnnotatingMarker(s)) {
				rels.add(new Relation(RelationshipType.MARKER_ANNOTATION_COMPOUND, m, s));
			}
		}

		for (Compound p : getProducts(reaction)) {
			rels.add(new Relation(RelationshipType.REACTION_HAS_PRODUCT, reaction, p));
			for (Marker m : getAnnotatingMarker(p)) {
				rels.add(new Relation(RelationshipType.MARKER_ANNOTATION_COMPOUND, m, p));
			}
		}

		// Extract pathways
		for (Pathway p : getPathways(reaction)) {
			rels.add(new Relation(RelationshipType.REACTION_HAPPENSIN_PATHWAY, reaction, p));
		}

		// Extract enzymes, ...
		for (Enzyme e : getEnzymes(reaction)) {
			rels.add(new Relation(RelationshipType.REACTION_NEEDS_ENZYME, reaction, e));

			// ... genes,
			for (Gene g : encodedByGenes(e)) {
				rels.add(new Relation(RelationshipType.GENE_ENCODES_ENZYME, g, e));

				// ... and transcripts
				for (Transcript t : getTranscripts(g)) {
					rels.add(new Relation(RelationshipType.TRANSCRIPT_ISFROM_GENE, t, g));
				}
			}

		}

		return rels;
	}

	synchronized public Set<Relation> getAllPathwayComponents(Pathway pathway) {
		if (!containsObject(pathway)) {
			throw new RuntimeException(this + " does not contain " + pathway);
		}
		Set<Relation> rels = new TreeSet<Relation>();


		for (Reaction reaction : getReactions(pathway)) {
			rels.add(new Relation(RelationshipType.REACTION_HAPPENSIN_PATHWAY, reaction, pathway));

			// Get the complete reaction environment ...
			for (Relation r : getEnvironment(reaction)) {
				// ... but exclude other pathways
				if (!r.getType().equals(RelationshipType.REACTION_HAPPENSIN_PATHWAY)) {
					rels.add(r);
				}
			}
		}


		return rels;
	}

	@SuppressWarnings("unchecked")
	synchronized public Class<? extends GraphObject>[] getAllObjectClasses() {
		return vertices.keySet().toArray(new Class[vertices.size()]);
	}
}
