package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
	// XAMPP MySQL Configuration
	// Hostname: localhost
	// Port: 5222 (MariaDB)
	// Database: parking_lot
	// User: root
	// Password: (empty - XAMPP default)
	private static final String URL = "jdbc:mysql://localhost:5222/parking_lot?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
	private static final String USER = "root";
	private static final String PASSWORD = "";

	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("MySQL JDBC Driver not found. Add the connector to the classpath.", e);
		}
	}

	/**
	 * Get a new Connection to the MySQL database. Caller must close the connection.
	 */
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

	/**
	 * Safe close helper (optional convenience).
	 */
	public static void closeQuietly(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException ignored) {
			}
		}
	}
}
