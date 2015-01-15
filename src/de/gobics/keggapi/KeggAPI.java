/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.keggapi;

import de.gobics.marvis.utils.reader.KeggEntry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class KeggAPI {

	protected static final Logger logger = Logger.getLogger(KeggAPI.class.
			getName());
	private final String url;

	public KeggAPI() {
		this("http://rest.kegg.jp");
	}

	public KeggAPI(String base_url) {
		this.url = base_url;
	}

	public String[] list_databases() throws MalformedURLException, IOException {
		return fetch_list(0, "list", "database");
	}

	public String[] list_organisms() throws MalformedURLException, IOException {
		return fetch_list(1, "list", "organism");
	}

	/**
	 * Returns a list of pathways for the given organism. The organism IDs
	 * correspond to the IDs returned by {@code list_organisms()}. Additionally
	 * the organism IDs "map", "ko", and "ec" are supported.
	 *
	 * @param org_id
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String[] list_pathways_of_organism(String org_id) throws MalformedURLException, IOException {
		return fetch_list(0, "list", "pathway", org_id);
	}

	public String[] list_reactions_of_pathway(String pathway_id) throws MalformedURLException, IOException {
		return link("reaction", pathway_id);
	}

	public String[] list_reactions_of_enzyme(String enzyme_id) throws MalformedURLException, IOException {
		return link("reaction", enzyme_id);
	}

	public String[] list_pathways_of_reaction(String pathway_id) throws MalformedURLException, IOException {
		return link("pathway", pathway_id);
	}

	public String[] list_enzymes_of_pathway(String pathway_id) throws MalformedURLException, IOException {
		return link("enzyme", pathway_id);
	}

	public String[] list_compounds_of_pathway(String pathway_id) throws MalformedURLException, IOException {
		return link("compound", pathway_id);
	}

	public String[] list_compounds_of_reaction(String reaction_id) throws MalformedURLException, IOException {
		return link("compound", reaction_id);
	}

	public String[] list_glycans_of_reaction(String reaction_id) throws MalformedURLException, IOException {
		return link("glycan", reaction_id);
	}

	public String[] list_reactions_of_compound(String compound_id) throws MalformedURLException, IOException {
		return link("reaction", compound_id);
	}

	public String[] list_genes_of_enzyme(String organism_id, String enzyme_id) throws MalformedURLException, IOException {
		return link(organism_id, enzyme_id);
	}

	public String[] list_genes_of_organism(String organism_id) throws MalformedURLException, IOException {
		return fetch_list(0, "list", organism_id);
	}

	public String[] list_enzymes_of_gene(String gene_id) throws MalformedURLException, IOException {
		return link("enzyme", gene_id);
	}

	public String[] list_enzymes_of_reaction(String reaction_id) throws MalformedURLException, IOException {
		return link("enzyme", reaction_id);
	}

	public String[] link(String dest_db, String source) throws MalformedURLException, IOException {
		return fetch_list(1, "link", dest_db, source);
	}

	public Map<String, TreeSet<String>> link_as_map(String dest_db, String source) throws MalformedURLException, IOException {
		return link_as_map(dest_db, source, 0, 1);
	}

	public Map<String, TreeSet<String>> link_as_map(String dest_db, String source, int id_column, int target_column) throws MalformedURLException, IOException {
		Map<String, TreeSet<String>> result = new TreeMap<String, TreeSet<String>>();
		for (String line : fetch("link", dest_db, source).split("\n")) {
			String[] token = line.split("\t");
			String key = token[id_column];
			if (!result.containsKey(key)) {
				result.put(key, new TreeSet<String>());
			}
			result.get(key).add(token[target_column]);
		}
		return result;
	}

	/**
	 * Returns all information to the given database.
	 *
	 * @param database
	 * @return
	 */
	public InfoResult info(String database_id) throws MalformedURLException, IOException {
		return InfoResult.parse(fetch("info", database_id));
	}

	public KeggEntry get(String id) throws MalformedURLException, IOException {
		return new KeggEntry(get_as_string(id));
	}

	public String get_as_string(String id) throws MalformedURLException, IOException {
		return fetch("get", id);
	}

	/**
	 * This method performs the HTTP request and returns all content as single
	 * string. The arguments are concatenated (separated by "/") to the base
	 * URL.
	 *
	 * @param arguments
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String fetch(String... arguments) throws MalformedURLException, IOException {
		BufferedReader in = getReader(arguments);
		StringBuilder result = new StringBuilder(100);
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			result.append(inputLine).append("\n");
		}
		in.close();
		return result.toString();
	}

	/**
	 * Return all contents in the specified column as string array. E.g. to list
	 * all available KEGG Orthologs, use:
	 * <code>new KeggAPI().fetch_list(0, "list", "ko")</code>
	 *
	 * @param column
	 * @param arguments
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String[] fetch_list(int column, String... arguments) throws MalformedURLException, IOException {
		BufferedReader reader = getReader(arguments);
		LinkedList<String> list = new LinkedList<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.isEmpty()) {
				String[] token = line.split("\t");
				list.add(token[column]);
			}
		}
		reader.close();
		return list.toArray(new String[list.size()]);
	}

	protected BufferedReader getReader(String[] arguments) throws MalformedURLException, IOException {
		StringBuilder sb = new StringBuilder(url);
		for (String s : arguments) {
			sb.append("/").append(s);
		}
		URL request_url = new URL(sb.toString());
		logger.finest("Requesting REST Url: " + request_url);
		URLConnection connection = request_url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.
				getInputStream()));

		return in;
	}
}
