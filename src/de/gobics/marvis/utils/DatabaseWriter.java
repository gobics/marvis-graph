/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class DatabaseWriter extends de.gobics.marvis.utils.Database {

	private static final Logger logger = Logger.getLogger(DatabaseWriter.class.getName());

	public DatabaseWriter(File property_file) throws IOException {
		super(property_file);
	}

	public DatabaseWriter(Properties props) {
		super(props);
	}

	public DatabaseWriter() {
		super();
	}

	public DatabaseWriter(String user, String password) {
		super(user, password);
	}

	public DatabaseWriter(String user, String password, String database_name) {
		super(user, password, database_name);
	}

	public DatabaseWriter(String user, String password, String database_name, String host, String port) {
		super(user, password, database_name, host, port);
	}
	
	/**
	 * Removes all metagroups of a given reference database from the SQL database.
	 */
	public void clearDatabase(ReferenceDatabase db) throws Exception{
		for(Metagroup mg : getMetagroups(db)){
			deleteMetagroup(mg);
		}
	}

	public boolean prepareDatabase(Metagroup metagroup) throws Exception {
		logger.info("Preparing database for insertion of " + metagroup);

		if (getReferenceDatabase(metagroup.getReferenceDatabase().getId()) == null) {
			insertNewReferenceDatabase(metagroup.getReferenceDatabase().getId());
		}

		/*
		if (metagroup.getReferenceDatabase().getId().equals("pwtools")) {
			deleteMetagroup(metagroup);
		}*/

		if (getMetagroup(metagroup.getReferenceDatabase(), metagroup.getId()) == null) {
			insertNewMetagroup(metagroup.getReferenceDatabase(), metagroup.getId(), metagroup.getDescription());
		} else {
			updateMetagroup(metagroup.getReferenceDatabase(), metagroup.getId(), metagroup.getDescription());
		}
		
		// Dropping all
		if (schemaExists(getSchema(metagroup))) {
			logger.finer("Dropping schema: " + getSchema(metagroup));
			if (query("DROP SCHEMA \"" + getSchema(metagroup) + "\" CASCADE").executeUpdate() != 0) {
				logger.warning("Can not drop schema");
			}
		}

		logger.finer("Creating schema: " + getSchema(metagroup));
		query("CREATE SCHEMA \"" + getSchema(metagroup) + "\"").executeUpdate();

		// Create compound table
		logger.finest("Creating compound table");
		query("CREATE TABLE " + getTable(metagroup, "compound") + " ("
				+ "id 	VARCHAR(200) PRIMARY KEY, "
				+ "name TEXT NOT NULL, "
				+ "formula TEXT DEFAULT NULL, "
				+ "mass NUMERIC(14,7) DEFAULT NULL, "
				+ "smiles TEXT DEFAULT NULL, "
				+ "inchi TEXT DEFAULT NULL, "
				+ "information TEXT DEFAULT NULL )").executeUpdate();

		// Create group table
		logger.finest("Creating group table");
		query("CREATE TABLE " + getTable(metagroup, "group") + " ("
				+ "id VARCHAR(200) PRIMARY KEY, "
				+ "name TEXT NOT NULL, "
				+ "description TEXT DEFAULT NULL, "
				+ "group_size INTEGER )").executeUpdate();

		// Create assignment
		logger.finest("Creating assignment table");
		query("CREATE TABLE " + getTable(metagroup, "assignment") + " ( "
				+ "compound VARCHAR(200) REFERENCES " + getTable(metagroup, "compound") + " (id), "
				+ "\"group\" VARCHAR(200) REFERENCES " + getTable(metagroup, "group") + " (id), "
				+ "PRIMARY KEY (compound, \"group\") "
				+ ")").executeUpdate();


		/*
		logger.finest("Commit schema creation");
		commit();
		 */
		return true;
	}

	public boolean deleteMetagroup(Metagroup metagroup) throws Exception {
		PreparedStatement ps = query("DELETE FROM " + getTable("public", "metagroups") + " WHERE database = ? AND id = ? ");
		ps.setString(1, metagroup.getReferenceDatabase().getId());
		ps.setString(2, metagroup.getId());
		ps.executeUpdate();

		// Dropping all
		if (schemaExists(getSchema(metagroup))) {
			logger.finer("Dropping schema: " + getSchema(metagroup));
			if (query("DROP SCHEMA \"" + getSchema(metagroup) + "\" CASCADE").executeUpdate() != 0) {
				logger.warning("Can not drop schema");
			}
		}
		return true;
	}

	public boolean deleteMolecule(Metagroup metagroup, Molecule molecule) throws Exception {
		String sqlb = "DELETE FROM " + getTable(metagroup, "compound") + " WHERE id = ?";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, molecule.getId());
		logger.finer("Deleting molecule " + molecule + " from " + metagroup);
		return ps.executeUpdate() > 0;
	}

	public boolean saveMolecule(Molecule molecule) throws Exception {
		return saveMolecule(molecule.getMetagroup(), molecule);
	}

	public boolean saveMolecule(Metagroup metagroup, Molecule molecule) throws Exception {
		// Delete first
		deleteMolecule(metagroup, molecule);

		logger.finer("Try to save " + molecule);
		// Try to calculate exact mass
		logger.finest("Try to calculate mass");
		double mass = molecule.getMass();
		try {
			// Check if formula can be parsed and set the mass to exact mass
			if (molecule.getFormulaAsObject() != null) {
				mass = molecule.getFormulaAsObject().getMass();
			}
			if (molecule.getInchiFormulaObject() != null) {
				mass = molecule.getInchiFormulaObject().getMass();
			}

		} catch (Exception ex) {
			logger.warning("Can not calculate mass from " + molecule + ": " + ex.getMessage());
		}
		if (mass < 0) {
			logger.warning("Mass is not valid for: " + molecule);
			mass = 0;
		}

		String sqlb = "INSERT INTO " + getTable(metagroup, "compound")
				+ " (id, name, formula, mass, information, smiles, inchi) "
				+ " VALUES (?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, molecule.getId());
		ps.setString(2, molecule.getName());
		ps.setString(3, molecule.getFormula());
		ps.setDouble(4, mass);
		ps.setString(5, molecule.getDescription());
		ps.setString(6, molecule.getSmiles());
		ps.setString(7, molecule.getInchi());

		ps.executeUpdate();

		if (molecule.isFormulaCurated()) {
			saveMoleculeCurated(molecule);
		}
		saveMoleculeCrossDatabaseReferences(metagroup, molecule);

		logger.finer("Saved molecule " + molecule + " in " + metagroup);
		return true;
	}

	private boolean saveMoleculeCrossDatabaseReferences(Metagroup metagroup, Molecule molecule) throws Exception {
		logger.finer("Deleting cross database references for: " + molecule);
		String sqlb = "DELETE FROM " + getTable("public", "cr_molecule")
				+ " WHERE \"database_from\" = ? AND \"molecule_from\" = ?";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, metagroup.getReferenceDatabase().getId());
		ps.setString(2, molecule.getId());

		ps.executeUpdate();

		logger.finer("Adding cross database references for: " + molecule);
		ps = query("INSERT INTO " + getTable("public", "cr_molecule")
				+ " (database_from, molecule_from, database_to, molecule_to) VALUES (?, ?, ?, ?)");
		ps.setString(1, metagroup.getReferenceDatabase().getId());
		ps.setString(2, molecule.getId());

		for (ReferenceDatabase rdb2 : molecule.getCrossDatabaseReferenceDatabases()) {

			// Check if the reference database exist
			if (getReferenceDatabase(rdb2.getId()) == null) {
				rdb2 = insertNewReferenceDatabase(rdb2.getId());
			}

			String other_molecule = molecule.getDatabaseReference(rdb2);

			// Insert the assignment
			ps.setString(3, rdb2.getId());
			ps.setString(4, molecule.getDatabaseReference(rdb2));
			if (ps.executeUpdate() != 1) {
				logger.warning("Can not store cross database reference: "
						+ molecule.getIdFull() + " == "
						+ rdb2.getId() + "/" + other_molecule);
			}
		}

		return true;
	}

	public boolean deleteGroup(Metagroup metagroup, Group group) throws Exception {
		logger.finer("Deleting group " + group + " from " + metagroup);
		String sqlb = "DELETE FROM " + getTable(metagroup, "group") + " WHERE id = ?";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, group.getId());
		return ps.executeUpdate() > 0;
	}

	public boolean saveGroup(Metagroup metagroup, Group group) throws Exception {
		// Delete first
		deleteGroup(metagroup, group);
		logger.finer("Saving group " + group + " in " + metagroup);

		String sqlb = "INSERT INTO " + getTable(metagroup, "group")
				+ " (id, name, description) "
				+ " VALUES (?, ?, ?)";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, group.getId());
		ps.setString(2, group.getName());
		ps.setString(3, group.getDescription());

		return ps.executeUpdate() > 0;
	}

	public boolean deleteAssignment(Metagroup metagroup, Group group, Molecule molecule) throws Exception {
		logger.finer("Deleting assignment from " + metagroup + ": " + molecule + " <=> " + group);
		String sqlb = "DELETE FROM " + getTable(metagroup, "assignment") + " WHERE compound = ? AND \"group\" = ?";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, molecule.getId());
		ps.setString(2, group.getId());
		return ps.executeUpdate() > 0;
	}

	public boolean saveAssignment(Metagroup metagroup, Group group, Molecule molecule) throws Exception {
		logger.finer("Saving assignment from " + metagroup + ": " + molecule + " <=> " + group);
		// Delete first
		deleteAssignment(metagroup, group, molecule);
		String sqlb = "INSERT INTO " + getTable(metagroup, "assignment")
				+ " (compound, \"group\") "
				+ " VALUES (?, ?)";
		PreparedStatement ps = query(sqlb);
		ps.setString(1, molecule.getId());
		ps.setString(2, group.getId());
		return ps.executeUpdate() > 0;
	}

	public void commitDatabaseUpdate(Metagroup mg) throws Exception {
		logger.info("Commiting database update for " + mg);
		String sqlb = null;
		int result = -1;

		// Insert "UNASSIGNED" pseudo group
		if (getGroup(mg, "UNASSIGNED") == null) {
			logger.fine("Creating group for unassigned molecules");
			saveGroup(mg, new Group("UNASSIGNED", mg));

			sqlb = "INSERT INTO " + getTable(mg, "assignment") + " "
					+ "SELECT id, 'UNASSIGNED' FROM " + getTable(mg, "compound")
					+ " WHERE NOT id IN (SELECT DISTINCT compound FROM " + getTable(mg, "assignment") + ")";
			logger.finer("Query is: " + sqlb);
			result = query(sqlb).executeUpdate();
			logger.finer(result + " molecules in unassigned group");
		}

		// Remove empty groups
		logger.fine("Removing empty groups");
		sqlb = "DELETE FROM " + getTable(mg, "group")
				+ " WHERE NOT id IN (SELECT DISTINCT \"group\" FROM " + getTable(mg, "assignment") + ")";
		logger.finer("Query is: " + sqlb);
		result = query(sqlb).executeUpdate();
		logger.finer("Removed " + result + " empty groups");

		// Update group sizes
		logger.fine("Calculating group size");
		sqlb = "UPDATE " + getTable(mg, "group") + " as base "
				+ "SET group_size = ("
				+ "  SELECT count(*) FROM " + getTable(mg, "assignment") + " as ref "
				+ "   WHERE base.id = ref.group)";
		logger.finer("Query is: " + sqlb);
		result = query(sqlb).executeUpdate();
		logger.finer("Updated size of " + result + " groups");


		// Create mapping view
		logger.fine("Calculating mapping view");
		sqlb = "CREATE OR REPLACE VIEW " + getTable(mg, "mapping") + " AS "
				+ "SELECT compound.id as compound_id, compound.name as compound_name, compound.formula as compound_formula, compound.mass as compound_mass, compound.information as compound_information, \"group\".id as group_id, \"group\".name as group_name, \"group\".description as group_description, \"group\".group_size"
				+ "  FROM " + getTable(mg, "compound") + ", " + getTable(mg, "assignment") + ", " + getTable(mg, "group")
				+ " WHERE compound.id = assignment.compound "
				+ "   AND assignment.\"group\" = \"group\".id";
		logger.finer("Query is: " + sqlb);
		query(sqlb).executeUpdate();

		logger.fine("Creating MarVis-Compound view for database " + mg.getReferenceDatabase());
		Metagroup[] metagroups = getMetagroups(mg.getReferenceDatabase());
		StringBuilder sb = new StringBuilder("CREATE OR REPLACE VIEW ").append(getTable("cache", "db_" + mg.getReferenceDatabase().getId())).append(" AS SELECT DISTINCT ON (id) * FROM ").append(getTable(mg, "compound"));
		for (Metagroup mg2 : metagroups) {
			if (!mg2.equals(mg)) {
				sb.append(" UNION (SELECT * FROM ").append(getTable(mg2, "compound")).append(")");
			}
		}
		logger.finer("Query is: " + sb);
		query(sb.toString()).executeUpdate();


		logger.fine("Grant select for MarVis-Compound");
		query("GRANT SELECT ON " + getTable("cache", "db_" + mg.getReferenceDatabase().getId()) + " TO mtblmx").executeUpdate();

		logger.fine("Setting update timestamp for database");
		PreparedStatement ps = query("UPDATE databases SET updated = NOW() WHERE id = ?");
		ps.setString(1, mg.getReferenceDatabase().getId());
		ps.executeUpdate();

		logger.fine("Doing database commit");
		commit(true);
	}

	private ReferenceDatabase insertNewReferenceDatabase(String id) throws Exception {
		ReferenceDatabase rdb = new ReferenceDatabase(id);
		logger.info("Creating new reference database: " + id);
		PreparedStatement ps = query("INSERT INTO " + getTable("public", "databases")
				+ " (id) VALUES (?)");
		ps.setString(1, id);
		ps.executeUpdate();

		if (!tableExists("curated_compound", id)) {
			query("CREATE TABLE " + getTable("curated_compound", id) + " ( "
					+ "id VARCHAR(200) PRIMARY KEY,"
					+ "name TEXT DEFAULT NULL, "
					+ "formula TEXT DEFAULT NULL, "
					+ "mass NUMERIC(14,7) DEFAULT NULL, "
					+ "smiles TEXT DEFAULT NULL, "
					+ "inchi TEXT DEFAULT NULL, "
					+ "information TEXT DEFAULT NULL )").executeUpdate();
		}
		return rdb;
	}

	private Metagroup insertNewMetagroup(ReferenceDatabase rdb, String id, String descr) throws Exception {
		Metagroup meta = new Metagroup(id, rdb);

		logger.info("Creating new metagroup: " + id);
		PreparedStatement ps = query("INSERT INTO " + getTable("public", "metagroups")
				+ " (id, database, description) VALUES (?, ?, ?)");
		ps.setString(1, id);
		ps.setString(2, rdb.getId());
		ps.setString(3, descr);
		if (ps.executeUpdate() != 1) {
			throw new RuntimeException("Can not insert new metagroup");
		}
		return meta;
	}

	private void updateMetagroup(ReferenceDatabase rdb, String id, String descr) throws Exception {
		logger.info("Creating new metagroup: " + id);
		PreparedStatement ps = query("UPDATE " + getTable("public", "metagroups")
				+ " SET description = ? WHERE id = ? AND database = ?");
		ps.setString(1, descr);
		ps.setString(2, id);
		ps.setString(3, rdb.getId());
		if (ps.executeUpdate() != 1) {
			throw new RuntimeException("Can not insert new metagroup");
		}
	}
}
