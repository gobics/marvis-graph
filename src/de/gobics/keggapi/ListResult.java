/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.keggapi;

import java.util.LinkedList;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class ListResult {

	private final String[] ids;
	private final String[] names;

	public ListResult(String[] ids, String[] names) {
		this.ids = ids;
		this.names = names;
	}
	
	public static ListResult parse(String text, int col_id, int col_name){
		LinkedList<String> ids = new LinkedList<String>();
		LinkedList<String> names = new LinkedList<String>();
		
		for(String line : text.split("\n")){
			String[] token = line.split("\t");
			ids.add(token[col_id]);
			names.add(token[col_name]);
		}
		
		return new ListResult(
				ids.toArray(new String[ids.size()]), 
				names.toArray(new String[names.size()])
			);
	}
	
}
