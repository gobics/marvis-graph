/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class Database {

	private static final Logger logger = Logger.getLogger(Database.class.getName());
	private static final String db_driver = "org.postgresql.Driver";
	private static final String db_jdbc_driver_string = "postgresql";
	private final String db_host;
	private final String db_port;
	private final String db_user;
	private final String db_password;
	private final String db_database;
	private Metagroup default_metagroup = null;
	private Connection dbh = null;

	public Database(File property_file) throws IOException {
		this(getPropertiesFromFile(property_file));
	}

	public Database(Properties props){
		this(
				props.getProperty("marvis.db.user", "marvis"),
				props.getProperty("marvis.db.password", "marvis"),
				props.getProperty("marvis.db.database", "marvis"),
				props.getProperty("marvis.db.host", "localhost"),
				props.getProperty("marvis.db.port", "5432")
			);
	}

	public Database(){
		this("marvis", "marvis");
	}

	public Database(String user, String password){
		this(user, password, "marvis");
	}

	public Database(String user, String password, String database_name){
		this(user, password, database_name, "marvis", "5432");
	}

	public Database(String user, String password, String database_name, String host, String port){
		this.db_host = host;
		this.db_port= port;
		this.db_database = database_name;
		this.db_user = user;
		this.db_password = password;
	}

	/**
	 * Connect to the database. If a connection has already been
	 * established it will return
	 *
	 * @return TRUE if connection has been established
	 */
	private boolean connect() throws Exception {
		if (this.dbh != null) {
			logger.finest("Connection has already been established");
			return true;
		}

		/*
		try {
		throw new Exception("fooo");
		} catch (Exception ex){
		logger.log(Level.SEVERE, "This is how we get here: ", ex);
		}
		 */

		// Try to load the database driver
		try {
			Class.forName(db_driver);
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Can not load driver '" + db_driver + "': ", e);
			throw e;
		}

		Properties connection_properties = new Properties();
		connection_properties.setProperty("user", db_user);
		connection_properties.setProperty("password", db_password);

		try {
			logger.fine("Connection to database via: jdbc:" + db_jdbc_driver_string + "://" + db_host + ":" + db_port + "/" + db_database);
			dbh = DriverManager.getConnection("jdbc:" + db_jdbc_driver_string + "://" + db_host + ":" + db_port + "/" + db_database, connection_properties);
			dbh.setAutoCommit(false);
		} catch (SQLException s) {
			logger.severe("Can not connect to database: " + s);
			throw s;
		}

		logger.info("Connected to backend database");
		return true;
	}

	protected Connection getConnection() throws Exception{
		connect();
		return dbh;
	}

	/**
	 * disconnect from the database
	 */
	public boolean disconnect() throws Exception {
		if (this.dbh == null) {
			logger.fine("There is no connection available");
			return true;
		}

		try {
			this.dbh.close();
		} catch (SQLException s) {
			logger.warning("Error closing connection: " + s.getMessage());
		}

		this.dbh = null;
		logger.info("Disonnect from backend database");

		return true;
	}

	public void commit() throws Exception {
		commit(false);
	}

	public void commit(boolean do_vacuum_analyze) throws Exception {
		if (this.dbh != null) {
			logger.finer("Commiting database handler");
			dbh.commit();
			if (do_vacuum_analyze) {
				logger.finer("Vacuum analyzing database");
				dbh.setAutoCommit(true);
				query("VACUUM ANALYZE").executeUpdate();
				dbh.setAutoCommit(false);
			}
		}
	}

	public PreparedStatement query(String sqlb) throws Exception {
		logger.finest("Preparing query: " + sqlb);
		return getConnection().prepareStatement(sqlb);
	}

	public ReferenceDatabase[] getReferenceDatabases() throws Exception {
		TreeSet<ReferenceDatabase> dbs = new TreeSet<ReferenceDatabase>();
		for (String id : getReferenceDatabaseIds()) {
			dbs.add(getReferenceDatabase(id));
		}
		return dbs.toArray(new ReferenceDatabase[dbs.size()]);
	}

	public String[] getReferenceDatabaseIds() throws Exception {
		String sqlb = "SELECT id FROM " + getTable("public", "databases");
		TreeSet<String> ids = new TreeSet<String>();
		ResultSet rs = query(sqlb).executeQuery();

		while (rs.next()) {
			ids.add(rs.getString(1));
		}

		return ids.toArray(new String[ids.size()]);
	}

	public ReferenceDatabase getReferenceDatabase(String id) throws Exception {
		String sqlb = "SELECT label, url, url_group, url_compound, license, updated FROM " + getTable("public", "databases") + " WHERE id = ?";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, id);
		ResultSet rs = ps.executeQuery();

		if (!rs.next()) {
			return null;
		}

		ReferenceDatabase rdb = new ReferenceDatabase(id);
		rdb.setLabel(rs.getString(1));
		rdb.setUrl(rs.getString(2));
		rdb.setUrlGroup(rs.getString(3));
		rdb.setUrlCompound(rs.getString(4));
		rdb.setUrlLicense(rs.getString(5));
		rdb.setUpdated(rs.getString(6));

		return rdb;
	}

	public Metagroup[] getMetagroups(ReferenceDatabase db) throws Exception {
		String sqlb = "SELECT id FROM " + getTable("public", "metagroups") + " WHERE database = ?";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, db.getId());
		ResultSet rs = ps.executeQuery();

		TreeSet<Metagroup> mgs = new TreeSet<Metagroup>();
		while (rs.next()) {
			mgs.add(getMetagroup(db, rs.getString(1)));
		}

		return mgs.toArray(new Metagroup[mgs.size()]);
	}

	public Metagroup[] getMetagroupsAll() throws Exception {
		TreeSet<Metagroup> mgs = new TreeSet<Metagroup>();

		for (ReferenceDatabase rdb : getReferenceDatabases()) {
			mgs.addAll(Arrays.asList(getMetagroups(rdb)));
		}

		return mgs.toArray(new Metagroup[mgs.size()]);
	}

	public Metagroup getMetagroup(ReferenceDatabase db, String mg_id) throws Exception {
		PreparedStatement ps = query("SELECT description FROM " + getTable("public", "metagroups") + " WHERE database =? AND id = ?");
		ps.setString(1, db.getId());
		ps.setString(2, mg_id);
		ResultSet rs = ps.executeQuery();

		if (!rs.next()) {
			return null;
		}

		Metagroup mg = new Metagroup(mg_id, db);
		mg.setDescription(rs.getString(1));

		return mg;
	}

	public Group[] getGroups(Metagroup metagroup) throws Exception {
		TreeSet<Group> groups = new TreeSet<Group>();
		String sqlb = "SELECT id FROM " + getTable(metagroup, "group");
		ResultSet rs = query(sqlb).executeQuery();

		while (rs.next()) {
			groups.add(getGroup(metagroup, rs.getString(1)));
		}

		return groups.toArray(new Group[groups.size()]);
	}

	public Molecule getMolecule(Metagroup metagroup, String id) throws Exception {
		String sqlb = "SELECT o.name, o.formula, o.smiles, o.inchi, c.formula, o.mass "
				+ "FROM " + getTable(metagroup, "compound") + " as o "
				+ "LEFT OUTER JOIN " + getTable("curated_compound", metagroup.getReferenceDatabase().getId()) + " as c ON c.id = o.id "
				+ "WHERE o.id = ?";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, id);
		ResultSet rs = ps.executeQuery();

		if (!rs.next()) {
			return null;
		}
		Molecule m = new Molecule(id, metagroup);
		m.setName(rs.getString(1));
		m.setName(rs.getString(1));
		m.setFormula(rs.getString(2));
		m.setSmiles(rs.getString(3));
		m.setInchi(rs.getString(4));
		m.setFormulaCurated(rs.getString(5));
		m.setMass(rs.getDouble(6));

		return m;
	}

	public Group getGroup(Metagroup metagroup, String id) throws Exception {
		String sqlb = "SELECT name, description FROM " + getTable(metagroup, "group") + " WHERE id = ?";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, id);
		ResultSet rs = ps.executeQuery();

		if (!rs.next()) {
			return null;
		}

		Group g = new Group(id, metagroup);
		g.setName(rs.getString(1));
		g.setDescription(rs.getString(2));

		return g;
	}

	public Molecule[] getAssignments(Group g) throws Exception {
		TreeSet<Molecule> molecules = new TreeSet<Molecule>();

		String sqlb = "SELECT compound FROM " + getTable(g.getMetagroup(), "assignment") + " WHERE \"group\" = ?";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, g.getId());
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			molecules.add(getMolecule(g.getMetagroup(), rs.getString(1)));
		}

		return molecules.toArray(new Molecule[molecules.size()]);
	}

	public Group[] getAssignments(Molecule g) throws Exception {
		TreeSet<Group> molecules = new TreeSet<Group>();

		String sqlb = "SELECT \"group\" FROM " + getTable(g.getMetagroup(), "assignment") + " WHERE \"compound\" = ?";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, g.getId());
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			molecules.add(getGroup(g.getMetagroup(), rs.getString(1)));
		}

		return molecules.toArray(new Group[molecules.size()]);
	}

	protected final String getTable(Metagroup metagroup, String table) {
		return getTable(getSchema(metagroup), table);
	}

	protected final String getSchema(Metagroup metagroup) {
		return "mg_" + metagroup.getReferenceDatabase().getId() + "_" + metagroup.getId();
	}

	protected final String getTable(String schema, String table) {
		return "\"" + schema + "\".\"" + table + "\"";
	}

	public boolean saveMoleculeCurated(Molecule formula) throws Exception {
		logger.finer("Saving curated information about" + formula);
		String tablename = getTable("curated_compound", formula.getReferenceDatabase().getId());

		// Drop curated formula
		PreparedStatement ps = query("DELETE FROM " + tablename + " WHERE id = ?");
		ps.setString(1, formula.getId());
		boolean row_changed = ps.executeUpdate() > 0;

		// Check if the molecule has a curated formula
		if (formula.isFormulaCurated()) {
			ps = query("INSERT INTO " + tablename + " (id, formula, mass) VALUES (?, ?, ?)");
			ps.setString(1, formula.getId());
			ps.setString(2, formula.getFormulaCurated());
			ps.setDouble(3, formula.getMassCurated());

			row_changed = ps.executeUpdate() > 0;
		}

		return row_changed;
	}

	protected boolean tableExists(String schema, String tablename) throws Exception {
		PreparedStatement ps = query("SELECT count(*) FROM information_schema.tables "
				+ "WHERE table_catalog = ? AND table_schema = ? and table_name = ?");
		ps.setString(1, db_database);
		ps.setString(2, schema);
		ps.setString(3, tablename);
		ResultSet rs = ps.executeQuery();
		rs.next();
		return rs.getInt(1) == 1;
	}

	protected boolean schemaExists(String schema) throws Exception {
		PreparedStatement ps = query("SELECT count(*) FROM information_schema.tables"
				+ " WHERE table_catalog = ? AND table_schema = ?");
		ps.setString(1, db_database);
		ps.setString(2, schema);
		ResultSet rs = ps.executeQuery();
		rs.next();
		return rs.getInt(1) > 0;
	}

	public void loadMoleculeCrossDatabaseReferences(Molecule m) throws Exception {
		LinkedList<CrossDatabaseReferenceEntry> entries = new LinkedList<CrossDatabaseReferenceEntry>();
		entries.add(new CrossDatabaseReferenceEntry(m.getReferenceDatabase(), m.getId()));

		// Prepare statements to search for the bidirectional relationships
		PreparedStatement ps_from = query(
				"SELECT database_to, molecule_to "
				+ "  FROM " + getTable("public", "cr_molecule")
				+ " WHERE database_from = ? "
				+ "   AND molecule_from = ?");
		PreparedStatement ps_to = query(
				"SELECT database_from, molecule_from "
				+ "  FROM " + getTable("public", "cr_molecule")
				+ " WHERE database_to = ? "
				+ "   AND molecule_to = ?");

		// Calculate all
		for (int idx = 0; idx < entries.size(); idx++) {
			CrossDatabaseReferenceEntry current_entry = entries.get(idx);

			ps_from.setString(1, current_entry.getReferenceDatabase().getId());
			ps_from.setString(2, current_entry.getOtherId());
			ps_to.setString(1, current_entry.getReferenceDatabase().getId());
			ps_to.setString(2, current_entry.getOtherId());

			ResultSet rs = ps_from.executeQuery();

			while (rs.next()) {
				CrossDatabaseReferenceEntry new_entry = new CrossDatabaseReferenceEntry(new ReferenceDatabase(rs.getString(1)), rs.getString(2));
				if (!entries.contains(new_entry)) {
					entries.add(new_entry);
				}
			}

			rs = ps_to.executeQuery();
			while (rs.next()) {
				CrossDatabaseReferenceEntry new_entry = new CrossDatabaseReferenceEntry(new ReferenceDatabase(rs.getString(1)), rs.getString(2));
				if (!entries.contains(new_entry)) {
					entries.add(new_entry);
				}
			}
		}

		// Put all entries in the molecule
		for (CrossDatabaseReferenceEntry entry : entries) {
			m.addDatabaseReference(entry.getReferenceDatabase(), entry.getOtherId());
		}
	}

	public Molecule[] getMolecules(Metagroup metagroup) throws Exception {
		TreeSet<Molecule> molecules = new TreeSet<Molecule>();

		ResultSet rs = query("SELECT id FROM " + getTable(metagroup, "compound")).executeQuery();

		while (rs.next()) {
			molecules.add(getMolecule(metagroup, rs.getString(1)));
		}

		return molecules.toArray(new Molecule[molecules.size()]);
	}

	private static Properties getPropertiesFromFile(File file) throws IOException{
		Properties props = new Properties();
		props.load( new FileInputStream(file) );
		return props;
	}
}

class CrossDatabaseReferenceEntry implements Comparable<CrossDatabaseReferenceEntry> {

	private final ReferenceDatabase database;
	private final String other_id;

	public ReferenceDatabase getReferenceDatabase() {
		return database;
	}

	public String getOtherId() {
		return other_id;
	}

	public CrossDatabaseReferenceEntry(ReferenceDatabase rdb, String mid) {
		database = rdb;
		other_id = mid;
	}

	public boolean equals(CrossDatabaseReferenceEntry o) {
		return getReferenceDatabase().equals(o.getReferenceDatabase())
				&& getOtherId().equals(o.getOtherId());
	}

	public int compareTo(CrossDatabaseReferenceEntry o) {
		int c = getReferenceDatabase().compareTo(o.getReferenceDatabase());
		if (c != 0) {
			return c;
		}
		return getOtherId().compareTo(o.getOtherId());
	}

}
