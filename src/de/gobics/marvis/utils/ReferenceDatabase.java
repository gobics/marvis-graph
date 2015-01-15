package de.gobics.marvis.utils;

import java.util.logging.Logger;


/**
 *
 * @author manuel
 */
public class ReferenceDatabase implements Comparable<ReferenceDatabase>{
	private static final Logger logger = Logger.getLogger(ReferenceDatabase.class.getName());
	
	private final String id;
	private String label = null;
	private String url = null;
	private String url_license = null;
	private String url_group = null;
	private String url_compound = null;
	private String updated = null;


	public ReferenceDatabase(String id){
		this.id = id.toLowerCase();
		setLabel(id);
	}

	@Override
	public String toString(){
		return getClass().getSimpleName() +"{id="+getId()+"}";
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getUpdated() {
		return updated;
	}

	public String getUrl() {
		return url;
	}

	public String getUrlCompound() {
		return url_compound;
	}

	public String getUrlGroup() {
		return url_group;
	}

	public String getUrlLicense() {
		return url_license;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setUrlCompound(String url_compound) {
		this.url_compound = url_compound;
	}

	public void setUrlGroup(String url_group) {
		this.url_group = url_group;
	}

	public void setUrlLicense(String url_license) {
		this.url_license = url_license;
	}

	@Override
	public boolean equals(Object o){
		return o instanceof ReferenceDatabase
			&& getId().equals(((ReferenceDatabase)o).getId());
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 71 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}
	
	public int compareTo(ReferenceDatabase o) {
		return getId().compareTo( o.getId() );
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
