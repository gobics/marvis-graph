package de.gobics.marvis.graph;

public class Constants {
	public static final String NODEOBJECT = "OBJECT";
	// Type and Names for node types
	public static final String NODEPROPERTY_ID = "ID";
	public static final String NODEPROPERTY_TYPE = "TYPE";
	public static final String NODEPROPERTY_EXPANDED = "EXPANDED";
	
	// Types of nodes
	public static final String NODETYPE_MARKER = "TYPE_MARKER";
	public static final String NODETYPE_CORRECTIONFACTOR = "TYPE_CORRECTIONFACTOR";
	public static final String NODETYPE_COMPOUND = "TYPE_COMPOUND";
	public static final String NODETYPE_REACTION = "TYPE_REACTION";
	public static final String NODETYPE_ENZYME = "TYPE_ENZYME";
	public static final String NODETYPE_GENE = "TYPE_GENE";
	public static final String NODETYPE_TRANSCRIPT = "TYPE_TRANSCRIPT";
	public static final String NODETYPE_PATHWAY = "TYPE_PATHWAY";

	// Properties for Marker nodes
	public static final String NODE_MARKER_PROPERTY_ID = "PROP_MARKER_ID";
	public static final String NODE_MARKER_PROPERTY_MASS = "PROP_MARKER_MASS";
	public static final String NODE_MARKER_PROPERTY_SCORE = "PROP_COMPOUND_SCORE";
	public static final String NODE_MARKER_PROPERTY_CORRECTIONFACTOR = "PROP_COMPOUND_CORRECTIONFACTOR";

	// Properties for Correctionfactor nodes
	public static final String NODE_CORRECTIONFACTOR_PROPERTY_ID = "PROP_CF_ID";

	// Properties for Compound nodes
	public static final String NODE_COMPOUND_PROPERTY_ID = "PROP_COMPOUND_ID";
	public static final String NODE_COMPOUND_PROPERTY_NAME = "PROP_COMPOUND_NAME";
	public static final String NODE_COMPOUND_PROPERTY_MASS = "PROP_COMPOUND_MASS";
	public static final String NODE_COMPOUND_PROPERTY_FORMULA = "PROP_COMPOUND_FORMULA";
	public static final String NODE_COMPOUND_PROPERTY_DESCRIPTION = "PROP_COMPOUND_DESCRIPTION";
	public static final String NODE_COMPOUND_PROPERTY_URL = "PROP_COMPOUND_URL";

	// Properties for reaction nodes
	public static final String NODE_REACTION_PROPERTY_ID = "PROP_REACTION_ID";
	public static final String NODE_REACTION_PROPERTY_NAME = "PROP_REACTION_NAME";
	public static final String NODE_REACTION_PROPERTY_FORMULA = "PROP_REACTION_FORMULA";
	public static final String NODE_REACTION_PROPERTY_DESCRIPTION = "PROP_REACTION_DESCRIPTION";
	
	// Properties for Enzyme nodes
	public static final String NODE_ENZYME_PROPERTY_ID = "PROP_ENZYME_ID";
	public static final String NODE_ENZYME_PROPERTY_NAME = "PROP_ENZYME_NAME";
	public static final String NODE_ENZYME_PROPERTY_FORMULA = "PROP_ENZYME_FORMULA";
	public static final String NODE_ENZYME_PROPERTY_DESCRIPTION = "PROP_ENZYME_DESCRIPTION";
	
	// Properties for gene nodes
	public static final String NODE_GENE_PROPERTY_ID = "PROP_GENE_ID";
	public static final String NODE_GENE_PROPERTY_ORGANISM = "PROP_GENE_ORGANISM";
	public static final String NODE_GENE_PROPERTY_NAME = "PROP_GENE_NAME";
	
	// Properties for transcript nodes
	public static final String NODE_TRANSCRIPT_PROPERTY_ID = "PROP_TRANSCR_ID";
	
	// Properties for Pathways
	public static final String NODE_PATHWAY_PROPERTY_ID = "PROP_PATHWAY_ID";
	public static final String NODE_PATHWAY_PROPERTY_NAME = "PROP_PATHWAY_NAME";
	public static final String NODE_PATHWAY_PROPERTY_DESCRIPTION = "PROP_PATHWAY_DESCRIPTION";
	
	

	}
