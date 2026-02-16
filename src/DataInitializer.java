import java.sql.Connection;
import java.sql.SQLException;


public class DataInitializer {

        private static final SpotType[] SPOT_TYPES = SpotType.values();

    public static void createDatabaseTables(Connection conn) throws SQLException {
        System.out.println("Creating database tables...");
        
        try (java.sql.Statement stmt = conn.createStatement()) {
            // Create parked_vehicles table
            String createParkedVehiclesSQL = "CREATE TABLE IF NOT EXISTS parked_vehicles ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "license_plate VARCHAR(50) NOT NULL UNIQUE,"
                    + "vehicle_type VARCHAR(50),"
                    + "entry_time VARCHAR(50),"
                    + "parking_spot VARCHAR(50),"
                    + "entry_millis BIGINT,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")";
            stmt.executeUpdate(createParkedVehiclesSQL);
            System.out.println("Table 'parked_vehicles' created/verified.");

            // Create vip_plates table
            String createVIPSQL = "CREATE TABLE IF NOT EXISTS vip_plates ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "license_plate VARCHAR(50) NOT NULL UNIQUE,"
                    + "registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")";
            stmt.executeUpdate(createVIPSQL);
            System.out.println("Table 'vip_plates' created/verified.");

            // Create oku_card_holders table
            String createOKUSQL = "CREATE TABLE IF NOT EXISTS oku_card_holders ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "license_plate VARCHAR(50) NOT NULL UNIQUE,"
                    + "registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")";
            stmt.executeUpdate(createOKUSQL);
            System.out.println("Table 'oku_card_holders' created/verified.");

            // Seed: add example OKU plate(s) used by tests/UI
            stmt.executeUpdate("INSERT IGNORE INTO oku_card_holders (license_plate) VALUES ('OKU-1')");

            // Create unpaid_fines table
            String createFinesSQL = "CREATE TABLE IF NOT EXISTS unpaid_fines ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "license_plate VARCHAR(50) NOT NULL,"
                    + "amount DOUBLE DEFAULT 0.0,"
                    + "recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "UNIQUE(license_plate)"
                    + ")";
            stmt.executeUpdate(createFinesSQL);
            System.out.println("Table 'unpaid_fines' created/verified.");
        }
    }
}
