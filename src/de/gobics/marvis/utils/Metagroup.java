package de.gobics.marvis.utils;

public class Metagroup implements Comparable<Metagroup> {

	private final String id;
	private final ReferenceDatabase database;
	private String description;
	private String url_group = null;
	private String url_compound = null;

	public Metagroup(String id, ReferenceDatabase source) {
		this(id, source, null);
	}

	public Metagroup(String id, ReferenceDatabase source, String description) {
		if (id == null) {
			throw new RuntimeException("ID can not be null");
		}
		this.id = id;
		this.database = source;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public String getIdFull() {
		return database.getId() + "/" + id;
	}

	public ReferenceDatabase getReferenceDatabase() {
		return database;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String nd) {
		description = nd;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +"{id="+getIdFull()+";\""+getDescription()+"\"}";
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Metagroup
				&& getIdFull().equals(((Metagroup)o).getIdFull());
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 37 * hash + (this.id != null ? this.id.hashCode() : 0);
		hash = 37 * hash + (this.database != null ? this.database.hashCode() : 0);
		return hash;
	}

	public int compareTo(Metagroup o) {
		return getIdFull().compareTo(o.getIdFull());
	}
}
