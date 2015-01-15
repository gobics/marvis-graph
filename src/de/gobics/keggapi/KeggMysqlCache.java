package de.gobics.keggapi;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The awesome new KeggMysqlCache
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class KeggMysqlCache extends KeggAPICached {

	private static final Logger logger = Logger.getLogger(KeggMysqlCache.class.
			getName());
	private Connection mysql_connection;
	private final String host;
	private final String user;
	private final String password;
	private final String database;

	public KeggMysqlCache() {
		this("biofung.gobics.de", "keggcache", "keggcache", "keggcache");
	}

	public KeggMysqlCache(String host, String user, String password, String database) {
		this.host = host;
		this.user = user;
		this.password = password;
		this.database = database;
	}

	@Override
	protected String getCached(String url) {
		try {
			if (!tryConnect()) {
				return null;
			}
			PreparedStatement st = mysql_connection.prepareStatement(
					"SELECT `result` FROM `keggcache2` "
					+ " WHERE `url` = ? AND CURRENT_DATE - CAST(downloaded AS DATE) < " + getCacheTime());
			st.setString(1, url);
			ResultSet rs = st.executeQuery();
			if (!rs.next()) { // If there is no result return null
				return null;
			}

			return rs.getString(1);
		}
		catch (SQLException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	protected void storeCached(String url, String result) {
		try {
			if (tryConnect()) {
				PreparedStatement st = mysql_connection.prepareStatement("REPLACE INTO keggcache2 (url, result, downloaded) VALUES (?, ?, NOW())");
				st.setString(1, url);
				st.setString(2, result);
				st.execute();
			}
		}
		catch (SQLException ex) {
			logger.log(Level.SEVERE, "Can not store cached data: ", ex);
		}
	}

	@Override
	public void removeFromCache(String url) {
		try {
			if (tryConnect()) {
				PreparedStatement st = mysql_connection.prepareStatement("DELETE FROM keggcache2 WHERE url=?");
				st.setString(1, url);
				st.execute();
			}
		}
		catch (SQLException ex) {
			logger.log(Level.SEVERE, "Can not remove cached data: ", ex);
		}
	}

	public boolean tryConnect() {
		try {
			if (mysql_connection != null && !mysql_connection.isClosed()) {
				return true;
			}
			if (mysql_connection != null) {
				mysql_connection.close();
				mysql_connection = null;
			}
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			mysql_connection = DriverManager.getConnection(
					"jdbc:mysql://" + host + "/" + database + "?user=" + user + "&password=" + password);

			ResultSet rs = mysql_connection.prepareStatement("SHOW TABLES").
					executeQuery();
			boolean found = false;
			while (rs.next()) {
				if (rs.getString(1).equals("keggcache2")) {
					found = true;
				}
			}

			if (!found) {
				mysql_connection.prepareStatement("CREATE TABLE IF NOT EXISTS `keggcache2` ( "
						+ "   `url` varchar(200) CHARACTER SET ascii NOT NULL,"
						+ "   `result` longtext CHARACTER SET utf8 NOT NULL,  "
						+ "   `downloaded` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
						+ "   PRIMARY KEY (`url`))").executeUpdate();
			}

			return true;
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		return false;
	}
	
	public void disconnect(){
		try {
			if( mysql_connection != null && ! mysql_connection.isClosed()){
				mysql_connection.close();
				mysql_connection = null;
			}
		}
		catch (SQLException ex) {
			logger.log(Level.SEVERE, "Can not disconnect: ", ex);
		}
	}
}
