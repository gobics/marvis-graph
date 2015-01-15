package de.gobics.keggapi;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class InfoResult {
	public final String id;
	public final String name;
	public final String release;
	public final int num_entries;

	public InfoResult(String id, String name, String release, int num_entries) {
		this.id = id;
		this.name = name;
		this.release = release;
		this.num_entries = num_entries;
	}

	public static InfoResult parse(String text) {
		String[] lines = text.split("\n");
		String[] token = null;
		
		if( lines.length != 4){
			KeggAPI.logger.severe("Can not split info text in 4 lines:\n"+text);
			return null;
		}

		// Parse the name
		token = lines[0].split("\\s+", 2);
		String name = token[1];

		// Parse the ID and release
		token = lines[1].split("\\s+", 2);
		String id = token[0];
		String release = token[1].replace("Release ", "");

		// Parse the number of entries
		token = lines[3].split("\\s+");
		int num_entries = new Double(token[1].replace(",", "")).intValue();

		return new InfoResult(id, name, release, num_entries);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + id + " - "+name+"]";
	}
}
