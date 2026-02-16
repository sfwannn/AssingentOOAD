package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

    // XAMPP MySQL Configuration (Port 5222 - MariaDB)
    private static final String BASE_URL = "jdbc:mysql://localhost:5222/"; // Note: No database name yet
    private static final String DB_NAME = "parking_lot";
    private static final String FULL_URL = "jdbc:mysql://localhost:5222/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // XAMPP default has no password

    public static void initialize() {
        try {
            // Step 1: Connect to MySQL Server (without selecting a DB) to create the DB
            try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("Checking database...");
                String createDbSql = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
                stmt.executeUpdate(createDbSql);
                System.out.println("Database '" + DB_NAME + "' confirmed.");
            }

            // Step 2: Connect to the new 'parking_lot' database to create tables
            try (Connection conn = DriverManager.getConnection(FULL_URL, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {

                // Create Users Table
                String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                        + "user_id VARCHAR(50) PRIMARY KEY, "
                        + "name VARCHAR(100), "
                        + "password VARCHAR(100), "
                        + "role VARCHAR(20), "
                        + "license_plate VARCHAR(20)"
                        + ")";
                stmt.executeUpdate(createUsersTable);

                // Create Payments Table
                String createPaymentsTable = "CREATE TABLE IF NOT EXISTS payments ("
                        + "payment_id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "license_plate VARCHAR(20), "
                        + "amount DOUBLE, "
                        + "method VARCHAR(20), "
                        + "parking_fee DOUBLE DEFAULT 0, "
                        + "fine_amount DOUBLE DEFAULT 0, "
                        + "payment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ")";
                stmt.executeUpdate(createPaymentsTable);

                // Create Fine Schemes Table
                String createFineSchemesTable = "CREATE TABLE IF NOT EXISTS fine_schemes ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "scheme_name VARCHAR(100) UNIQUE, "
                        + "is_active TINYINT(1) DEFAULT 0, "
                        + "changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ")";
                stmt.executeUpdate(createFineSchemesTable);

                // Create Fine Scheme History Table (logs all scheme changes over time)
                String createFineSchemeHistoryTable = "CREATE TABLE IF NOT EXISTS fine_scheme_history ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "scheme_name VARCHAR(100) NOT NULL, "
                        + "activated_at TIMESTAMP NOT NULL, "
                        + "INDEX idx_activated (activated_at)"
                        + ")";
                stmt.executeUpdate(createFineSchemeHistoryTable);

                // Insert Default Admin (Using INSERT IGNORE to prevent crashing if admin exists)
                String insertAdmin = "INSERT IGNORE INTO users (user_id, name, password, role) "
                        + "VALUES ('admin', 'System Administrator', 'admin123', 'ADMIN')";
                stmt.executeUpdate(insertAdmin);

                // Insert Default Fine Scheme
                String insertFineScheme = "INSERT IGNORE INTO fine_schemes (scheme_name, is_active) "
                        + "VALUES ('Option A (Fixed)', 1)";
                stmt.executeUpdate(insertFineScheme);
                
                // Insert initial history entry for default scheme
                String insertHistory = "INSERT IGNORE INTO fine_scheme_history (scheme_name, activated_at) "
                        + "VALUES ('Option A (Fixed)', CURRENT_TIMESTAMP)";
                stmt.executeUpdate(insertHistory);

                // Migrate existing tables - add entry_millis column if it doesn't exist
                try {
                    // Check if entry_millis column exists
                    java.sql.ResultSet rs = stmt.executeQuery(
                        "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = '" + DB_NAME + "' " +
                        "AND TABLE_NAME = 'parked_vehicles' " +
                        "AND COLUMN_NAME = 'entry_millis'");
                    
                    if (!rs.next()) {
                        // Column doesn't exist, add it
                        stmt.executeUpdate("ALTER TABLE parked_vehicles ADD COLUMN entry_millis BIGINT AFTER parking_spot");
                        System.out.println("Migration: Added entry_millis column to parked_vehicles table.");
                    }
                    
                    // Check and add license_plate column to payments if it doesn't exist
                    rs = stmt.executeQuery(
                        "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = '" + DB_NAME + "' " +
                        "AND TABLE_NAME = 'payments' " +
                        "AND COLUMN_NAME = 'license_plate'");
                    
                    if (!rs.next()) {
                        stmt.executeUpdate("ALTER TABLE payments ADD COLUMN license_plate VARCHAR(20) AFTER payment_id");
                        System.out.println("Migration: Added license_plate column to payments table.");
                    }
                    
                    // Check and add parking_fee column if it doesn't exist
                    rs = stmt.executeQuery(
                        "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = '" + DB_NAME + "' " +
                        "AND TABLE_NAME = 'payments' " +
                        "AND COLUMN_NAME = 'parking_fee'");
                    
                    if (!rs.next()) {
                        stmt.executeUpdate("ALTER TABLE payments ADD COLUMN parking_fee DOUBLE DEFAULT 0");
                        System.out.println("Migration: Added parking_fee column to payments table.");
                    }
                    
                    // Check and add fine_amount column if it doesn't exist
                    rs = stmt.executeQuery(
                        "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = '" + DB_NAME + "' " +
                        "AND TABLE_NAME = 'payments' " +
                        "AND COLUMN_NAME = 'fine_amount'");
                    
                    if (!rs.next()) {
                        stmt.executeUpdate("ALTER TABLE payments ADD COLUMN fine_amount DOUBLE DEFAULT 0");
                        System.out.println("Migration: Added fine_amount column to payments table.");
                    }

                    // Check and add changed_at column to fine_schemes if it doesn't exist
                    rs = stmt.executeQuery(
                        "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = '" + DB_NAME + "' " +
                        "AND TABLE_NAME = 'fine_schemes' " +
                        "AND COLUMN_NAME = 'changed_at'");
                    
                    if (!rs.next()) {
                        stmt.executeUpdate("ALTER TABLE fine_schemes ADD COLUMN changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                        System.out.println("Migration: Added changed_at column to fine_schemes table.");
                    }

                    // Check if fine_scheme_history table exists, if not create it
                    rs = stmt.executeQuery(
                        "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                        "WHERE TABLE_SCHEMA = '" + DB_NAME + "' " +
                        "AND TABLE_NAME = 'fine_scheme_history'");
                    
                    if (!rs.next()) {
                        String createHistoryTable = "CREATE TABLE fine_scheme_history ("
                                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                                + "scheme_name VARCHAR(100) NOT NULL, "
                                + "activated_at TIMESTAMP NOT NULL, "
                                + "INDEX idx_activated (activated_at)"
                                + ")";
                        stmt.executeUpdate(createHistoryTable);
                        System.out.println("Migration: Created fine_scheme_history table.");
                        
                        // Populate initial history entry
                        stmt.executeUpdate("INSERT IGNORE INTO fine_scheme_history (scheme_name, activated_at) " +
                                "SELECT scheme_name, changed_at FROM fine_schemes WHERE is_active = 1 LIMIT 1");
                    }
                } catch (SQLException e) {
                    System.err.println("Migration warning: " + e.getMessage());
                }

                System.out.println("Tables and Default Admin initialized successfully!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Database Initialization Error: " + e.getMessage());
        }
    }
}