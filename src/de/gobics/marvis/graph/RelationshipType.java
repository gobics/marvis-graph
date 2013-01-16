package de.gobics.marvis.graph;

/**
 * This enumeration describes the type of relations that are allowed in the 
 * metabolic network. The possible relations are:
 * <ul>
 * <li> MARKER_ANNOTATION_COMPOUND
 * <li> REACTION_HAS_SUBSTRATE
 * <li> REACTION_HAS_PRODUCT
 * <li> REACTION_NEEDS_ENZYME
 * <li> GENE_ENCODES_ENZYME
 * <li> TRANSCRIPT_ISFROM_GENE
 * <li> REACTION_HAPPENSIN_PATHWAY
 * <li> ENZYME_USEDIN_PATHWAY
 * </ul>
 * @author Manuel Landesfeind {@link mailto:manuel@gobics.de}
 */
public enum RelationshipType {
		MARKER_ANNOTATION_COMPOUND, 
		REACTION_HAS_SUBSTRATE, 
		REACTION_HAS_PRODUCT, 
		REACTION_NEEDS_ENZYME, 
		GENE_ENCODES_ENZYME, 
		TRANSCRIPT_ISFROM_GENE, 
		REACTION_HAPPENSIN_PATHWAY, 
		ENZYME_USEDIN_PATHWAY,
		REACTION_SHARECOMPOUND_REACTION
	}

