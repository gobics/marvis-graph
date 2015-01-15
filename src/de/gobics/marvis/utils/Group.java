/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils;

import java.util.TreeSet;

/**
 *
 * @author manuel
 */
public class Group implements Comparable<Group> {

	private final String id;
	private final Metagroup metagroup;
	private String name = null;
	private String description = null;
	private final TreeSet<Molecule> molecules = new TreeSet<Molecule>();

	public Group(String id, Metagroup metagroup) {
		if( id == null )
			throw new NullPointerException("Id is null");
		if( metagroup == null )
			throw new NullPointerException("Metagroup is null");
		
		this.id = id;
		setName(id);
		this.metagroup = metagroup;
	}

	public String getId() {
		return id;
	}

	public ReferenceDatabase getReferenceDatabase() {
		return metagroup.getReferenceDatabase();
	}

	public void setName(String new_name) {
		name = new_name;
		if( name == null || name.isEmpty() )
			name = getId();	
	}

	public String getName() {
		return name;
	}

	public void setDescription(String new_desc) {
		description = new_desc;
	}

	public String getDescription() {
		return description;
	}

	public boolean assignMolecule(Molecule m) {
		if (m.getReferenceDatabase().equals(getReferenceDatabase())) {
			return molecules.add(m);
		}
		return false;
	}

	public String getUrl(){
		return getReferenceDatabase().getUrlGroup().replace("$ID", getId());
	}

	public Metagroup getMetagroup() {
		return metagroup;
	}

	public int compareTo(Group o) {
		int c = getReferenceDatabase().compareTo( o.getReferenceDatabase() );
		if( c == 0 ){
			c = getId().compareTo( o.getId());
		}
		return c;
	}

	@Override
	public String toString(){
		return getClass().getSimpleName()+"{id="+getId()+"; rdb="+getReferenceDatabase().getId()+"}";
	}
}
