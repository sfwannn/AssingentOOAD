package Management;

import Database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OKUCardChecker {
    public static boolean isOKUCardHolder(String licensePlate) {
        String sql = "SELECT 1 FROM oku_card_holders WHERE license_plate = ? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, licensePlate);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("OKU Card Check Error: " + e.getMessage());
            return false;
        }
    }
}
