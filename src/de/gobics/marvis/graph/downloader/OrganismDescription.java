package de.gobics.marvis.graph.downloader;

/**
 * Simple class to contain information about available organisms.
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class OrganismDescription {

	public final String id;
	public final String name;
	public final String description;

	public OrganismDescription(String i, String n) {
		this(i, n, "");
	}

	public OrganismDescription(String i, String n, String description) {
		this.id = i;
		this.name = n;
		this.description = description;
	}
	
}
